package com.gdn.x.chatorchestrator.command.payment;

import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantValue;
import com.gdn.x.chatorchestrator.command.ChatCommand;
import com.gdn.x.chatorchestrator.command.ChatResult;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
public class PaymentChatCommand implements ChatCommand {

  private final PaymentClient paymentClient;
  private final String prompt;
  private final String userEmail;
  private final String userId;

  @Override
  public ChatResult execute() {
    PaymentAiAssistantResponse resp = paymentClient.processPrompt(prompt);

    // Fallback message if something is wrong
    String message = Optional.ofNullable(resp)
        .map(PaymentAiAssistantResponse::getValue)
        .map(PaymentAiAssistantValue::getMessage)
        .orElse("Payment assistant could not process the request right now.");

    var roleNames = Optional.ofNullable(resp)
        .map(PaymentAiAssistantResponse::getValue)
        .map(PaymentAiAssistantValue::getRoleNames)
        .orElse(Collections.emptyList());

    return ChatResult.builder()
        .message(message)
        .userEmail(userEmail)
        .userId(userId)
        .roleNames(roleNames)
        .build();
  }
}