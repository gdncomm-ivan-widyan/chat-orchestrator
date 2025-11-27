package com.gdn.x.chatorchestrator.command;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatCommandExecutor {

  private static final Logger log = LoggerFactory.getLogger(ChatCommandExecutor.class);

  public ChatResult execute(ChatCommand command) {
    log.debug("Executing chat command: {}", command.getClass().getSimpleName());
    return command.execute();
  }
}
