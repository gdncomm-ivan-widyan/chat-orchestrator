package com.gdn.x.chatorchestrator.service.payment;

import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the "check_payment_rule" payment action.
 * For now, still delegates to /x-payment3/api/ai-assistant/prompt.
 * Later you can switch this to conditional-payment service APIs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRuleHandler {

  private final PaymentClient paymentClient;

  public ChatResult handle(String originalMessage, UserContext ctx, String language) {
    log.info("Handling CHECK_PAYMENT_RULE for userId={}, language={}, message='{}'",
        ctx.getUserId(), language, originalMessage);

    PaymentAiAssistantResponse resp = paymentClient.processPrompt(originalMessage);

    String message = resp != null && resp.getValue() != null
        ? resp.getValue().getMessage()
        : "[payment-service] Empty response for payment rule. Original message: " + originalMessage;

    return ChatResult.builder()
        .message(message)
        .userEmail(ctx.getEmail())
        .userId(ctx.getUserId())
        .roleNames(ctx.getRoleNames())
        .build();
  }
}
