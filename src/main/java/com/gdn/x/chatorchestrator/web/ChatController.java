package com.gdn.x.chatorchestrator.web;

import com.gdn.x.chatorchestrator.command.ChatResult;
import com.gdn.x.chatorchestrator.service.ChatOrchestratorService;
import com.gdn.x.chatorchestrator.web.dto.ChatRequest;
import com.gdn.x.chatorchestrator.web.dto.ChatResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "ChatController", description = "Chat Orchestrator API")
public class ChatController {

  private final ChatOrchestratorService chatOrchestratorService;

  @PostMapping(value = "/prompt", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ChatResponse processPrompt(@RequestBody ChatRequest request) {
    ChatResult result = chatOrchestratorService.handleUserMessage(request.getMessage());

    return ChatResponse.builder()
        .message(result.getMessage())
        .userEmail(result.getUserEmail())
        .userId(result.getUserId())
        .roleNames(result.getRoleNames())
        .build();
  }
}