package com.gdn.x.chatorchestrator.command.payment;

import com.gdn.x.chatorchestrator.agent.PaymentAgentFactory;
import com.gdn.x.chatorchestrator.command.ChatCommand;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentChatCommand implements ChatCommand {

  private final PaymentAgentFactory paymentAgentFactory;
  private final String prompt;
  private final String userEmail;
  private final String userId;

  @Override
  public ChatResult execute() {
    // 1) Build a fresh payment agent for this command
    BaseAgent paymentAgent = paymentAgentFactory.buildPaymentAgent();

    // 2) Wrap it in an in-memory runner
    InMemoryRunner runner = new InMemoryRunner(paymentAgent);

    RunConfig runConfig = RunConfig.builder().build();

    // 3) Create or reuse a session for this user
    Session session = runner
        .sessionService()
        .createSession(runner.appName(), userId)
        .blockingGet();

    // 4) Prepare user message
    Content userMsg = Content.fromParts(Part.fromText(prompt));

    // 5) Run the agent asynchronously and collect events
    Flowable<Event> events =
        runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

    StringBuilder answer = new StringBuilder();
    events.blockingForEach(event -> {
      if (event.finalResponse()) {
        // stringifyContent() gives the aggregated text of the final response
        answer.append(event.stringifyContent());
      }
    });

    return ChatResult.builder()
        .message(answer.toString())
        .userEmail(userEmail)
        .userId(userId)
        .roleNames(null) // TODO: populate from your AccessService equivalent
        .build();
  }
}