package com.gdn.x.chatorchestrator.client.payment.model;

import lombok.Data;

@Data
public class PaymentAiAssistantResponse {

  private String requestId;
  private String errorMessage;
  private String errorCode;
  private boolean success;
  private PaymentAiAssistantValue value;
}
