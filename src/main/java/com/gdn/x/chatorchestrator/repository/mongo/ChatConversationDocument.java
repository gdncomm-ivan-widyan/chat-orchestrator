package com.gdn.x.chatorchestrator.repository.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "chat_conversation")
public class ChatConversationDocument {

  @Id
  private String id;

  private String userId;
  private List<Message> messages;
  private Instant createdAt;

  @Data
  public static class Message {
    private String role;   // user / assistant / tool
    private String text;
    private Instant timestamp;
  }
}
