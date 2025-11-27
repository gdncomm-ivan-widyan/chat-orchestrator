package com.gdn.x.chatorchestrator.service;

import com.gdn.x.chatorchestrator.agent.PaymentAgentFactory;
import com.gdn.x.chatorchestrator.agent.RouterAgentFactory;
import com.gdn.x.chatorchestrator.command.ChatCommandExecutor;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.command.payment.PaymentChatCommand;
import com.gdn.x.chatorchestrator.context.UserContext;
import com.gdn.x.chatorchestrator.context.UserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestratorService {

  private final PaymentAgentFactory paymentAgentFactory;
  private final RouterAgentFactory routerAgentFactory;
  private final ChatCommandExecutor commandExecutor;
  private final UserContextService userContextService;

  public ChatResult handleUserMessage(String message) {
    UserContext ctx = userContextService.getCurrentUserContext();

    // 🔹 Call RouterAgent and get full classification (domain + action + language)
    RouterAgentFactory.IntentClassification classification =
        routerAgentFactory.classifyIntent(ctx.getUserId(), message);

    RouterAgentFactory.IntentDomain domain = classification.getDomain();

    log.info("RouterAgent classified intent={} (action={}, lang={}) for userId={}",
        domain, classification.getAction(), classification.getLanguage(), ctx.getUserId());

    return switch (domain) {
      case PAYMENT -> handlePayment(message, ctx /* later you can also pass classification */);
      case SHIPPING, PROMO, FRAUD, GENERAL -> handleNotImplemented(message, ctx, domain);
    };
  }

  private ChatResult handlePayment(String message, UserContext ctx) {
    PaymentChatCommand cmd = new PaymentChatCommand(
        paymentAgentFactory,
        message,
        ctx.getEmail(),
        ctx.getUserId()
    );
    return commandExecutor.execute(cmd);
  }

  private ChatResult handleNotImplemented(String message,
      UserContext ctx,
      RouterAgentFactory.IntentDomain domain) {
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