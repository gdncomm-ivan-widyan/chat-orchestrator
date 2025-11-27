package com.gdn.x.chatorchestrator.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RouterAgentFactory {

  public enum IntentDomain {
    PAYMENT,
    PROMO,
    GENERAL
  }

  private static final Schema ROUTER_OUTPUT_SCHEMA =
      Schema.builder()
          .type("OBJECT")
          .description("Routing result for Blibli chat orchestrator.")
          .properties(
              Map.of(
                  "domain",
                  Schema.builder()
                      .type("STRING")
                      .description("Intent domain: PAYMENT, PROMO, or GENERAL.")
                      .build(),
                  "language",
                  Schema.builder()
                      .type("STRING")
                      .description("Language of the user prompt, e.g. 'indonesia' or 'english'.")
                      .build()
              ))
          .build();

  @Getter
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RouterResult {
    private String domain;   // "PAYMENT" | "PROMO" | "GENERAL"
    private String language; // "english" | "indonesia" | null
  }

  @Getter
  @ToString
  public static class IntentClassification {
    private final IntentDomain domain;
    private final String language; // normalized "indonesia"/"english"

    public IntentClassification(IntentDomain domain, String language) {
      this.domain = domain;
      this.language = language;
    }
  }

  private final InMemoryRunner routerRunner;
  private final ObjectMapper objectMapper;

  public RouterAgentFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;

    BaseAgent routerAgent =
        LlmAgent.builder()
            .name("router_agent")
            .description("Routes Blibli CS prompts to PAYMENT or PROMO domains.")
            .model("gemini-2.5-flash")
            .instruction(
                """
                You are a routing agent for Blibli internal tools.

                Supported domains:
                - PAYMENT: questions about payment, order payment status, error codes, etc.
                - PROMO: questions about promos, vouchers, coupons, discounts.
                - GENERAL: anything else that is not payment or promo.

                Examples of PAYMENT questions:
                - "cek status order", "kenapa order id ini gagal"
                - "apakah order dilakukan pembayaran / terdebit beberapa kali"
                - "apakah order kartu kredit pakai poin / berapa poin yang digunakan pada order ini?"
                - "kenapa user tidak bisa menggunakan payment tertentu"
                - "kenapa pembayaran tertentu tidak dapat dipilih"

                PROMO examples:
                - "promo apa yang tersedia?"
                - "kenapa voucher ini tidak bisa dipakai?"
                - "diskon untuk produk ini apa saja?"

                Task:
                - Read the user input (Indonesian or English).
                - Decide the best domain: "PAYMENT", "PROMO", or "GENERAL".
                - Optionally guess language: "indonesia" or "english".

                Rules:
                - ALWAYS respond ONLY with a single JSON object.
                - JSON must match this shape:
                  {
                    "domain": "PAYMENT" | "PROMO" | "GENERAL",
                    "language": "indonesia" | "english"
                  }
                - If you're not sure, use "GENERAL" as domain.
                """)
            .outputSchema(ROUTER_OUTPUT_SCHEMA)
            // Optional: to store it in state, you can also call:
            // .outputKey("router_result")
            .build();

    this.routerRunner = new InMemoryRunner(routerAgent);
  }

  public IntentClassification classifyIntent(String userId, String prompt) {
    try {
      RunConfig runConfig = RunConfig.builder().build();

      // Create / reuse a session per user
      Session session = routerRunner
          .sessionService()
          .createSession(routerRunner.appName(), userId)
          .blockingGet();

      Content userMsg = Content.fromParts(Part.fromText(prompt));
      Flowable<Event> events =
          routerRunner.runAsync(session.userId(), session.id(), userMsg, runConfig);

      String json = events
          .filter(Event::finalResponse)
          .map(Event::stringifyContent)
          .blockingLast();

      log.info("RouterAgent raw JSON response: {}", json);

      RouterResult routerResult = objectMapper.readValue(json, RouterResult.class);

      IntentDomain domain = mapDomain(routerResult.getDomain());
      String language = normalizeLanguage(routerResult.getLanguage());

      return new IntentClassification(domain, language);
    } catch (Exception e) {
      log.error("Failed to classify intent with RouterAgent, fallback to GENERAL. Error: {}", e.getMessage(), e);
      return new IntentClassification(IntentDomain.GENERAL, "indonesia");
    }
  }

  private IntentDomain mapDomain(String rawDomain) {
    if (rawDomain == null) {
      return IntentDomain.GENERAL;
    }
    return switch (rawDomain.toUpperCase()) {
      case "PAYMENT" -> IntentDomain.PAYMENT;
      case "PROMO" -> IntentDomain.PROMO;
      default -> IntentDomain.GENERAL;
    };
  }

  private String normalizeLanguage(String raw) {
    if (raw == null) {
      return "indonesia"; // preferred default
    }
    String lower = raw.toLowerCase();
    if (lower.startsWith("eng")) {
      return "english";
    }
    return "indonesia";
  }
}