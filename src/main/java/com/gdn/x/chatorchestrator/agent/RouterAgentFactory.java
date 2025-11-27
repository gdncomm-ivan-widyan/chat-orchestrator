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
import io.reactivex.rxjava3.core.Flowable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RouterAgentFactory {

  public enum IntentDomain {
    PAYMENT,
    SHIPPING,
    PROMO,
    FRAUD,
    GENERAL
  }

  @Getter
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RouterResult {
    private String domain;   // "PAYMENT" | "SHIPPING" | ...
    private String action;   // "check_order_status" | "check_payment_rule" | "small_talk" | ...
    private String language; // "english" | "indonesia"
  }

  @Getter
  @ToString
  public static class IntentClassification {
    private final IntentDomain domain;
    private final String action;
    private final String language;

    public IntentClassification(IntentDomain domain, String action, String language) {
      this.domain = domain;
      this.action = action;
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
            .description("Routes Blibli CS prompts to PAYMENT, SHIPPING, PROMO, FRAUD, or GENERAL.")
            .model("gemini-2.5-flash") // or your configured model
            .instruction(
                """
                You are a routing agent for Blibli internal tools.

                Task:
                - Read the user input (in English or Indonesian).
                - Decide:
                  - domain: one of ["PAYMENT","SHIPPING","PROMO","FRAUD","GENERAL"]
                  - action: short snake_case action name if applicable (e.g. "check_order_status","check_payment_rule","small_talk","help","unknown")
                  - language: "english" or "indonesia"

                Rules:
                - ALWAYS respond ONLY with a single JSON object.
                - Do NOT include markdown, explanations, or extra text.
                - Example valid responses:
                  {"domain":"PAYMENT","action":"check_order_status","language":"indonesia"}
                  {"domain":"PROMO","action":"help","language":"english"}
                  {"domain":"GENERAL","action":"small_talk","language":"indonesia"}

                Now analyze the user input and respond with JSON only.
                """)
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

      // Collect final response text
      String json = events
          .filter(Event::finalResponse)
          .map(Event::stringifyContent)
          .blockingLast();

      log.info("RouterAgent raw JSON response: {}", json);

      RouterResult routerResult = objectMapper.readValue(json, RouterResult.class);

      IntentDomain domain = mapDomain(routerResult.getDomain());
      return new IntentClassification(domain, routerResult.getAction(), routerResult.getLanguage());
    } catch (Exception e) {
      log.error("Failed to classify intent with RouterAgent, fallback to GENERAL. Error: {}", e.getMessage(), e);
      return new IntentClassification(IntentDomain.GENERAL, "unknown", "indonesia");
    }
  }

  private IntentDomain mapDomain(String rawDomain) {
    if (rawDomain == null) {
      return IntentDomain.GENERAL;
    }
    return switch (rawDomain.toUpperCase()) {
      case "PAYMENT" -> IntentDomain.PAYMENT;
      case "SHIPPING" -> IntentDomain.SHIPPING;
      case "PROMO" -> IntentDomain.PROMO;
      case "FRAUD" -> IntentDomain.FRAUD;
      default -> IntentDomain.GENERAL;
    };
  }
}