package com.gdn.x.chatorchestrator.context;

import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Placeholder for user context resolution.
 * Integrate later on with existing AccessService / MDC / security context.
 */
@Service
public class UserContextService {

  public UserContext getCurrentUserContext() {
    return UserContext.builder()
        .userId("dummy-user")
        .email("dummy@example.com")
        .roleNames(Collections.emptyList())
        .build();
  }
}
