package com.gdn.x.chatorchestrator.service.payment;

import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the "check_order_status" payment action.
 * For now, it still delegates to /x-payment3/api/ai-assistant/prompt.
 * Later you can switch this to a dedicated order-status endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderStatusHandler {

  private final PaymentClient paymentClient;

  public ChatResult handle(String originalMessage, UserContext ctx, String language) {
    log.info("Handling CHECK_ORDER_STATUS for userId={}, language={}, message='{}'",
        ctx.getUserId(), language, originalMessage);

    PaymentAiAssistantResponse resp = paymentClient.processPrompt(originalMessage);

    String message = resp != null && resp.getValue() != null
        ? resp.getValue().getMessage()
        : "[payment-service] Empty response for order status. Original message: " + originalMessage;

    return ChatResult.builder()
        .message(message)
        .userEmail(ctx.getEmail())
        .userId(ctx.getUserId())
        .roleNames(ctx.getRoleNames())
        .build();
  }
}
