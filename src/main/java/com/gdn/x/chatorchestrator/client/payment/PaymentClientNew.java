package com.gdn.x.chatorchestrator.client.payment;

import com.gdn.x.chatorchestrator.client.payment.model.PaymentOrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PaymentClientNew {

  private final RestClient restClient;

  @Value("${payment.service.base-url:http://localhost:8081}")
  private String baseUrl;

  public PaymentOrderStatusResponse checkOrderStatus(String orderId) {
    // TODO adjust to your actual payment assistant endpoint
    return restClient.get()
        .uri(baseUrl + "/internal-api/payment-assistant/orders/{orderId}", orderId)
        .retrieve()
        .body(PaymentOrderStatusResponse.class);
  }

  public String checkPaymentRule(String merchant, String category, String product) {
    // TODO adjust to your actual payment rule endpoint
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(baseUrl + "/internal-api/payment-assistant/payment-rules")
            .queryParam("merchant", merchant)
            .queryParam("category", category)
            .queryParam("product", product)
            .build())
        .retrieve()
        .body(String.class);
  }
}