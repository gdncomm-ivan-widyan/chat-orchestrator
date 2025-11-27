package com.gdn.x.chatorchestrator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendChatLog(String topic, String key, String payloadJson) {
    kafkaTemplate.send(topic, key, payloadJson);
  }
}
