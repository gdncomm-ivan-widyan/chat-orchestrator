# Chat Orchestrator

`chat-orchestrator` is a Spring Boot service that routes internal Blibli CS / QA prompts
to the appropriate domain agents (starting with **Payment**) and orchestrates calls
to downstream services like **Payment Service AI Assistant**.

It sits in front of existing domain backends and Gemini-based flows, providing:

- A single **HTTP entrypoint** for chat-style prompts.
- **Intent routing** (PAYMENT vs other domains).
- **Command-based orchestration**.
- Integration with existing **Payment AI Assistant** endpoint in `payment-service`.

---

## High-Level Flow

1. Client calls `POST /api/chat/prompt` with a JSON body:

```json
{
  "message": "Kenapa order 123456 masih pending?"
}
```

2. `ChatController` delegates to `ChatOrchestratorService`.

3. `ChatOrchestratorService`:
    - Fetches current `UserContext`.
    - Calls `RouterAgentFactory` (Gemini) to classify intent.
    - Routes to `PaymentChatCommand` for PAYMENT domain.

4. `PaymentChatCommand` calls `PaymentClient`,
   which calls `/x-payment3/api/ai-assistant/prompt`.

5. Response returned as `ChatResponse`.

---

## Components

### ChatController
- Endpoint: `POST /api/chat/prompt`
- Uses DTOs: `ChatRequest`, `ChatResponse`.

### ChatOrchestratorService
- Handles routing.
- Calls router.
- Executes command.

### RouterAgentFactory
- Gemini‑powered intent classifier.
- Currently supports: PAYMENT, PROMO, GENERAL.
- In tests, mocked via `@MockitoBean`.

### PaymentAgentFactory / PaymentChatCommand
- Delegates to Payment Service's AI Assistant.
- Wraps results into a `ChatResult`.

### PaymentClient
- Uses Spring `RestClient`.
- Calls payment backend.

---

## Configuration

Set environment variables:

```
export GENAI_API_KEY=your-gemini-key
export PAYMENT_SERVICE_BASE_URL=https://payment-service-next.qa2-sg.cld
```

---

## Running Locally

```
mvn clean install
mvn spring-boot:run
```

Test with:

```
curl -X POST http://localhost:8090/api/chat/prompt   -H "Content-Type: application/json"   -d '{"message":"cek status order 27000000001"}'
```

---

## Testing

- Uses Spring Boot Test, WireMock, Mockito.
- RouterAgentFactory is mocked so tests do not call Gemini.

---

## Roadmap
- Add PROMO agent.
- Migrate full Gemini prompt logic from payment-service.
- Add session history.