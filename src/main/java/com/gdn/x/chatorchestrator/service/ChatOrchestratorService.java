package com.gdn.x.chatorchestrator.service;

import com.gdn.x.chatorchestrator.agent.PaymentAgentFactory;
import com.gdn.x.chatorchestrator.agent.RouterAgentFactory;
import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.command.ChatCommandExecutor;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.command.payment.PaymentChatCommand;
import com.gdn.x.chatorchestrator.context.UserContext;
import com.gdn.x.chatorchestrator.context.UserContextService;
import com.gdn.x.chatorchestrator.service.payment.PaymentOrderStatusHandler;
import com.gdn.x.chatorchestrator.service.payment.PaymentRuleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestratorService {

  private final PaymentClient paymentClient; // still injected for future use if needed
  private final RouterAgentFactory routerAgentFactory;
  private final ChatCommandExecutor commandExecutor;
  private final UserContextService userContextService;

  private final PaymentAgentFactory paymentAgentFactory;
  private final PaymentOrderStatusHandler paymentOrderStatusHandler;
  private final PaymentRuleHandler paymentRuleHandler;

  public ChatResult handleUserMessage(String message) {
    UserContext ctx = userContextService.getCurrentUserContext();
    RouterAgentFactory.IntentClassification intent =
        routerAgentFactory.classifyIntent(ctx.getUserId(), message);

    RouterAgentFactory.IntentDomain domain = intent.getDomain();

    log.info("RouterAgent classified intent={} for userId={}", domain, ctx.getUserId());

    return switch (domain) {
      case PAYMENT -> handlePayment(message, ctx);
      case PROMO -> handlePromo(message, ctx); // TODO later
      case GENERAL -> handleNotImplemented(message, ctx, domain);
    };
  }

  private ChatResult handlePayment(String message, UserContext ctx) {
    // New DI-based PaymentChatCommand
    PaymentChatCommand cmd = new PaymentChatCommand(
        message,
        userContextService,
        paymentAgentFactory,
        paymentOrderStatusHandler,
        paymentRuleHandler
    );
    return commandExecutor.execute(cmd);
  }

  private ChatResult handlePromo(String message, UserContext ctx) {
    String reply = "[PROMO stub] Promo domain not yet implemented. Original message: " + message;
    return ChatResult.builder()
        .message(reply)
        .userEmail(ctx.getEmail())
        .userId(ctx.getUserId())
        .roleNames(ctx.getRoleNames())
        .build();
  }

  private ChatResult handleNotImplemented(
      String message,
      UserContext ctx,
      RouterAgentFactory.IntentDomain domain
  ) {
    String reply = "[RouterAgent stub] Domain " + domain +
        " is not yet implemented in ChatOrchestrator. Original message: " + message;
    return ChatResult.builder()
        .message(reply)
        .userEmail(ctx.getEmail())
        .userId(ctx.getUserId())
        .roleNames(ctx.getRoleNames())
        .build();
  }
}