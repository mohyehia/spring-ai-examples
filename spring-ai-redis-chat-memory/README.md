# spring-ai-redis-chat-memory

A Spring Boot 4.0.5 demonstration of **AI-powered chat with persistent memory using Redis**. This module integrates [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/) for local LLM orchestration, leveraging **Redis as a distributed chat memory repository** for multi-user, stateful conversations.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Technologies](#key-technologies)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Development](#development)
- [Project Structure](#project-structure)

## Overview

This module demonstrates **Redis-backed persistent chat memory** for multi-user conversational AI systems. Unlike simple in-memory implementations, Redis ensures conversations survive application restarts and scale across distributed instances.

**Key Use Cases:**
- Multi-user chat applications with persistent history
- Production deployments needing conversation durability
- Microservices with shared conversation state
- Stateless horizontally-scaled applications
- Chat systems with conversation auditing requirements

Perfect for building enterprise chatbots, customer support systems, and conversational AI that require persistent, distributed conversation management.

## Key Technologies

| Technology      | Version  | Purpose                                  |
|-----------------|----------|------------------------------------------|
| **Java**        | 21       | Language runtime                         |
| **Spring Boot** | 4.0.5    | Application framework                    |
| **Spring AI**   | 2.0.0-M4 | AI abstraction layer with Ollama support |
| **Ollama**      | Latest   | Local LLM orchestration (llama3.2:3b)    |
| **Redis**       | Latest   | Distributed chat memory persistence      |
| **Maven**       | Latest   | Build & dependency management            |

## Architecture

### Component Diagram

```
┌────────────────────────────────────────────────────────┐
│        Spring Boot Application (Port 8084)             │
│                                                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │  RedisChatMemoryController                      │   │
│  │  (/memory/{username}/chat)                      │   │
│  │  (/memory/{username}/conversations)             │   │
│  │  (/memory/conversations)                        │   │
│  └─────────────────────────────────────────────────┘   │
│                        │                               │
│  ┌────────────────────▼─────────────────────────────┐  │
│  │     Spring AI ChatClient                         │  │
│  │  (Auto-configured by Spring AI BOM)              │  │
│  │                                                  │  │
│  │  Advisors:                                       │  │
│  │  • PromptChatMemoryAdvisor (history context)     │  │
│  │  • SimpleLoggerAdvisor (request/response logs)   │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼────────────────────────────────┘
                        │
            ┌───────────┴──────────┐
            │                      │
      ┌─────▼────────┐       ┌─────▼──────────┐
      │  Ollama      │       │  Redis         │
      │  localhost   │       │  localhost     │
      │  :11434      │       │  :6379         │
      │ (llama3.2)   │       │ (Memory Store) │
      └──────────────┘       └────────────────┘
```

### Data Flow

**User-Scoped Chat Flow:**
1. Request: `GET /memory/{username}/chat?question=What%20is%20Spring%20AI?`
2. RedisChatMemoryController receives username and question
3. PromptChatMemoryAdvisor queries Redis for previous messages with conversation ID = username
4. ChatClient calls Ollama with full conversation context
5. Ollama generates response based on context
6. Response and new message stored in Redis with 24-hour TTL
7. Response returned to user
8. Conversation accessible across requests and restarts

**Conversation Retrieval Flow:**
1. Request: `GET /memory/{username}/conversations`
2. Controller queries RedisChatMemoryRepository.findByConversationId(username)
3. Redis returns all Message objects for that conversation ID
4. Messages converted to text and returned as JSON array
5. Complete conversation history available for replay/audit

**Conversation Discovery Flow:**
1. Request: `GET /memory/conversations`
2. Controller queries RedisChatMemoryRepository.findConversationIds()
3. Redis returns all active conversation IDs
4. Useful for monitoring all active users/conversations

## Quick Start

### Prerequisites

1. **Java 21+** installed
2. **Maven 3.8+** installed
3. **Ollama** running on localhost:11434
4. **Redis 7.0+** running on localhost:6379

> ⚠️ **Important**: Redis version **7.0 or higher** is required for the redis-chat-memory module to work as expected. Earlier versions may lack the necessary features for optimal chat memory storage and retrieval.

### Setup Steps

```bash
# 1. Pull the required Ollama model
ollama pull llama3.2:3b

# 2. Navigate to module directory
cd spring-ai-redis-chat-memory

# 3. Build the application
mvn clean install

# 4. Run the application
mvn spring-boot:run
```

Application starts on **http://localhost:8084**

### Test Endpoints

```bash
# Chat as user 'alice' - first message
curl "http://localhost:8084/memory/alice/chat?question=What%20is%20Spring%20AI?"

# Chat as user 'alice' - second message (will have context from first)
curl "http://localhost:8084/memory/alice/chat?question=Tell%20me%20more"

# Chat as different user 'bob' - separate conversation
curl "http://localhost:8084/memory/bob/chat?question=Hello%20there"

# View alice's conversation history
curl "http://localhost:8084/memory/alice/conversations"

# View bob's conversation history
curl "http://localhost:8084/memory/bob/conversations"

# View all active conversation IDs
curl "http://localhost:8084/memory/conversations"
```

## API Endpoints

### 1. Chat with User-Scoped Memory

**Request:**
```http
GET /memory/{username}/chat?question=What%20is%20Spring%20AI?
```

**Example:**
```bash
curl "http://localhost:8084/memory/alice/chat?question=What%20is%20Spring%20AI?"
curl "http://localhost:8084/memory/bob/chat?question=Tell%20me%20a%20joke"
```

**Description:** Multi-user conversation with persistent Redis-backed memory. Each user's conversation history is maintained separately using their username as the conversation ID.

**Response:**
```
Spring AI is a Spring Framework project that provides abstractions for 
integrating with AI models like LLMs (Large Language Models). It offers a 
unified API for working with various AI providers...
```

**Key Features:**
- ✅ **Per-user conversations** - Each username has isolated conversation history
- ✅ **Persistent storage** - Conversations survive application restarts
- ✅ **Context awareness** - LLM has access to conversation history
- ✅ **Auto-configured** - Uses `RedisChatMemoryRepository` injected by Spring
- ✅ **Advisors enabled** - `PromptChatMemoryAdvisor` + `SimpleLoggerAdvisor`

**Implementation Details:**
- Endpoint: `GET /memory/{username}/chat`
- Path Variable: `username` - User identifier (e.g., "alice", "bob", "user123")
- Query Parameter: `question` - The user's question/input
- Memory Type: Redis-backed with conversation ID = username
- Return Type: Plain text response from Ollama

### 2. View User Conversations

**Request:**
```http
GET /memory/{username}/conversations
```

**Example:**
```bash
curl "http://localhost:8084/memory/alice/conversations"
```

**Description:** Retrieve the complete conversation history for a specific user.

**Response:**
```json
[
  "What is Spring AI?",
  "Spring AI is a Spring Framework project that provides abstractions...",
  "Tell me more about LLMs",
  "Large Language Models (LLMs) are neural networks trained on vast amounts of text...",
  "How do I use Spring AI with Ollama?"
]
```

**Key Features:**
- ✅ **Full history** - All messages in the conversation thread
- ✅ **Message order** - Chronological order of conversation
- ✅ **Plain text** - Text content of each message
- ✅ **No metadata** - Returns only message content

**Implementation Details:**
- Endpoint: `GET /memory/{username}/conversations`
- Path Variable: `username` - User identifier
- Return Type: List<String> - Array of message texts
- Retrieves from Redis using conversation ID

### 3. View All Conversation IDs

**Request:**
```http
GET /memory/conversations
```

**Example:**
```bash
curl "http://localhost:8084/memory/conversations"
```

**Description:** List all active conversation IDs in the Redis memory store. Useful for monitoring, debugging, or administration.

**Response:**
```json
[
  "alice",
  "bob",
  "charlie",
  "user123"
]
```

**Key Features:**
- ✅ **System overview** - See all active users/conversations
- ✅ **No filtering** - Lists all conversation IDs
- ✅ **Administrative use** - Monitor system activity
- ✅ **Debugging support** - Verify conversations are stored

**Implementation Details:**
- Endpoint: `GET /memory/conversations`
- No path variables or query parameters
- Return Type: List<String> - All conversation IDs
- Global view across all users

## Configuration

### Application YAML (`application.yaml`)

```yaml
spring:
  application:
    name: spring-ai-redis-chat-memory
  
  # AI Configuration
  ai:
    ollama:
      chat:
        model: "llama3.2:3b"  # Model to use (configurable)
    
    # Redis Chat Memory Configuration
    chat:
      memory:
        redis:
          host: localhost      # Redis server host
          port: 6379          # Redis server port
          index-name: spring-ai-chat-memory  # Namespace

server:
  port: 8084  # Distinct from main app (8083)

logging:
  level:
    root: info
    org.springframework.ai: debug              # AI framework logs
    org.springframework.ai.chat.client.advisor: debug  # Memory advisor logs
```

### Environment Variables (Override YAML)

```bash
# Override Redis connection
export SPRING_AI_CHAT_MEMORY_REDIS_HOST=redis-server.example.com
export SPRING_AI_CHAT_MEMORY_REDIS_PORT=6380

# Override Ollama model
export SPRING_AI_OLLAMA_CHAT_MODEL=llama3.1:8b

# Override server port
export SERVER_PORT=9084
```

## Development

### Building

```bash
# Full build with tests
mvn clean install

# Fast build (skip tests)
mvn clean install -DskipTests

# Build specific module from root
mvn -pl spring-ai-redis-chat-memory clean install
```

### Running

```bash
# Development mode with auto-reload
mvn spring-boot:run

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9084"
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SpringAiRedisChatMemoryApplicationTests
```

### Debugging

Enable detailed logging:

```yaml
logging:
  level:
    com.moh.yehia.chat.memory: debug
    org.springframework.ai: debug
    org.springframework.data.redis: debug
```

## Project Structure

```
spring-ai-redis-chat-memory/
├── src/
│   ├── main/
│   │   ├── java/com/moh/yehia/chat/memory/
│   │   │   ├── SpringAiRedisChatMemoryApplication.java  # Entry point
│   │   │   ├── RedisChatMemoryController.java           # REST endpoints (/memory/*)
│   │   │   └── ChatMemoryConfig.java                    # Redis bean configuration
│   │   └── resources/
│   │       └── application.yaml                         # Spring configuration
│   └── test/
│       └── java/...
├── pom.xml                                               # Maven configuration
├── README.md                                             # This file
└── HELP.md                                              # Spring Boot help

```

### Key Files Explained

| File                                      | Purpose                                                                           |
|-------------------------------------------|-----------------------------------------------------------------------------------|
| `SpringAiRedisChatMemoryApplication.java` | Spring Boot entry point with `@SpringBootApplication` and `@EnableCaching`        |
| `RedisChatMemoryController.java`          | REST endpoints for user-scoped chat, conversation history, and conversation list  |
| `ChatMemoryConfig.java`                   | Spring Configuration bean for RedisChatMemoryRepository with JedisPooled client   |
| `application.yaml`                        | Configuration for Ollama model, Redis connection, server port, and logging levels |

## Dependencies

### Core Dependencies

- **spring-boot-starter-webmvc**: Web application framework
- **spring-ai-starter-model-ollama**: Ollama chat model integration
- **spring-ai-starter-model-chat-memory-repository-redis**: Redis chat memory (Spring AI BOM managed)
- **spring-boot-starter-data-redis**: Spring Data Redis framework
- **jedis**: Redis Java client (transitive via Spring Data Redis)

### Dependency Management

Spring AI BOM (Bill of Materials) automatically manages version compatibility:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>2.0.0-M4</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Common Patterns

### Pattern 1: User-Scoped Chat with Redis Memory

```java
@GetMapping("/{username}/chat")
public String chat(@PathVariable String username, @RequestParam String question) {
    return chatClient.prompt()
            .user(question)
            .advisors(PromptChatMemoryAdvisor.builder(
                MessageWindowChatMemory.builder()
                    .chatMemoryRepository(redisChatMemoryRepository)
                    .build()
            ).build())
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
            .call()
            .content();
}
```

**Key Points:**
- `{username}` path variable isolates conversations per user
- `ChatMemory.CONVERSATION_ID` parameter = username
- `redisChatMemoryRepository` maintains persistent state
- `PromptChatMemoryAdvisor` injects history into prompt
- `SimpleLoggerAdvisor` logs all requests/responses

### Pattern 2: Retrieve User Conversation History

```java
@GetMapping("/{username}/conversations")
public List<String> viewConversations(@PathVariable String username) {
    return redisChatMemoryRepository.findByConversationId(username)
            .stream().map(Message::getText).toList();
}
```

**Key Points:**
- Queries Redis by conversation ID (username)
- Returns all `Message` objects for that conversation
- Maps to text content for clean response
- Useful for UI display, debugging, auditing

### Pattern 3: List All Active Conversations

```java
@GetMapping("/conversations")
public List<String> viewConversationIDs() {
    return redisChatMemoryRepository.findConversationIds();
}
```

**Key Points:**
- Global view of all conversations
- Returns list of conversation IDs
- No filtering or parameters
- Useful for monitoring and administration

### Pattern 4: Custom Redis Configuration

```java
@Configuration
public class ChatMemoryConfig {
    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository(
            @Value("${spring.ai.chat.memory.redis.host:localhost}") String host,
            @Value("${spring.ai.chat.memory.redis.port:6379}") int port) {
        
        JedisPooled jedis = new JedisPooled(host, port);
        
        return RedisChatMemoryRepository.builder()
                .jedisClient(jedis)
                .indexName("spring-ai-chat-memory")
                .keyPrefix("memory:")
                .timeToLive(Duration.ofHours(24))
                .build();
    }
}
```

**Configuration Options:**
- `indexName` - Logical namespace for Redis storage
- `keyPrefix` - Prefix for all Redis keys (e.g., "memory:" → "memory:alice")
- `timeToLive` - Auto-expiration duration for conversations (24 hours by default)
- `jedisClient` - Redis connection pool

## Troubleshooting

| Issue                                 | Solution                                                                                           |
|---------------------------------------|----------------------------------------------------------------------------------------------------|
| **Port 8084 already in use**          | Change `server.port` in application.yaml or use `-Dspring-boot.run.arguments="--server.port=9084"` |
| **Ollama connection failed**          | Verify Ollama runs on localhost:11434 with `curl http://localhost:11434/api/tags`                  |
| **Redis connection refused**          | Ensure Redis 7.0+ is running on localhost:6379; adjust `spring.ai.chat.memory.redis` config        |
| **Redis version too old**             | Upgrade to Redis 7.0+: `brew upgrade redis` or update your Redis installation                      |
| **Model not found**                   | Run `ollama pull llama3.2:3b` before starting application                                          |
| **Conversations not persisting**      | Check Redis is running and accessible; verify TTL in `ChatMemoryConfig`                            |
| **No conversation history retrieved** | Ensure username path variable matches conversation ID; verify Redis keys exist                     |
| **Build fails: dependency not found** | Update Spring AI version in pom.xml or clear Maven cache: `mvn clean`                              |

### Endpoint Testing

```bash
# Test basic chat
curl "http://localhost:8084/memory/test-user/chat?question=hello"

# Test conversation retrieval  
curl "http://localhost:8084/memory/test-user/conversations"

# Test get all conversation IDs
curl "http://localhost:8084/memory/conversations"

# Verify Redis connection
redis-cli ping                          # Should return: PONG

# Check Redis keys for chat data
redis-cli KEYS "memory:*"              # Lists all memory keys
redis-cli TTL "memory:test-user:*"     # Check TTL for messages
```

## Performance Considerations

- **In-Memory Chat**: Faster, zero external dependencies, limited to single instance
- **Redis Chat**: Slightly higher latency, supports distributed/clustered deployments, survives restarts

Choose based on your requirements:
- Development/Testing → Use in-memory
- Production/Multi-instance → Use Redis-backed

## Extension Points

1. **Add conversation search** - Search conversations by content or date range
2. **Add conversation export** - Save conversations to database or file
3. **Add user profiles** - Store user metadata alongside conversations
4. **Add analytics endpoints** - Track conversation metrics, token usage, response times
5. **Add conversation cleanup** - Implement scheduled TTL or manual deletion
6. **Add multimodal support** - Extend to support image analysis (use spring-ai-ollama patterns)
7. **Add conversation tagging** - Tag conversations for organization and filtering
8. **Add conversation merging** - Combine multiple conversation threads
9. **Add rate limiting** - Prevent abuse with per-user request limits
10. **Add webhooks** - Notify external systems when conversations end or hit milestones

## References

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [Spring AI Chat Memory](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_chat_memory)
- [Ollama GitHub](https://github.com/ollama/ollama)
- [Redis Documentation](https://redis.io/documentation)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
