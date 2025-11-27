package com.gdn.x.chatorchestrator.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatConversationRepository extends MongoRepository<ChatConversationDocument, String> {

  List<ChatConversationDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
