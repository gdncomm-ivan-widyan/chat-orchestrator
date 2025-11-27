package com.gdn.x.chatorchestrator.command;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ChatResult {
  String message;
  String userEmail;
  String userId;
  List<String> roleNames;
}
