package com.gdn.x.chatorchestrator.agent;

import lombok.Value;

@Value
public class PaymentActionClassification {
  PaymentAction action;
  String language;   // "english" or "indonesia"
  String message;    // optional natural-language message (for GENERAL / unsupported)
}
