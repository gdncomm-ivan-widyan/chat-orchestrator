package com.gdn.x.chatorchestrator.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

  Optional<ChatSessionEntity> findByUserId(String userId);
}
