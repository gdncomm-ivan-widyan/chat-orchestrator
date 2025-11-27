package com.gdn.x.chatorchestrator.client.payment;

import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;

public interface PaymentClient {

  /**
   * Delegate payment-related questions (e.g. "cek status order 2700...")
   * to payment-service AI assistant.
   */
  PaymentAiAssistantResponse processPrompt(String message);
}
