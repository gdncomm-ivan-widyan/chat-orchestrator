package com.gdn.x.chatorchestrator.context;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserContext {
  String userId;
  String email;
  List<String> roleNames;
}
