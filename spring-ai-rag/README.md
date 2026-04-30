# spring-ai-rag
A Spring Boot 4.0.5 demonstration of **Retrieval Augmented Generation (RAG)** using [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/). This module showcases how to combine document retrieval with generative AI to provide accurate, context-aware responses grounded in your data.

## 📋 Table of Contents
- [Overview](#overview)
- [Key Technologies](#key-technologies)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [VectorStore Setup](#vectorstore-setup)
- [Configuration](#configuration)
- [Development](#development)
- [Project Structure](#project-structure)
- [Common Patterns](#common-patterns)
- [Extension Points](#extension-points)
- [References](#references)


## Overview

**RAG (Retrieval Augmented Generation)** is a pattern that combines document retrieval with LLM generation to provide accurate, context-grounded responses. This module demonstrates:

- ✨ **Document embeddings** - Convert documents to vector representations
- ✨ **Semantic search** - Retrieve relevant documents using similarity
- ✨ **Context augmentation** - Add retrieved documents to LLM prompts
- ✨ **Accurate responses** - Ground LLM answers in your data
- ✨ **Vector store integration** - Use SimpleVectorStore for in-memory storage

**Perfect for:**
- Q&A systems over company documents
- Knowledge base search
- FAQ automation
- Document analysis
- Information retrieval pipelines

## Key Technologies

| Technology       | Version   | Purpose                               |
|------------------|-----------|---------------------------------------|
| **Java**         | 21        | Language runtime                      |
| **Spring Boot**  | 4.0.6     | Application framework                 |
| **Spring AI**    | 2.0.0-M4  | AI abstraction with RAG support       |
| **Ollama**       | Latest    | Local LLM orchestration (llama3.2:3b) |
| **Vector Store** | In-Memory | SimpleVectorStore for embeddings      |

## Architecture

### Component Diagram

```
┌────────────────────────────────────────────────────────┐
│        Spring Boot Application (Port 8087)             │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │     ChatController                               │  │
│  │  • GET /?question=... - RAG-enhanced chat        │  │
│  └────────────────┬─────────────────────────────────┘  │
│                   │                                    │
│  ┌────────────────▼─────────────────────────────────┐  │
│  │     Spring AI ChatClient                         │  │
│  │                                                  │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │ Advisors:                                  │  │  │
│  │  │ • QuestionAnswerAdvisor (document search)  │  │  │
│  │  │ • SimpleLoggerAdvisor (logging)            │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  │                                                  │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │ VectorStore:                               │  │  │
│  │  │ • SimpleVectorStore (in-memory)            │  │  │
│  │  │ • Stores embeddings + documents            │  │  │
│  │  └────────────────────────────────────────────┘  │  │
│  └────────────────┬─────────────────────────────────┘  │
└───────────────────┼────────────────────────────────────┘
                    │
            ┌───────▼──────────┐
            │  Ollama          │
            │  localhost:11434 │
            │  (llama3.2:3b)   │
            │  (embeddings)    │
            └──────────────────┘
```

### RAG Flow

1. **Document Indexing** - Load documents and create embeddings via VectorStoreConfig
2. **Query** - User asks question: `"What is Spring AI?"`
3. **Retrieval** - QuestionAnswerAdvisor searches VectorStore for similar documents
4. **Augmentation** - Add top-K relevant documents to LLM prompt
5. **Generation** - LLM generates answer grounded in retrieved context
6. **Response** - Return context-aware answer to user

## Quick Start

### Prerequisites

1. **Java 21+** installed
2. **Maven 3.8+** installed
3. **Ollama** running on localhost:11434
4. **Model installed**: `ollama pull llama3.2:3b`

### Setup Steps

```bash
# 1. Navigate to module directory
cd spring-ai-rag

# 2. Build the application
mvn clean install

# 3. Run the application
mvn spring-boot:run
```

On startup, VectorStoreConfig loads `documents.txt` and creates embeddings. Application starts on **http://localhost:8087**

### Test Endpoint

```bash
# Ask a question (will search documents and use context)
curl "http://localhost:8087/?question=What%20is%20Spring%20AI?"
```

Response uses retrieved documents for accuracy!

## API Endpoints

### RAG-Enhanced Chat Endpoint

**Request:**
```http
GET /?question=What is Spring AI?
```

**Parameters:**
- `question` (query) - User question; documents are retrieved and included in prompt

**Description:** Chat endpoint enhanced with document retrieval. QuestionAnswerAdvisor retrieves relevant documents from SimpleVectorStore based on semantic similarity, augments the prompt, and the LLM generates grounded responses.

**Response:**
```
Spring AI is a Spring project that provides an abstraction over AI models like Ollama,
allowing you to build AI-powered applications with Spring Boot. It integrates with Ollama
for local LLM orchestration and supports RAG patterns through vector stores for document retrieval.
```

**Implementation Details:**
- `QuestionAnswerAdvisor` handles document retrieval and prompt augmentation
- `SearchRequest` configures similarity threshold (0.8) and top-K (6)
- SimpleVectorStore stores embeddings from `documents.txt`
- `SimpleLoggerAdvisor` logs request/response for observability
- Documents are automatically embedded on initialization

## VectorStore Setup

### SimpleVectorStore Bean

The `VectorStoreConfig` class creates and configures SimpleVectorStore:

```java
@Bean
public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
    return new SimpleVectorStore(embeddingModel);
}
```

**Key Points:**
- EmbeddingModel auto-configured from Spring AI BOM (Ollama by default)
- In-memory storage—survives application restarts only if data persisted
- Perfect for development and testing
- Use PgVectorStore for production persistence

### Document Loading

CommandLineRunner initializes vector store with documents on startup:

```java
@Bean
public CommandLineRunner initVectorStore(SimpleVectorStore vectorStore) {
    return args -> {
        String documents = new String(
            new ClassPathResource("documents.txt").getInputStream().readAllBytes()
        );
        vectorStore.add(org.springframework.ai.document.Document.from(documents));
    };
}
```

**Process:**
1. Load `documents.txt` from classpath
2. Create Document objects from text
3. Embeddings auto-created (Ollama embedding model)
4. Stored in SimpleVectorStore

## Configuration

### Application YAML (`application.yaml`)

```yaml
spring:
  application:
    name: spring-ai-rag
  ai:
    ollama:
      chat:
        model: "llama3.2:3b"  # Model for chat responses

server:
  port: 8087                  # RAG module port

logging:
  level:
    root: info
    org.springframework.ai: debug
```

### Search Configuration

In ChatController:

```java
SearchRequest.builder()
    .similarityThreshold(0.8)  // Only documents with >80% similarity
    .topK(6)                   # Retrieve top 6 documents
    .build()
```

**Tuning:**
- **similarityThreshold**: 0.0-1.0, higher = stricter matching
- **topK**: Number of documents to retrieve (6 = good balance)

## Development

### Building

```bash
# Full build with tests
mvn clean install

# Fast build (skip tests)
mvn clean install -DskipTests
```

### Running

```bash
# Development mode with auto-reload
mvn spring-boot:run

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9087"
```

### Debugging

Enable detailed logging:

```yaml
logging:
  level:
    com.moh.yehia.rag: debug
    org.springframework.ai: debug
    org.springframework.ai.vectorstore: debug
```

## Project Structure

```
spring-ai-rag/
├── src/
│   ├── main/
│   │   ├── java/com/moh/yehia/rag/
│   │   │   ├── SpringAiRagApplication.java    # Entry point
│   │   │   ├── ChatController.java            # RAG endpoint
│   │   │   └── VectorStoreConfig.java         # VectorStore bean
│   │   └── resources/
│   │       ├── application.yaml               # Configuration
│   │       └── documents.txt                  # Sample documents
│   └── test/
│       └── java/...
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
└── HELP.md                                    # Spring Boot help
```

## Common Patterns

### Pattern 1: Basic RAG with SimpleVectorStore

```java
@GetMapping
public String chat(@RequestParam String question) {
    QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder()
                    .similarityThreshold(0.8)
                    .topK(6)
                    .build())
            .build();
    
    return chatClient.prompt()
            .user(question)
            .advisors(new SimpleLoggerAdvisor(), qaAdvisor)
            .call()
            .content();
}
```

**Key Points:**
- QuestionAnswerAdvisor retrieves documents automatically
- SimpleVectorStore stores embeddings in-memory
- SearchRequest tunes retrieval behavior
- LLM answers based on retrieved context

### Pattern 2: Custom Document Loading

```java
public void addDocuments(String content) {
    List<Document> documents = Arrays.asList(
        new Document(content, Map.of("source", "custom"))
    );
    vectorStore.add(documents);
}
```

### Pattern 3: Advanced Search Configuration

```java
.searchRequest(SearchRequest.builder()
        .similarityThreshold(0.75)     // Lower = broader matches
        .topK(10)                      // More documents for context
        .filterExpression("source == 'docs'")  // Metadata filtering
        .build())
```

## Extension Points

1. **Add documents dynamically** - Upload files at runtime, add to vector store
2. **Use PgVectorStore** - Replace SimpleVectorStore with PostgreSQL backend
3. **Custom embedding model** - Use different embeddings provider
4. **Metadata filtering** - Filter documents by source, date, category
5. **Hybrid search** - Combine semantic and keyword search
6. **Multi-document types** - Support PDFs, web pages, CSV data
7. **Document chunking** - Split large documents for better retrieval
8. **Re-ranking** - Re-rank retrieved documents with secondary model
9. **Cache embeddings** - Store embeddings externally for reuse
10. **Cost optimization** - Use smaller embedding models for speed

## References

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [Spring AI RAG Pattern](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_retrieval_augmented_generation_rag)
- [Spring AI Vector Stores](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
- [SimpleVectorStore JavaDoc](https://docs.spring.io/spring-ai/api/org/springframework/ai/vectorstore/SimpleVectorStore.html)
- [Ollama GitHub](https://github.com/ollama/ollama)
