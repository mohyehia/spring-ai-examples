# AI Agent Guide for spring-ai-examples

## 📋 Quick Navigation

- [Project Overview](#project-overview) - Architecture and modules
- [Developer Workflows](#developer-workflows) - Build, run, and test
- [Code Patterns & Conventions](#code-patterns--conventions) - Implementation examples
- [Module Selection Guide](#module-selection-guide) - When to use which module
- [Extension Points](#extension-points) - How to extend
- [Critical Developer Knowledge](#critical-developer-knowledge) - Must-know information
- [Common Pitfalls](#common-pitfalls) - Issues and solutions
- [File Reference](#file-reference) - Project structure and locations

---

## Project Overview

This is a **Spring Boot 4.0.5 repository** demonstrating **Spring AI integration patterns** with three distinct modules:

1. **`spring-ai-ollama`** (Port 8083) - Core AI patterns with Ollama LLM (6 controllers, 22+ endpoints)
2. **`spring-ai-redis-chat-memory`** (Port 8084) - Persistent chat memory with Redis (1 controller, 1 endpoint)
3. **`spring-ai-jdbc-chat-memory`** (Port 8085) - Persistent chat memory with PostgreSQL JDBC (1 controller, 3 endpoints)

**Key Technologies:**
- Spring Boot 4.0.5 (Java 21)
- Spring AI 2.0.0-M4 (specifically `spring-ai-starter-model-ollama`)
- Ollama chat models: `gemma4:e2b` (multimodal, recommended), `llama3.2:3b` (lightweight)
- Redis 7.0+ (for redis-chat-memory module, optional)
- PostgreSQL 12+ (for jdbc-chat-memory module, optional)
- Maven for build management

## Architecture & Data Flow

### Multi-Module Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    Spring Boot 4.0.5 REST Applications                   │
│                                                                          │
│  ┌────────────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │ spring-ai-ollama       │  │ spring-ai-redis │  │ spring-ai-jdbc  │    │
│  │ Port 8083              │  │ -chat-memory    │  │ -chat-memory    │    │
│  │                        │  │ Port 8084       │  │ Port 8085       │    │
│  │ • ChatController       │  │                 │  │                 │    │
│  │ • PromptController     │  │ RedisChatMemory │  │ ChatMemoryCtrlr │    │
│  │ • OutputParserCtrlr    │  │ Controller      │  │ (3 endpoints)   │    │
│  │ • MetadataController   │  │ (1 endpoint)    │  │ • /{username}   │    │
│  │ • ChatMemoryController │  │ • /memory       │  │ • /{u}/conv     │    │
│  │ • MultiModalController │  │                 │  │ • /conversations│    │
│  └────────────┬───────────┘  └───────┬─────────┘  └────────┬────────┘    │
└───────────────┼──────────────────────┼─────────────────────┼─────────────┘
                │                      │                     │
        ┌───────▼─────────┐   ┌────────▼────────┐   ┌────────▼─────────┐
        │ Ollama          │   │ Redis           │   │ PostgreSQL       │
        │ localhost:11434 │   │ localhost:6379  │   │ localhost:5432   │
        │ (gemma4:e2b)    │   │ (v7.0+)         │   │ (v12+)           │
        └─────────────────┘   └─────────────────┘   └──────────────────┘
```

### Module Breakdown

**spring-ai-ollama** (6 Controllers, 22+ Endpoints):
- ChatController - Simple Q&A with logging
- PromptController - Template-based & system messages
- OutputParserController - Structured output (List, Map, DTO, Entity, ResponseEntity, Stream)
- MetadataController - Custom metadata & advisor patterns
- ChatMemoryController - In-memory conversation management
- MultiModalController - Image analysis with multimodal models

**spring-ai-redis-chat-memory** (1 Controller, 1 Endpoint):
- RedisChatMemoryController - Redis-backed persistent chat (TTL: 24h)

**spring-ai-jdbc-chat-memory** (1 Controller, 3 Endpoints):
- ChatMemoryController - JDBC-backed persistent chat with PostgreSQL
  - `GET /{username}` - Chat with memory
  - `GET /{username}/conversations` - View conversation history
  - `GET /conversations` - List all conversation IDs

### Critical Integration Points

1. **ChatClient.Builder** - Auto-configured by Spring AI, injected via constructor
2. **PromptTemplate** - StringTemplate-based prompt parameterization
3. **Output Converters** - ListOutputConverter, MapOutputConverter, BeanOutputConverter
4. **Memory Advisors** - MessageChatMemoryAdvisor, PromptChatMemoryAdvisor
5. **Redis Repository** - RedisChatMemoryRepository for distributed state

### Configuration

**spring-ai-ollama:**
- **Model selection:** `spring.ai.ollama.chat.model` in `application.yaml`
- **Ollama endpoint:** `localhost:11434` (Ollama default)
- **Server port:** 8083
- **Prompt templates:** `src/main/resources/prompts/` (StringTemplate `.st` format)
- **Output converters:** ListOutputConverter, MapOutputConverter, BeanOutputConverter, native `.entity()`, `.responseEntity()`
- **Default model:** `gemma4:e2b` (multimodal), fallback: `llama3.2:3b` (text-only)

**spring-ai-redis-chat-memory:**
- **Redis endpoint:** `localhost:6379` (Redis default)
- **Redis version:** 7.0+ required
- **Memory repository:** RedisChatMemoryRepository (auto-configured)
- **Server port:** 8084
- **Default model:** `llama3.2:3b` (lightweight text processing)
- **TTL:** 24 hours configurable

**spring-ai-jdbc-chat-memory:**
- **PostgreSQL endpoint:** `localhost:5432` (PostgreSQL default)
- **PostgreSQL version:** 12+ required
- **Memory repository:** JdbcChatMemoryRepository (auto-configured)
- **Server port:** 8085
- **Default model:** `llama3.2:3b` (lightweight text processing)
- **Schema:** Auto-created on startup via `initialize-schema: embedded`
- **Storage:** Permanent with full SQL capabilities

## Developer Workflows

### Build & Run

**Build all modules:**
```bash
mvn clean install           # Full build with tests
mvn clean install -DskipTests  # Skip tests for speed
```

**Run spring-ai-ollama (Port 8083):**
```bash
cd spring-ai-ollama
mvn spring-boot:run
# Or: ./mvnw spring-boot:run (cross-platform)
```

**Run spring-ai-redis-chat-memory (Port 8084):**
```bash
cd spring-ai-redis-chat-memory
mvn spring-boot:run
# Requires Redis 7.0+ running on localhost:6379
```

### Testing Endpoints

**spring-ai-ollama endpoints:**
- `GET /` → Joke from LLM (ChatController)
- `GET /prompts` → Simple prompt (PromptController)
- `GET /prompts/popular?genre=tech` → Parameterized template
- `GET /prompts/instruct?userInput=Tell%20me%20a%20Joke` → System + user messages
- `GET /parser?artist=Adele` → Text response (OutputParserController)
- `GET /parser/list?artist=Adele` → ListOutputConverter
- `GET /parser/map?artist=Adele` → MapOutputConverter
- `GET /parser/dto?artist=Adele` → BeanOutputConverter (strongly-typed DTO)
- `GET /parser/entity?artist=Adele` → Native `.entity()` conversion
- `GET /parser/response-entity?artist=Adele` → Metadata + entity
- `GET /parser/stream?artist=Adele` → Server-Sent Events streaming
- `GET /metadata` → Custom metadata on prompts
- `GET /metadata/default` → Default context with metadata
- `GET /metadata/custom-logger-advisor` → Custom advisor implementation
- `GET /memory?question=...` → Simple in-memory conversation
- `GET /memory/{user}/ask?question=...` → Per-user in-memory history
- `GET /memory/conversations-ids` → List all conversation IDs
- `GET /memory/conversations/{id}` → Retrieve conversation history
- `GET /multi-modal` → Image analysis (PNG)

**spring-ai-redis-chat-memory endpoints:**
- `GET /memory?question=...` → Redis-backed persistent chat (TTL 24h)

**spring-ai-jdbc-chat-memory endpoints:**
- `GET /{username}?question=...` → JDBC-backed chat with PostgreSQL persistence
- `GET /{username}/conversations` → View conversation history for user
- `GET /conversations` → List all conversation IDs (usernames)

**HTTP Client:** `generated-http-requests.http` in each module

### Dependencies

**spring-ai-ollama:**
- Spring Web (auto-configures Tomcat)
- spring-ai-starter-model-ollama (includes Ollama chat via BOM)
- JUnit 5 + Spring Boot Test

**spring-ai-redis-chat-memory:**
- Spring Web
- Spring Data Redis
- spring-ai-starter-model-ollama
- Lettuce Redis client

## Code Patterns & Conventions

### ChatClient Pattern Quick Reference

| Use Case            | Method                            | Return Type                       | Best For                  | Example                |
|---------------------|-----------------------------------|-----------------------------------|---------------------------|------------------------|
| **Simple Q&A**      | `.call().chatClientResponse()`    | `ChatClientResponse`              | Basic text responses      | Get joke from LLM      |
| **Templates**       | `.call().chatResponse()`          | `ChatResponse`                    | Parameterized prompts     | Genre-based queries    |
| **Structured Data** | `.call().entity(typeRef)`         | `T`                               | Type-safe conversion      | List/Map/DTO responses |
| **With Metadata**   | `.call().responseEntity(typeRef)` | `ResponseEntity<ChatResponse, T>` | Token counting, debugging | Response analysis      |
| **Real-time**       | `.stream().content()`             | `Flux<String>`                    | Long responses, streaming | Chat responses         |
| **Memory**          | `.advisors(advisor)`              | `String`                          | Multi-turn conversation   | Chat history context   |

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

**Pattern 4: Output Conversion (OutputParserController)**
```java
// Using ListOutputConverter as example
ListOutputConverter converter = new ListOutputConverter();
Prompt prompt = new PromptTemplate(resource)
  .create(Map.of("artist", artist, "format", converter.getFormat()));
String content = chatClient.prompt(prompt).call().content();
List<String> result = converter.convert(content);
```

### Prompt Template Files (.st format)

StringTemplate files in `src/main/resources/prompts/` support variable interpolation:
- `youtube-prompt.st` - Used for `/prompts/popular` endpoint with `{genre}` parameter
- `system-prompt.st` - Used for `/prompts/instruct` endpoint as system context
- `song-text-prompt.st` - Plain text song list for `/parser` endpoint
- `song-structured-prompt.st` - Structured output format with `{format}` placeholder for converters

Load templates via `@Value("classpath:prompts/filename.st")` and pass to `PromptTemplate`.

### Response Object Handling

**ChatClientResponse** (ChatController):
- Direct response wrapper with `.toString()` method
- Contains content and metadata
- Use when you need simple string output

**ChatResponse** (PromptController, OutputParserController):
- Returns list of results via `.getResults()`
- Extract text: `response.getResults().getFirst().getOutput().getText()`
- Access metadata: `response.getMetadata()`
- `.content()` shorthand available: `chatClient.prompt(prompt).call().content()`
- Use for complex extraction or when you need result metadata

### Output Converters (OutputParserController)

Three converter types available:

1. **ListOutputConverter** → `List<String>`
   - Use for: Simple lists of items
   - Example: Song titles, article headlines

2. **MapOutputConverter** → `Map<String, Object>`
   - Use for: Key-value pairs with mixed types
   - Example: Ranked lists (key=rank, value=description)

3. **BeanOutputConverter<T>** → `T` (Generic POJO or Record)
   - Use for: Strongly-typed data structures
   - Example: `List<SongDTO>` with multiple fields
   - Requires: `new ParameterizedTypeReference<Type>() {}`

**Modern Patterns (V1.1+):**

1. **Native `.entity()` method** → `T`
   - Use: Direct type conversion without manual converters
   - Cleaner syntax: `chatClient.prompt(prompt).call().entity(typeRef)`

2. **`.responseEntity()` method** → `ResponseEntity<ChatResponse, T>`
   - Use: Get both metadata and parsed data
   - Access token counts: `response.chatResponse().getMetadata()`

3. **`.stream()` method** → `Flux<String>`
   - Use: Server-Sent Events (SSE) streaming
   - Non-blocking, token-by-token output
   - Best for: Long-form content, real-time UI updates

**Converter Pattern:**
1. Create converter: `new XxxOutputConverter()` or `new BeanOutputConverter<>(typeRef)`
2. Get format: `converter.getFormat()` → injected into prompt
3. Parse result: `converter.convert(chatClient.prompt(...).call().content())`

### Memory Management Patterns (V1.3)

**Pattern 1: Simple In-Memory Chat (ChatMemoryController)**
```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .maxMessages(5)
    .build();
chatClient.prompt()
    .user(question)
    .advisors(MessageChatMemoryAdvisor.builder(memory).build())
    .call()
    .content();
```

**Pattern 2: Per-User In-Memory Chat**
```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(inMemoryChatMemoryRepository)
    .build();
PromptChatMemoryAdvisor advisor = PromptChatMemoryAdvisor.builder(memory).build();
chatClient.prompt()
    .user(question)
    .advisors(advisor)
    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, userId))
    .call()
    .content();
```

**Pattern 3: Redis-Backed Persistent Chat (RedisChatMemoryController)**
```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(redisChatMemoryRepository)  // Injected bean
    .build();
chatClient.prompt()
    .user(question)
    .advisors(PromptChatMemoryAdvisor.builder(memory).build())
    .call()
    .content();
```

**Pattern 4: JDBC-Backed Persistent Chat (JdbcChatMemoryController)**
```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(jdbcChatMemoryRepository)  // Auto-configured, PostgreSQL-backed
    .build();
chatClient.prompt()
    .user(question)
    .advisors(PromptChatMemoryAdvisor.builder(memory).build())
    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, username))  // Per-user isolation
    .call()
    .content();
```

**Key Differences:**
- **In-Memory:** Lost on app restart, single-instance only, fastest
- **Redis:** TTL-based (24h default), multi-instance, high performance, optional
- **JDBC:** Permanent storage, full SQL queries, ACID transactions, enterprise-grade, optional

### Metadata & Advisor Patterns (V1.2)

**Custom Metadata on Prompts:**
```java
chatClient.prompt()
    .system(spec -> spec.text("You are helpful")
        .metadata("version", "1.0"))
    .user(spec -> spec.text("Hello")
        .metadata("userId", "user123"))
    .call()
    .chatResponse();
```

**Custom SimpleLoggerAdvisor:**
```java
SimpleLoggerAdvisor advisor = new SimpleLoggerAdvisor(
    request -> "Custom request: " + request.prompt(),
    response -> "Custom response: " + response.getResult(),
    0  // Priority (0 = highest)
);
chatClient.prompt("Hello")
    .advisors(advisor)
    .call()
    .content();
```

**Use Cases:** Request tracing, metrics collection, content filtering, transformation

### Multimodal Patterns (V1.3)

**Image Analysis with Text:**
```java
chatClient.prompt()
    .system("You analyze images")
    .user(spec -> spec
        .text("Analyze this image")
        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/images/test.png")))
    .call()
    .content();
```

**Requirements:**
- Multimodal-capable model (e.g., `gemma4:e2b`)
- Image files in `src/main/resources/images/`
- Supported formats: PNG, JPEG, GIF, WebP

### Logging
- SLF4J with loggers created via `LoggerFactory.getLogger()`
- All controllers log model responses at INFO level for debugging
- Framework: Spring AI logs available at DEBUG level
- See ChatController and PromptController for examples
- **Enable debug logging:** Add to `application.yaml`:
  ```yaml
  logging:
    level:
      org.springframework.ai: DEBUG
      com.moh.yehia: DEBUG
  ```

### Dependency Injection
- Constructor injection (all controllers receive `ChatClient.Builder`)
- Spring auto-configuration handles ChatClient bean creation
- No explicit @Bean definitions needed for AI components
- Resource injection via `@Value("classpath:...")`
- **Pattern:** All beans created through Spring Framework without manual configuration

## Version & Release Information

### Current Versions
- **Spring Boot:** 4.0.5 (stable)
- **Spring AI:** 2.0.0-M4 (milestone, pre-release)
- **Java:** 21 (LTS)
- **Ollama:** Latest (self-managed)
- **Redis:** 7.0+ (for redis-chat-memory module)

### Release History
- **V1.3:** Chat memory, per-user conversations, multimodal image analysis, metadata advisors
- **V1.2:** Metadata management, request/response tracing, advisor patterns
- **V1.1:** Native entity conversion, response metadata, streaming support

### API Stability Notes
- ⚠️ Spring AI 2.0.0-M4 is pre-release, API may change
- ✅ Output converter API stable since V1.1
- ✅ Memory advisor API stable since V1.3
- ✅ Ollama integration stable across versions
- **Recommendation:** Monitor Spring AI docs for updates before upgrading

## Extension Points

### spring-ai-ollama
1. **Add new endpoints:** Create methods in controllers with different prompts/patterns
2. **Change models:** Update `spring.ai.ollama.chat.model` in `application.yaml` (gemma4:e2b, llama3.2:3b, llama3.1:8b)
3. **Create new prompt templates:** Add `.st` files in `src/main/resources/prompts/` and inject with `@Value`
4. **Add custom converters:** Extend `OutputConverter<T>` interface for new output types
5. **Add streaming endpoints:** Use `.stream().content()` for real-time responses
6. **Add advisor chains:** Combine multiple advisors for complex processing
7. **Add image processing:** Use `MultiModalController` as template for other media types

### spring-ai-redis-chat-memory
1. **Add conversation export:** Save conversations to database
2. **Add conversation search:** Query past conversations by content
3. **Add TTL customization:** Adjust `Duration.ofHours(24)` in configuration
4. **Add analytics:** Track conversation metrics
5. **Add multimodal:** Use same patterns from spring-ai-ollama MultiModalController

### spring-ai-jdbc-chat-memory
1. **Add conversation search:** Full-text search with PostgreSQL
2. **Add conversation export:** Generate PDF/CSV reports from database
3. **Add conversation analytics:** Create dashboards with conversation statistics
4. **Add user authentication:** Store user IDs instead of usernames
5. **Add conversation tagging:** Add tag column to database
6. **Add sentiment analysis:** Analyze conversation sentiment with PostgreSQL UDFs
7. **Add rate limiting:** Query-based rate limiting per user
8. **Add conversation archival:** Move old conversations to archive table
9. **Add custom indexes:** Optimize query performance for large datasets
10. **Add data retention policies:** Automatic cleanup of old conversations

## Module Selection Guide

### When to Use spring-ai-ollama
- **Learning Spring AI patterns** - Most comprehensive examples (6 controllers)
- **Prototyping AI features** - Quick development with immediate feedback
- **Image/document analysis** - Multimodal support built-in
- **Complex output parsing** - All converter patterns demonstrated
- **Single-instance deployments** - No distributed state needed
- **Advanced patterns** - Metadata, advisors, streaming, memory

### When to Use spring-ai-redis-chat-memory
- **Multi-user chat applications** - Distributed conversation management
- **Production deployments** - Persistent storage with configurable TTL (24h default)
- **Microservices architecture** - Shared memory across instances
- **Long-lived conversations** - Conversations survive app restarts
- **High performance needed** - In-memory data store for speed
- **Horizontal scaling** - Stateless application design
- **Simple persistence** - No complex queries needed

### When to Use spring-ai-jdbc-chat-memory
- **Enterprise applications** - Full relational database power
- **Complex queries required** - Full SQL for analytics and reporting
- **Permanent storage** - No TTL, conversations stored indefinitely
- **ACID transactions** - Guaranteed data consistency
- **Multi-user with auditing** - Complete conversation audit trail
- **Advanced analytics** - Dashboards and reporting from conversation data
- **Large-scale deployments** - Unlimited storage with disk-bound scaling
- **Compliance requirements** - Full data control and retention policies

### Combined Usage Pattern
**Recommended for Production:**
1. Use spring-ai-ollama for core AI logic (all modules depend on this)
2. Choose memory backend based on requirements:
   - Fast, distributed, TTL-based → spring-ai-redis-chat-memory
   - Enterprise, permanent, SQL queries → spring-ai-jdbc-chat-memory
3. Extend with custom converters and advisors from spring-ai-ollama
4. Deploy as microservice with load balancing

## Critical Developer Knowledge

### Prerequisites
- Ollama service must be running on `localhost:11434` before starting app
- Redis 7.0+ required for spring-ai-redis-chat-memory (NOT required for spring-ai-ollama alone)
- Sufficient models installed: `ollama list`
- Verify services: `curl http://localhost:11434/api/tags && redis-cli ping`

### Spring AI Stability
- Using 2.0.0-M4 (milestone release, not stable)
- API may change; refer to official Spring AI docs for updates
- ChatClient/ChatResponse object handling differs between endpoints
- Output converter API is stable and production-ready
- Memory advisor patterns stable since V1.3

### Performance Considerations
- First request is slow (model loads into memory)
- gemma4:e2b requires more resources than llama3.2:3b
- Streaming responses better for long content
- Redis adds latency vs. in-memory; use for multi-instance scenarios
- Token counting useful for cost analysis

## File Reference

### spring-ai-ollama Controllers
- `src/main/java/com/moh/yehia/ollama/ChatController.java` - Simple joke endpoint
- `src/main/java/com/moh/yehia/ollama/PromptController.java` - Template & system message patterns
- `src/main/java/com/moh/yehia/ollama/OutputParserController.java` - Output conversion (6 patterns)
- `src/main/java/com/moh/yehia/ollama/MetadataController.java` - Metadata & advisor patterns
- `src/main/java/com/moh/yehia/ollama/ChatMemoryController.java` - In-memory conversation management
- `src/main/java/com/moh/yehia/ollama/MultiModalController.java` - Image analysis

### spring-ai-redis-chat-memory Controllers
- `src/main/java/com/moh/yehia/chat/memory/RedisChatMemoryController.java` - Redis-backed chat (1 endpoint)

### spring-ai-jdbc-chat-memory Controllers
- `src/main/java/com/moh/yehia/chat/memory/ChatMemoryController.java` - JDBC-backed chat (3 endpoints)

### Data Objects
- `src/main/java/com/moh/yehia/ollama/SongDTO.java` - Record for structured responses
- `src/main/java/com/moh/yehia/chat/memory/ChatMemoryConfig.java` - Redis bean configuration

### Prompt Templates
- `src/main/resources/prompts/youtube-prompt.st` - Template with `{genre}` parameter
- `src/main/resources/prompts/system-prompt.st` - System message template
- `src/main/resources/prompts/song-text-prompt.st` - Plain text response format
- `src/main/resources/prompts/song-structured-prompt.st` - Structured format with `{format}` placeholder

### Configuration Files
- `spring-ai-ollama/src/main/resources/application.yaml` - Ollama config, port 8083
- `spring-ai-redis-chat-memory/src/main/resources/application.yaml` - Redis & Ollama config, port 8084
- `spring-ai-jdbc-chat-memory/src/main/resources/application.yaml` - PostgreSQL & Ollama config, port 8085

### Build & Test
- `spring-ai-ollama/pom.xml` - Maven configuration
- `spring-ai-redis-chat-memory/pom.xml` - Maven configuration
- `spring-ai-jdbc-chat-memory/pom.xml` - Maven configuration
- `spring-ai-ollama/src/test/java/.../SpringAiOllamaApplicationTests.java` - Context load test only
- `spring-ai-ollama/generated-http-requests.http` - HTTP client test file

## Common Pitfalls

### Ollama Issues
| Problem                 | Verification                                                | Solution                                             |
|-------------------------|-------------------------------------------------------------|------------------------------------------------------|
| **Ollama not running**  | `curl http://localhost:11434/api/tags` → Connection refused | Start Ollama: `ollama serve`                         |
| **Wrong model name**    | Typos in `application.yaml`                                 | Run `ollama list` to verify installed models         |
| **Model not installed** | Model not in output of `ollama list`                        | Install: `ollama pull gemma4:e2b`                    |
| **Silent failures**     | Requests fail but app started normally                      | Check logs with `DEBUG` level, verify Ollama running |
| **Out of memory**       | First request or large models timeout                       | Use lighter model (llama3.2:3b), increase RAM        |

### Redis Issues (spring-ai-redis-chat-memory only)
| Problem                 | Verification                                | Solution                                                    |
|-------------------------|---------------------------------------------|-------------------------------------------------------------|
| **Redis not running**   | `redis-cli ping` → Connection refused       | Start Redis: `redis-server`                                 |
| **Wrong Redis version** | `redis-cli INFO server` shows v6 or earlier | Upgrade to Redis 7.0+: `brew upgrade redis`                 |
| **Memory full**         | Conversations disappear randomly            | Check `redis-cli INFO memory`, increase Redis max memory    |
| **Network issues**      | Connection timeout errors                   | Verify `localhost:6379` is accessible, check firewall       |
| **Keys not persisting** | Conversations lost after TTL                | Keys expire by design (24h default), check ChatMemoryConfig |

### Port Conflicts
| Port      | Service                     | Solution                                        |
|-----------|-----------------------------|-------------------------------------------------|
| **8083**  | spring-ai-ollama            | Change `server.port` in `application.yaml`      |
| **8084**  | spring-ai-redis-chat-memory | Change `server.port` in `application.yaml`      |
| **11434** | Ollama                      | Change Ollama port: `ollama serve --port 11435` |
| **6379**  | Redis                       | Change Redis port: `redis-server --port 6380`   |

### Code Issues & Solutions
| Issue                                   | Root Cause                        | Solution                                                                    |
|-----------------------------------------|-----------------------------------|-----------------------------------------------------------------------------|
| **Prompt template not found**           | `.st` file missing from resources | Verify file exists in `src/main/resources/prompts/` before build            |
| **Variable interpolation failures**     | Key mismatch in `Map.of()`        | Ensure keys match template placeholders exactly (e.g., `{genre}` ← `genre`) |
| **Output converter assertion failures** | LLM response format unexpected    | Manually test endpoint without converter, inspect actual response           |
| **Entity conversion fails (JSON)**      | DTO field mismatch                | Add `@JsonProperty` annotations or adjust DTO fields to match LLM output    |
| **Streaming endpoint silent failure**   | Request timeout too short         | Set `spring.mvc.async.request-timeout: 30000` in `application.yaml`         |
| **Metadata not propagating**            | Advisor not configured            | Ensure advisor is added: `.advisors(new SimpleLoggerAdvisor())`             |

### Testing Issues
| Issue                                | Cause                                | Resolution                                                              |
|--------------------------------------|--------------------------------------|-------------------------------------------------------------------------|
| **Tests are minimal**                | Only context loading tested          | Add integration tests with `@MockBean` for ChatClient                   |
| **Assertion errors in controllers**  | `assert` statements disabled         | Enable assertions in IDE: Run → Edit Configurations → VM options: `-ea` |
| **Memory tests fail**                | Repository state shared across tests | Use `@BeforeEach` to create new repository per test                     |
| **Port already in use during tests** | Previous test didn't release port    | Use random ports: `@SpringBootTest(webEnvironment = RANDOM_PORT)`       |

