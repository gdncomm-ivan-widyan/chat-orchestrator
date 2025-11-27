package com.gdn.x.chatorchestrator.web;

import com.gdn.x.chatorchestrator.web.dto.ChatRequest;
import com.gdn.x.chatorchestrator.web.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)   // Spring starts WireMock on random port, exposed as wiremock.server.port
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

  @LocalServerPort
  int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeEach
  void setupWiremock() {
    // No need configureFor(host,port) when using @AutoConfigureWireMock
    stubFor(get(urlMatching("/internal-api/payment-assistant/orders/.*"))
        .willReturn(okJson("""
            {
              "orderId": "123456",
              "paymentStatus": "WAITING_PAYMENT"
            }
            """)));
  }

  @Test
  void processPrompt_shouldReturnMessage() {
    ChatRequest request = new ChatRequest();
    request.setMessage("Kenapa order 123456 masih pending?");

    var response = restTemplate.postForEntity(
        "http://localhost:" + port + "/api/chat/prompt",
        request,
        ChatResponse.class
    );

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).contains("order");
  }
}