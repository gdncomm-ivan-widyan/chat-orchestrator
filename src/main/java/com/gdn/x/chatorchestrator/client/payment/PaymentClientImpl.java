package com.gdn.x.chatorchestrator.client.payment;

import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantPromptRequest;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient {

  private final RestClient paymentRestClient;

  @Value("${payment.service.store-id:10001}")
  private String storeId;

  @Value("${payment.service.channel-id:web}")
  private String channelId;

  @Value("${payment.service.client-id:web}")
  private String clientId;

  @Value("${payment.service.username:chatorchestrator}")
  private String username;

  @Value("${payment.service.request-id:chatorchestrator}")
  private String requestId;

  @Override
  public PaymentAiAssistantResponse processPrompt(String message) {
    PaymentAiAssistantPromptRequest body = new PaymentAiAssistantPromptRequest(message);

    log.debug("Calling payment-service AI assistant with prompt='{}'", message);

    return paymentRestClient.post()
        .uri(uriBuilder -> uriBuilder
            .path("/x-payment3/api/ai-assistant/prompt")
            .queryParam("storeId", storeId)
            .queryParam("channelId", channelId)
            .queryParam("clientId", clientId)
            .queryParam("username", username)
            .queryParam("requestId", requestId)
            .build())
        .body(body)
        .retrieve()
        .body(PaymentAiAssistantResponse.class);
  }
}
