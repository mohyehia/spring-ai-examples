# spring-ai-jdbc-chat-memory

A Spring Boot 4.0.5 demonstration of **AI-powered chat with persistent memory using JDBC and PostgreSQL**. This module integrates [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/) for local LLM orchestration, leveraging **JDBC-backed database storage** for multi-user, stateful conversations with full relational database support.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Technologies](#key-technologies)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Development](#development)
- [Project Structure](#project-structure)
- [Common Patterns](#common-patterns)
- [Troubleshooting](#troubleshooting)
- [Performance Considerations](#performance-considerations)
- [Extension Points](#extension-points)
- [References](#references)

## Overview

JDBC-backed persistent chat memory using PostgreSQL. Perfect for enterprise-grade chatbots requiring:
- ✨ Persistent conversation history with full audit trail
- ✨ Multi-user support with per-user conversation isolation
- ✨ Horizontal scalability across instances
- ✨ Full SQL power for complex queries and reporting
- ✨ Conversations survive application restarts

**vs Redis:** JDBC provides permanent storage, full SQL querying, and ACID transactions vs Redis' configurable TTL and key-value only approach.

## Key Technologies

| Technology      | Version  | Purpose                                           |
|-----------------|----------|---------------------------------------------------|
| **Java**        | 21       | Language runtime                                  |
| **Spring Boot** | 4.0.5    | Application framework                             |
| **Spring AI**   | 2.0.0-M4 | AI abstraction layer with Ollama support          |
| **Ollama**      | Latest   | Local LLM orchestration (llama3.2:3b)             |
| **PostgreSQL**  | 12+      | Relational database for chat memory persistence   |
| **JDBC**        | Standard | Java Database Connectivity via PostgreSQL driver  |
| **Maven**       | Latest   | Build & dependency management                     |

## Architecture

### Component Diagram

```
┌───────────────────────────────────────────────────────────┐
│        Spring Boot Application (Port 8085)                │
│                                                           │
│  ┌──────────────────────────────────────────────────────┐ │
│  │     ChatMemoryController                             │ │
│  │  • GET /{username}              - Chat with memory   │ │
│  │  • GET /{username}/conversations - View history      │ │
│  │  • GET /conversations   - List all conversationIDs   │ │
│  └────────────────┬─────────────────────────────────────┘ │
│                   │                                       │
│  ┌────────────────▼─────────────────────────────────────┐ │
│  │     Spring AI ChatClient                             │ │
│  │  (Auto-configured by Spring AI BOM)                  │ │
│  │                                                      │ │
│  │  ┌──────────────────────────────────────────────┐    │ │
│  │  │ Advisors:                                    │    │ │
│  │  │ • PromptChatMemoryAdvisor                    │    │ │
│  │  │ • SimpleLoggerAdvisor                        │    │ │
│  │  │ • Per-user conversation ID management        │    │ │
│  │  └──────────────────────────────────────────────┘    │ │
│  └────────────────┬──────────────────────────┬──────────┘ │
└───────────────────┼──────────────────────────┼────────────┘
                    │                          │
          ┌─────────▼──────────┐    ┌─────────▼────────────┐
          │  Ollama            │    │  PostgreSQL          │
          │  localhost:11434   │    │  localhost:5432      │
          │  (llama3.2:3b)     │    │  (Persistent DB)     │
          └────────────────────┘    └──────────────────────┘
```

### Data Flow

**JDBC Chat Flow:**
1. Request: `GET /{username}?question=Hello`
2. ChatMemoryController receives username and question
3. JdbcChatMemoryRepository queries PostgreSQL for prior messages by conversation ID (username)
4. PromptChatMemoryAdvisor builds conversation context from database
5. ChatClient calls Ollama with full conversation history
6. Response returned to client
7. Conversation automatically stored in PostgreSQL via JdbcChatMemoryRepository
8. Conversation persists across requests and application restarts

## Quick Start

### Prerequisites
- Java 21+, Maven 3.8+, Ollama on localhost:11434, PostgreSQL 12+ on localhost:5432

### Setup
```bash
ollama pull llama3.2:3b
cd spring-ai-jdbc-chat-memory
mvn clean install
mvn spring-boot:run
```
Application starts on **http://localhost:8085**

### Test
```bash
curl "http://localhost:8085/alice?question=What%20is%20Spring%20AI?"
curl "http://localhost:8085/alice/conversations"
curl "http://localhost:8085/conversations"
```

## API Endpoints

### 1. Chat with Memory
```http
GET /{username}?question=What%20is%20Spring%20AI?
```
Multi-user chat with persistent memory. Username = conversation ID. Each user has isolated conversation context loaded from PostgreSQL.

**Response:** AI response with full conversation history as context

### 2. View Conversation History
```http
GET /{username}/conversations
```
Returns all messages for user as `List<String>` in chronological order.

### 3. List All Users
```http
GET /conversations
```
Returns all active conversation IDs (usernames) in the system.

## Configuration

### Application YAML
```yaml
spring:
  application:
    name: spring-ai-jdbc-chat-memory
  ai:
    ollama:
      chat:
        model: "llama3.2:3b"
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: embedded
  datasource:
    url: jdbc:postgresql://localhost:5432/chat-memory
    username: user
    password: password
server:
  port: 8085
logging:
  level:
    root: info
    org.springframework.ai: debug
```

### PostgreSQL Setup (Docker - Recommended)
```bash
docker run --name postgres-chat-memory \
  -e POSTGRES_DB=chat-memory \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  -d postgres:15
```

Or via psql:
```bash
psql -U postgres
CREATE DATABASE "chat-memory";
CREATE USER "user" WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE "chat-memory" TO "user";
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.example.com:5432/chat
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_AI_OLLAMA_CHAT_MODEL=llama3.1:8b
export SERVER_PORT=9085
```

### Auto-Generated Schema
```sql
create table spring_ai_chat_memory (
    conversation_id varchar(36) not null,
    content text not null,
    type varchar(10) not null,
    timestamp timestamp not null
);
create index spring_ai_chat_memory_conversation_id_timestamp_idx
    on spring_ai_chat_memory (conversation_id, timestamp);
```


## Development

### Build & Run
```bash
mvn clean install                    # Full build with tests
mvn clean install -DskipTests         # Fast build
mvn spring-boot:run                   # Run locally
mvn test                              # Run tests
```

### Debug & Monitor
Enable debug logging in `application.yaml`:
```yaml
logging:
  level:
    com.moh.yehia.chat.memory: debug
    org.springframework.ai: debug
    org.springframework.data.jdbc: debug
```

Monitor conversations:
```bash
psql -U user -d chat-memory
SELECT conversation_id, type, content FROM spring_ai_chat_memory ORDER BY timestamp DESC;
SELECT DISTINCT conversation_id FROM spring_ai_chat_memory;
DELETE FROM spring_ai_chat_memory WHERE conversation_id = 'alice';
```

## Project Structure

```
spring-ai-jdbc-chat-memory/
├── src/main/java/com/moh/yehia/chat/memory/
│   ├── SpringAiJdbcChatMemoryApplication.java  # Entry point
│   └── ChatMemoryController.java               # 3 REST endpoints
├── src/main/resources/
│   └── application.yaml                         # Configuration
├── pom.xml                                      # Dependencies
└── README.md                                    # This file
```

| File                        | Purpose                                                          |
|-----------------------------|------------------------------------------------------------------|
| `ChatMemoryController.java` | REST endpoints using `JdbcChatMemoryRepository`                  |
| `application.yaml`          | Ollama, PostgreSQL, JDBC memory configuration                    |
| `pom.xml`                   | `spring-ai-starter-model-chat-memory-repository-jdbc` dependency |

## Common Patterns

### Pattern 1: Simple JDBC-Backed Chat

```java
@GetMapping("/{username}")
public String ask(@PathVariable String username, @RequestParam String question) {
    
    return chatClient.prompt()
            .advisors(new SimpleLoggerAdvisor())
            .advisors(PromptChatMemoryAdvisor.builder(
                    MessageWindowChatMemory.builder()
                            .chatMemoryRepository(jdbcChatMemoryRepository)
                            .build()
            ).build())
            .advisors(advisorSpec -> advisorSpec.param(
                    ChatMemory.CONVERSATION_ID, 
                    username  // Per-user conversation isolation
            ))
            .user(question)
            .call()
            .content();
}
```

**Key Points:**
- `JdbcChatMemoryRepository` - Auto-configured by Spring AI
- `ChatMemory.CONVERSATION_ID` - Username serves as conversation ID
- Database queries happen automatically in advisor
- No explicit database calls needed

### Pattern 2: Retrieve Conversation History

```java
@GetMapping("/{username}/conversations")
public List<String> viewConversations(@PathVariable String username) {
    return jdbcChatMemoryRepository
            .findByConversationId(username)  // Query PostgreSQL
            .stream()
            .map(Message::getText)           // Extract text
            .toList();                        // Convert to list
}
```

**Key Points:**
- Direct JDBC query to PostgreSQL
- Returns all messages for conversation ID
- Maintains chronological order from database
- Empty list if no conversation exists

### Pattern 3: List All Active Conversations

```java
@GetMapping("/conversations")
public List<String> viewConversationIDs() {
    return jdbcChatMemoryRepository.findConversationIds();
}
```

**Key Points:**
- Queries distinct conversation IDs
- Shows all active users/conversations
- Useful for monitoring and analytics
- No parameters needed

## Troubleshooting

| Issue                            | Solution                                                                                                     |
|----------------------------------|--------------------------------------------------------------------------------------------------------------|
| **PostgreSQL not running**       | `brew services start postgresql@15`                                                                          |
| **Wrong database credentials**   | Check `application.yaml` datasource config                                                                   |
| **Port 5432 in use**             | Stop PostgreSQL or use different port                                                                        |
| **Ollama connection failed**     | Run `ollama serve` and verify with `curl http://localhost:11434/api/tags`                                    |
| **Port 8085 already in use**     | Change `server.port` in `application.yaml`                                                                   |
| **Schema initialization failed** | Grant privileges: `GRANT ALL ON DATABASE "chat-memory" TO "user"`                                            |
| **Slow queries**                 | Add index: `CREATE INDEX idx_conv ON spring_ai_chat_memory(conversation_id)`                                 |
| **Disk space full**              | Archive old conversations: `DELETE FROM spring_ai_chat_memory WHERE timestamp < NOW() - INTERVAL '6 months'` |

## Performance Considerations

### Comparison
| Feature         | In-Memory | Redis       | JDBC        |
|-----------------|-----------|-------------|-------------|
| **Speed**       | ⚡⚡⚡ Fast  | ⚡⚡ Medium   | ⚡ Moderate  |
| **Persistence** | ❌ No      | ✅ TTL       | ✅ Permanent |
| **Scalability** | Single    | Multiple    | Unlimited   |
| **Querying**    | Window    | Limited     | Full SQL    |
| **Best For**    | Dev/Test  | Distributed | Enterprise  |

### Optimization
1. Add indexes: `CREATE INDEX idx_conv ON spring_ai_chat_memory(conversation_id);`
2. Enable connection pooling (auto-configured)
3. Archive old conversations: `DELETE FROM spring_ai_chat_memory WHERE timestamp < NOW() - INTERVAL '6 months';`
4. Monitor with: `logging.level.org.springframework.jdbc: debug`

## Extension Points

1. **Change LLM Model** - Update `spring.ai.ollama.chat.model` in `application.yaml`
2. **Add conversation search** - Implement full-text search on PostgreSQL
3. **Add conversation export** - Generate PDF/CSV reports from database
4. **Add conversation analytics** - Create dashboards with conversation statistics
5. **Add user authentication** - Store user IDs instead of usernames
6. **Add conversation tagging** - Add tag column to database
7. **Add multi-language support** - Store language metadata with conversations
8. **Add sentiment analysis** - Analyze conversation sentiment with PostgreSQL UDFs
9. **Add rate limiting** - Query-based rate limiting per user
10. **Add conversation archival** - Move old conversations to archive table

## References

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [Spring AI Chat Memory - JDBC](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_chat_memory_jdbc)
- [Ollama GitHub](https://github.com/ollama/ollama)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Data JDBC](https://spring.io/projects/spring-data-jdbc)
- [JDBC Connection Pooling (HikariCP)](https://github.com/brettwooldridge/HikariCP)

