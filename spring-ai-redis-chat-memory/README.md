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

This module showcases two distinct chat memory implementations:

1. **In-Memory Chat** - Lightweight, session-scoped conversation tracking using Spring AI's built-in in-memory repository
2. **Redis-Based Chat** - Persistent, distributed conversation management ideal for production multi-instance deployments

Perfect for building chatbots, conversational AI systems, and interactive applications that need to remember conversation context across requests.

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
┌──────────────────────────────────────────────────────┐
│        Spring Boot Application (Port 8084)           │
│                                                      │
│  ┌─────────────────────┐  ┌──────────────────────┐  │
│  │ ChatController      │  │ RedisChatMemory      │  │
│  │ (/chat - in-memory) │  │ Controller           │  │
│  │                     │  │ (/memory - Redis)    │  │
│  └──────────┬──────────┘  └──────────┬───────────┘  │
│             │                        │              │
│  ┌──────────▼────────────────────────▼────────────┐ │
│  │     Spring AI ChatClient                       │ │
│  │  (Auto-configured by Spring AI BOM)           │ │
│  │                                                │ │
│  │  ┌─────────────────────────────────────────┐  │ │
│  │  │ Advisors:                               │  │ │
│  │  │ • PromptChatMemoryAdvisor               │  │ │
│  │  │ • SimpleLoggerAdvisor                   │  │ │
│  │  └─────────────────────────────────────────┘  │ │
│  └──────────┬─────────────────────────┬──────────┘ │
└─────────────┼─────────────────────────┼────────────┘
              │                         │
      ┌───────▼────────┐       ┌────────▼──────────┐
      │  Ollama        │       │   Redis          │
      │  localhost     │       │   localhost      │
      │  :11434        │       │   :6379          │
      │  (llama3.2:3b) │       │  (Persistence)   │
      └────────────────┘       └──────────────────┘
```

### Data Flow

**In-Memory Chat Flow:**
1. Request: `GET /chat?question=Hello`
2. ChatController creates InMemoryChatMemoryRepository
3. PromptChatMemoryAdvisor maintains conversation context in-process
4. ChatClient calls Ollama with context
5. Response returned with conversation stored in-memory

**Redis Chat Flow:**
1. Request: `GET /memory?question=Hello`
2. RedisChatMemoryController injects pre-configured RedisChatMemoryRepository
3. PromptChatMemoryAdvisor retrieves prior messages from Redis
4. ChatClient calls Ollama with full conversation context
5. Response stored in Redis with 24-hour TTL
6. Conversation accessible across application restarts

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
# In-memory chat (simple, session-scoped)
curl "http://localhost:8084/chat?question=What%20is%20Spring%20AI?"

# Redis-backed chat (persistent, distributed)
curl "http://localhost:8084/memory?question=What%20is%20Spring%20AI?"
```

## API Endpoints

### 1. In-Memory Chat Endpoint

**Request:**
```http
GET /chat?question=Tell me a joke
```

**Description:** Simple Q&A with in-process memory. Context is lost on application restart.

**Response:**
```
Why did the programmer quit his job? Because he didn't get arrays. 😄
```

**Implementation:**
- Uses `InMemoryChatMemoryRepository`
- Maintains conversation window in application memory
- Advisors: `PromptChatMemoryAdvisor`, `SimpleLoggerAdvisor`

### 2. Redis-Backed Chat Endpoint

**Request:**
```http
GET /memory?question=What are your capabilities?
```

**Description:** Stateful Q&A with Redis persistence. Conversation history persists across requests and application restarts.

**Response:**
```
I can help you with various tasks including answering questions, 
providing explanations, code reviews, and creative writing...
```

**Implementation:**
- Uses `RedisChatMemoryRepository` (auto-configured in `ChatMemoryConfig`)
- Stores conversation with 24-hour TTL
- Retrieves prior messages for context-aware responses
- Thread-safe, distributed conversation tracking
- Advisors: `PromptChatMemoryAdvisor`, `SimpleLoggerAdvisor`

**Configuration (ChatMemoryConfig):**
```java
RedisChatMemoryRepository.builder()
    .jedisClient(jedisClient)           // Redis connection
    .indexName("spring-ai-chat-memory") // Logical namespace
    .keyPrefix("my-chat:")              // Redis key prefix
    .timeToLive(Duration.ofHours(24))   // TTL for messages
    .build()
```

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
│   │   │   ├── ChatController.java                       # In-memory endpoint
│   │   │   ├── RedisChatMemoryController.java           # Redis endpoint
│   │   │   └── ChatMemoryConfig.java                    # Redis bean config
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
| `ChatController.java`                     | REST endpoint for in-memory chat (`/chat`) using InMemoryChatMemoryRepository     |
| `RedisChatMemoryController.java`          | REST endpoint for Redis chat (`/memory`) using RedisChatMemoryRepository          |
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

### Pattern 1: Basic In-Memory Chat

```java
@GetMapping("/chat")
public String ask(@RequestParam String question) {
    InMemoryChatMemoryRepository repo = new InMemoryChatMemoryRepository();
    MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(repo)
            .build();
    
    return chatClient.prompt()
            .user(question)
            .advisors(PromptChatMemoryAdvisor.builder(memory).build())
            .call()
            .content();
}
```

### Pattern 2: Redis-Backed Persistent Chat

```java
@GetMapping("/memory")
public String chat(@RequestParam String question) {
    MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(redisChatMemoryRepository)  // Injected bean
            .build();
    
    return chatClient.prompt()
            .user(question)
            .advisors(PromptChatMemoryAdvisor.builder(memory).build())
            .call()
            .content();
}
```

### Pattern 3: Custom Redis Configuration

```java
@Configuration
public class ChatMemoryConfig {
    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository(
            @Value("${spring.ai.chat.memory.redis.host}") String host,
            @Value("${spring.ai.chat.memory.redis.port}") int port) {
        
        JedisPooled jedis = new JedisPooled(host, port);
        
        return RedisChatMemoryRepository.builder()
                .jedisClient(jedis)
                .indexName("my-chat-index")
                .keyPrefix("chat:")
                .timeToLive(Duration.ofHours(24))
                .build();
    }
}
```

## Troubleshooting

| Issue                                 | Solution                                                                                           |
|---------------------------------------|----------------------------------------------------------------------------------------------------|
| **Port 8084 already in use**          | Change `server.port` in application.yaml or use `-Dspring-boot.run.arguments="--server.port=9084"` |
| **Ollama connection failed**          | Verify Ollama runs on localhost:11434 with `curl http://localhost:11434/api/tags`                  |
| **Redis connection refused**          | Ensure Redis is running on localhost:6379; adjust `spring.ai.chat.memory.redis.host/port`          |
| **Model not found**                   | Run `ollama pull llama3.2:3b` before starting application                                          |
| **Conversation not persisting**       | Check Redis TTL and key prefixes in `ChatMemoryConfig`; verify Redis connectivity                  |
| **Build fails: dependency not found** | Update Spring AI version in pom.xml or clear Maven cache: `mvn clean`                              |

## Performance Considerations

- **In-Memory Chat**: Faster, zero external dependencies, limited to single instance
- **Redis Chat**: Slightly higher latency, supports distributed/clustered deployments, survives restarts

Choose based on your requirements:
- Development/Testing → Use in-memory
- Production/Multi-instance → Use Redis-backed

## Extension Points

1. **Change LLM Model**: Update `spring.ai.ollama.chat.model` in `application.yaml`
2. **Modify Redis TTL**: Adjust `Duration.ofHours(24)` in `ChatMemoryConfig`
3. **Add New Endpoints**: Create methods in controller with different memory strategies
4. **Custom Memory Window**: Adjust `MessageWindowChatMemory` builder configuration
5. **Add Request Logging**: Extend `SimpleLoggerAdvisor` or create custom advisor

## References

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [Spring AI Chat Memory](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_chat_memory)
- [Ollama GitHub](https://github.com/ollama/ollama)
- [Redis Documentation](https://redis.io/documentation)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
