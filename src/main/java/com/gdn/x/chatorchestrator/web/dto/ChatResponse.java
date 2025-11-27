// src/main/java/com/gdn/x/chatorchestrator/web/dto/ChatResponse.java
package com.gdn.x.chatorchestrator.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
  private String message;
  private String userEmail;
  private String userId;
  private List<String> roleNames;
}
