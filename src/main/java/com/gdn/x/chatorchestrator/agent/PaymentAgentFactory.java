package com.gdn.x.chatorchestrator.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PaymentAgentFactory {

  private final BaseAgent paymentRouterAgent;
  private final InMemoryRunner paymentRouterRunner;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final Schema ROUTER_OUTPUT_SCHEMA =
      Schema.builder()
          .type("OBJECT")
          .description("Routing result for Blibli - Payment chat orchestrator.")
          .properties(
              Map.of(
                  "action",
                  Schema.builder()
                      .type("STRING")
                      .description("Intent domain: PAYMENT, PROMO, or GENERAL.")
                      .build(),
                  "language",
                  Schema.builder()
                      .type("STRING")
                      .description("Language of the user prompt, e.g. 'indonesia' or 'english'.")
                      .build(),
                  "message",
                  Schema.builder()
                      .type("STRING")
                      .description("Natural language reply")
                      .build()
              ))
          .build();

  public PaymentAgentFactory() {
    // This agent only decides which PAYMENT action to take.
    this.paymentRouterAgent = LlmAgent.builder()
        .name("payment-router-agent")
        .model("gemini-2.5-flash")
        .description("""
            Classifies payment-related questions from Blibli CS agents.
            Determines if the question is to:
            - check order/payment status,
            - check conditional payment rules, or
            - just a greeting/help/unsupported.
            """)
        .instruction("""
            You are a PAYMENT routing assistant for Blibli internal CS tooling.
            
            Your job:
            1. Read the user's input (Bahasa Indonesia or English).
            2. Decide ONE of these actions:
               - "check_order_status"
               - "check_payment_rule"
               - "general"      (greeting, asking for help, small talk)
               - "unsupported"  (anything not related to those payment tasks)
            3. Detect the language:
               - "indonesia" if the input is mainly Bahasa Indonesia
               - "english"   if the input is mainly English

            Examples that should map to "check_order_status":
            - "cek status order 27000000001"
            - "kenapa order id ini gagal?"
            - "apakah order ini terdebit beberapa kali?"
            - "apakah order kartu kredit pakai poin / berapa poin yang digunakan pada order ini?"
            
            Examples that should map to "check_payment_rule":
            - "kenapa user tidak bisa menggunakan payment tertentu?"
            - "kenapa pembayaran tertentu tidak dapat dipilih?"
            - "kenapa metode X tidak tersedia untuk produk/merchant/kategori ini?"

            Output:
            - ALWAYS respond ONLY in valid JSON with EXACTLY:
              {
                "action": "<check_order_status|check_payment_rule|general|unsupported>",
                "language": "<english|indonesia>",
                "message": "<optional short natural language reply>"
              }

            Notes:
            - For "general" or "unsupported":
              * Put a short friendly explanation in "message", mentioning briefly what you can do.
            - For "check_order_status" or "check_payment_rule":
              * "message" can be empty or a short summary; the main signal is "action".
            - Do NOT add any extra fields, markdown, or text outside the JSON.
            """)
        .outputSchema(ROUTER_OUTPUT_SCHEMA)
        .build();

    // Runner to execute this agent
    this.paymentRouterRunner = new InMemoryRunner(this.paymentRouterAgent);
  }

  /**
   * Expose the raw router agent in case you ever want to call it directly.
   */
  public BaseAgent buildPaymentRouterAgent() {
    return paymentRouterAgent;
  }

  /**
   * Classify PAYMENT action using ADK runner + session + events.
   */
  public PaymentActionClassification classifyPaymentAction(String userId, String message) {
    try {
      // 1) Create (or reuse) a session for this user
      Session session = paymentRouterRunner
          .sessionService()
          .createSession(paymentRouterRunner.appName(), userId)
          .blockingGet();

      // 2) Build user message content
      Content userMsg = Content.fromParts(Part.fromText(message));
      RunConfig config = RunConfig.builder().build();

      // 3) Run async & collect events
      Flowable<Event> events =
          paymentRouterRunner.runAsync(session.userId(), session.id(), userMsg, config);

      String raw = events
          .filter(Event::finalResponse)
          .map(Event::stringifyContent)
          .blockingLast();

      log.info("PaymentRouterAgent raw response for userId={}, message='{}': {}",
          userId, message, raw);

      // 4) Parse JSON safely using Jackson
      JsonNode json = objectMapper.readTree(raw);

      String actionStr = json.has("action") ? json.get("action").asText() : "unsupported";
      String language = json.has("language") ? json.get("language").asText() : "indonesia";
      String msg = json.has("message") ? json.get("message").asText() : "";

      PaymentAction action = switch (actionStr.toLowerCase()) {
        case "check_order_status" -> PaymentAction.CHECK_ORDER_STATUS;
        case "check_payment_rule" -> PaymentAction.CHECK_PAYMENT_RULE;
        case "general" -> PaymentAction.GENERAL;
        case "unsupported" -> PaymentAction.UNKNOWN;
        default -> PaymentAction.UNKNOWN;
      };

      return new PaymentActionClassification(action, language, msg);

    } catch (Exception e) {
      log.error("Failed to classify payment action for userId={}, message='{}': {}",
          userId, message, e.getMessage(), e);

      return new PaymentActionClassification(
          PaymentAction.UNKNOWN,
          "indonesia",
          "Maaf, saya tidak bisa mengenali jenis pertanyaan pembayaran ini."
      );
    }
  }
}