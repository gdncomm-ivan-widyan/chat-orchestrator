package com.gdn.x.chatorchestrator.service;

import com.gdn.x.chatorchestrator.agent.RouterAgentFactory;
import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantValue;
import com.gdn.x.chatorchestrator.command.ChatCommandExecutor;
import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.context.UserContext;
import com.gdn.x.chatorchestrator.context.UserContextService;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatOrchestratorServiceTest {

  @Test
  void handleUserMessage_routesToPaymentForPaymentIntent() {
    PaymentClient paymentClient = mock(PaymentClient.class);
    RouterAgentFactory routerAgentFactory = mock(RouterAgentFactory.class);
    ChatCommandExecutor executor = mock(ChatCommandExecutor.class);
    UserContextService userContextService = mock(UserContextService.class);

    UserContext ctx = UserContext.builder()
        .userId("user-1")
        .email("user@example.com")
        .roleNames(Collections.emptyList())
        .build();
    when(userContextService.getCurrentUserContext()).thenReturn(ctx);

    RouterAgentFactory.IntentClassification paymentIntent =
        new RouterAgentFactory.IntentClassification(
            RouterAgentFactory.IntentDomain.PAYMENT,
            "indonesia"
        );
    when(routerAgentFactory.classifyIntent(eq("user-1"), anyString()))
        .thenReturn(paymentIntent);

    // optional: instead of mocking executor, you can spy the real PaymentChatCommand,
    // but simplest is to mock executor behaviour directly
    ChatResult fakeResult = ChatResult.builder()
        .message("Hello from payment")
        .userEmail("user@example.com")
        .userId("user-1")
        .build();
    when(executor.execute(any())).thenReturn(fakeResult);

    ChatOrchestratorService service = new ChatOrchestratorService(
        paymentClient,
        routerAgentFactory,
        executor,
        userContextService
    );

    ChatResult result = service.handleUserMessage("Kenapa order saya masih pending?");

    assertThat(result.getMessage()).isEqualTo("Hello from payment");

    verify(routerAgentFactory).classifyIntent(eq("user-1"), anyString());
    verify(executor).execute(any());
  }
}