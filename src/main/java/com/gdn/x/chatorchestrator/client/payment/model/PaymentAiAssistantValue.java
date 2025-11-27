package com.gdn.x.chatorchestrator.client.payment.model;

import lombok.Data;

import java.util.List;

@Data
public class PaymentAiAssistantValue {
  private String message;
  private List<String> roleNames;
}
