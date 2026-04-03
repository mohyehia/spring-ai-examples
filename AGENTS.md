# AI Agent Guide for spring-ai-examples

## Project Overview

This is a Spring Boot 4.0.5 repository demonstrating **Spring AI integration with Ollama**, a local LLM orchestration framework. The single module (`spring-ai-ollama`) runs on port 8083 and exposes REST endpoints for various AI-powered operations using Ollama's LLM models.

**Key Technologies:**
- Spring Boot 4.0.5 (Java 21)
- Spring AI 2.0.0-M4 (specifically `spring-ai-starter-model-ollama`)
- Maven for build management
- Ollama chat model: `llama3.1:8b` (configurable)

## Architecture & Data Flow

### Component Structure
```
┌─────────────────────────────────────────┐
│       Spring Boot REST Application      │
│  Port 8083                              │
│                                         │
│  ┌─────────────────┐  ┌──────────────┐ │
│  │ ChatController  │  │ PromptCtrlr  │ │
│  │ GET /           │  │ GET /prompts │ │
│  └────────┬────────┘  └──────┬───────┘ │
│           │                   │        │
│  ┌────────▼───────────────────▼──────┐ │
│  │   Spring AI ChatClient             │ │
│  │ (Auto-configured by Spring)        │ │
│  └────────┬──────────────────────────┘ │
│           │                            │
└───────────┼────────────────────┬───────┘
            │                    │
      ┌─────▼───────┐    ┌──────▼──────────┐
      │   Ollama    │    │ application.yaml│
      │ localhost   │    │ + prompts/**    │
      │  :11434     │    └─────────────────┘
      └─────────────┘
```

**Critical Integration Point:** Both `ChatController` and `PromptController` use constructor injection of `ChatClient.Builder` which is **auto-configured by Spring AI**. The `ChatClient` abstracts direct Ollama API calls.

### Configuration
- **Model selection:** Configured in `application.yaml` under `spring.ai.ollama.chat.model`
- **Ollama endpoint:** Defaults to `localhost:11434` (Ollama's default port)
- **Server port:** 8083 (distinct from Ollama)
- **Prompt templates:** Located in `src/main/resources/prompts/` (StringTemplate format `.st`)

## Developer Workflows

### Build & Run
```bash
# From spring-ai-ollama directory
mvn clean install           # Full build with tests
mvn spring-boot:run         # Start application on port 8083
./mvnw spring-boot:run      # Cross-platform alternative (Windows/Unix)
```

### Testing Endpoints
- **GET** `http://localhost:8083/` → Returns joke from Ollama model (ChatController)
- **GET** `http://localhost:8083/prompts` → Simple prompt response (PromptController)
- **GET** `http://localhost:8083/prompts/popular?genre=tech` → Template-based response with parameters
- **GET** `http://localhost:8083/prompts/instruct?userInput=Tell%20me%20a%20Joke` → System prompt + user message
- HTTP client requests available in `generated-http-requests.http`

### Dependencies
- Spring Web auto-configures embedded Tomcat
- `spring-ai-starter-model-ollama` transitively brings Ollama chat dependencies via BOM import
- No explicit test runner needed—JUnit 5 + Spring Boot Test included

## Code Patterns & Conventions

### Spring AI ChatClient Usage Patterns

**Pattern 1: Simple User Prompt (ChatController)**
```java
chatClient.prompt()
  .user("Tell me a joke about computers")
  .call()
  .chatClientResponse()
```

**Pattern 2: Prompt Template with Parameters (PromptController.popular)**
```java
Prompt prompt = new PromptTemplate(resourceFile)
  .create(Map.of("key", "value"));
chatClient.prompt(prompt)
  .call()
  .chatResponse()
```

**Pattern 3: System + User Messages (PromptController.instruct)**
```java
SystemMessage systemMessage = new SystemMessage(systemPromptResource);
UserMessage userMessage = new UserMessage(userInput);
Prompt prompt = new Prompt(systemMessage, userMessage);
chatClient.prompt(prompt)
  .call()
  .chatResponse()
```

### Prompt Template Files (.st format)

StringTemplate files in `src/main/resources/prompts/` support variable interpolation:
- `youtube-prompt.st` - Used for `/prompts/popular` endpoint with `{genre}` parameter
- `system-prompt.st` - Used for `/prompts/instruct` endpoint as system context

Load templates via `@Value("classpath:prompts/filename.st")` and pass to `PromptTemplate`.

### Response Object Handling

**ChatClientResponse** (ChatController):
- Direct response wrapper with `.toString()` method
- Contains content and metadata

**ChatResponse** (PromptController):
- Returns list of results via `.getResults()`
- Extract text: `response.getResults().getFirst().getOutput().getText()`
- Access metadata: `response.getMetadata()`

### Logging
- SLF4J with loggers created via `LoggerFactory.getLogger()`
- Model responses logged at INFO level for debugging
- Framework: Spring AI logs available at DEBUG level

### Dependency Injection
- Constructor injection (both controllers receive `ChatClient.Builder`)
- Spring auto-configuration handles ChatClient bean creation
- No explicit @Bean definitions needed for AI components

## Critical Developer Knowledge

### Ollama Prerequisites
- Ollama service must be running on `localhost:11434` before starting the app
- Model `llama3.1:8b` must be available locally: `ollama pull llama3.1:8b`
- The application **fails silently** if Ollama isn't accessible—check logs carefully

### Spring AI Stability Notes
- Using 2.0.0-M4 (milestone release, not stable)
- API may change; refer to `spring-ai-starter-model-ollama` docs if unexpected issues occur
- ChatClient/ChatResponse object handling differs between endpoints

### Extension Points
1. **Add new endpoints:** Create methods in controllers with different prompts
2. **Change models:** Update `spring.ai.ollama.chat.model` in `application.yaml`
3. **Create new prompt templates:** Add `.st` files in `src/main/resources/prompts/` and inject with `@Value`
4. **Add custom AI logic:** Extend ChatClient usage with streaming, function calling (see Spring AI docs)
5. **Add storage/persistence:** No current database layer—would need additional dependencies

## File Reference
- **Main logic:** 
  - `src/main/java/com/moh/yehia/ollama/ChatController.java` - Simple joke endpoint
  - `src/main/java/com/moh/yehia/ollama/PromptController.java` - Advanced prompting patterns
- **Prompt templates:** `src/main/resources/prompts/` (`.st` StringTemplate files)
- **Config:** `src/main/resources/application.yaml`
- **Tests:** `src/test/java/.../SpringAiOllamaApplicationTests.java` (minimal—context loads test only)
- **Build config:** `pom.xml`

## Common Pitfalls
- **Ollama not running:** App starts but requests hang or fail. Check `localhost:11434/api/tags`
- **Wrong model name:** Typos in `application.yaml` cause runtime errors, not compile errors
- **Port conflicts:** Port 8083 must be available; change in `application.yaml` if needed
- **Test execution:** Tests are minimal and don't validate AI responses—only context loading
- **Prompt template not found:** Ensure `.st` files exist in `src/main/resources/prompts/` before building
- **Variable interpolation failures:** Verify `Map.of()` keys match placeholders in template (e.g., `{genre}`)

