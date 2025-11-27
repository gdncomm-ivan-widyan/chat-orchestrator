package com.gdn.x.chatorchestrator.repository.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "chat_session")
@Getter
@Setter
public class ChatSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;

  @Column(length = 2000)
  private String lastMessage;

  private Instant updatedAt;
}
