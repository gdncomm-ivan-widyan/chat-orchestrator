package com.gdn.x.chatorchestrator.agent;

import com.gdn.x.chatorchestrator.client.payment.PaymentClient;
import com.gdn.x.chatorchestrator.client.payment.model.PaymentOrderStatusResponse;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentAgentFactory {

  private final PaymentClient paymentClient;

  public PaymentAgentFactory(PaymentClient paymentClient) {
    this.paymentClient = paymentClient;
  }

  public BaseAgent buildPaymentAgent() {
    return LlmAgent.builder()
        .name("payment-assistant-agent")
        .description("""
            Handles all payment-related customer questions: why an order is pending,
            payment status, and payment rules/filters for products, merchants, and categories.
            """)
        .model("gemini-2.5-flash") // or your chosen model
        .instruction("""
            You are a payment assistant for Blibli CS.
            - For questions about why an order is still pending or payment status,
              call the `getOrderStatus` tool.
            - For questions about allowed/blocked payment methods, promotions, or
              conditional payment rules, call the `getPaymentRuleDetails` tool.
            Always respond in a friendly, concise style, and never expose internal error codes.
            """)
        .tools(
            FunctionTool.create(this, "getOrderStatus"),
            FunctionTool.create(this, "getPaymentRuleDetails")
        )
        .build();
  }

//  @Schema(description = "Get a human-readable payment status for an order")
//  public Map<String, String> getOrderStatus(
//      @Schema(name = "orderId", description = "Customer order ID") String orderId) {
//
//    PaymentOrderStatusResponse resp = paymentClient.checkOrderStatus(orderId);
//
//    String message = "Order %s is currently %s".formatted(
//        resp.getOrderId(),
//        resp.getPaymentStatus()
//    );
//    return Map.of(
//        "orderId", resp.getOrderId(),
//        "status", resp.getPaymentStatus(),
//        "message", message
//    );
//  }
//
//  @Schema(description = "Get payment rule details for a combination of merchant, category, and product")
//  public Map<String, String> getPaymentRuleDetails(
//      @Schema(name = "merchant", description = "Merchant code (can be null)") String merchant,
//      @Schema(name = "category", description = "Category code (can be null)") String category,
//      @Schema(name = "product", description = "Product code (can be null)") String product) {
//
//    String rulesSummary = paymentClient.checkPaymentRule(merchant, category, product);
//
//    return Map.of(
//        "merchant", merchant == null ? "" : merchant,
//        "category", category == null ? "" : category,
//        "product", product == null ? "" : product,
//        "rulesSummary", rulesSummary
//    );
//  }
}