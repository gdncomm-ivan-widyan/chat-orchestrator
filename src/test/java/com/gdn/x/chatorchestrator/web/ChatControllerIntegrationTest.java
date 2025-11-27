package com.gdn.x.chatorchestrator.web;

import com.gdn.x.chatorchestrator.agent.RouterAgentFactory;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

  @LocalServerPort
  int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @MockitoBean
  private RouterAgentFactory routerAgentFactory;

  @BeforeEach
  void setup() {
    when(routerAgentFactory.classifyIntent(anyString(), anyString()))
        .thenReturn(new RouterAgentFactory.IntentClassification(
            RouterAgentFactory.IntentDomain.PAYMENT,
            "indonesia"
        ));

    stubFor(post(urlPathEqualTo("/x-payment3/api/ai-assistant/prompt"))
        .willReturn(okJson("""
            {
              "requestId": "test-request",
              "errorMessage": null,
              "errorCode": null,
              "success": true,
              "value": {
                "message": "Order 123456 masih pending karena menunggu pembayaran.",
                "roleNames": []
              }
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
    assertThat(response.getBody().getMessage()).contains("Order 123456");
  }
}
