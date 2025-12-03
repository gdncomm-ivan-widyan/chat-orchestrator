package com.gdn.x.chatorchestrator.command.payment;

import com.gdn.x.chatorchestrator.agent.PaymentActionClassification;
import com.gdn.x.chatorchestrator.agent.PaymentAgentFactory;
import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.command.ChatCommand;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.context.UserContext;
import com.gdn.x.chatorchestrator.context.UserContextService;
import com.gdn.x.chatorchestrator.service.payment.PaymentOrderStatusHandler;
import com.gdn.x.chatorchestrator.service.payment.PaymentRuleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates PAYMENT domain flow:
 *  1) PaymentAgentFactory (LLM) classifies payment action
 *  2) Depending on action -> route to order-status or payment-rule handler
 */
@Slf4j
@RequiredArgsConstructor
public class PaymentChatCommand implements ChatCommand {

  private final String message;
  private final UserContextService userContextService;
  private final PaymentAgentFactory paymentAgentFactory;
  private final PaymentOrderStatusHandler orderStatusHandler;
  private final PaymentRuleHandler paymentRuleHandler;

  /**
   * Backward-compatible constructor you currently use:
   * new PaymentChatCommand(paymentClient, message, ctx.getEmail(), ctx.getUserId())
   *
   * To avoid changing ChatOrchestratorService for now, you can:
   *  - Wire this via a factory or
   *  - Update ChatOrchestratorService to use the DI-based constructor instead.
   *
   * Easiest is to update ChatOrchestratorService (see next section).
   */
  @Deprecated
  public PaymentChatCommand(
      PaymentClient unusedLegacyPaymentClient,
      String message,
      String email,
      String userId
  ) {
    throw new UnsupportedOperationException(
        "Please switch ChatOrchestratorService to use the DI-based PaymentChatCommand constructor.");
  }

  @Override
  public ChatResult execute() {
    UserContext ctx = userContextService.getCurrentUserContext();

    PaymentActionClassification classification =
        paymentAgentFactory.classifyPaymentAction(ctx.getUserId(), message);

    log.info("PaymentAgent classified action={} language={} for userId={}",
        classification.getAction(), classification.getLanguage(), ctx.getUserId());

    return switch (classification.getAction()) {
      case CHECK_ORDER_STATUS ->
          orderStatusHandler.handle(message, ctx, classification.getLanguage());

      case CHECK_PAYMENT_RULE ->
          paymentRuleHandler.handle(message, ctx, classification.getLanguage());

      case GENERAL -> ChatResult.builder()
          .message(classification.getMessage())
          .userEmail(ctx.getEmail())
          .userId(ctx.getUserId())
          .roleNames(ctx.getRoleNames())
          .build();

      case UNKNOWN -> ChatResult.builder()
          .message("[PaymentAgent] Jenis pertanyaan pembayaran belum didukung. Pesan asli: " + message)
          .userEmail(ctx.getEmail())
          .userId(ctx.getUserId())
          .roleNames(ctx.getRoleNames())
          .build();
    };
  }
}