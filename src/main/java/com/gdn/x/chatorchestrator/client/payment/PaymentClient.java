package com.gdn.x.chatorchestrator.client.payment;

import com.gdn.x.chatorchestrator.client.payment.model.PaymentAiAssistantResponse;

public interface PaymentClient {

  PaymentAiAssistantResponse processPrompt(String message);
}
