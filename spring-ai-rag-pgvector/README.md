# spring-ai-rag-pgvector

**Retrieval Augmented Generation (RAG) with PostgreSQL PgVector Vector Store**

Spring Boot 4.0.6 application demonstrating enterprise-grade RAG patterns using PostgreSQL with pgvector extension for persistent vector embeddings and similarity search.

## 🎯 Quick Start

### Prerequisites
- **Java**: 21+
- **PostgreSQL**: 12+ with [pgvector](https://github.com/pgvector/pgvector) extension installed
- **Ollama**: Running on `localhost:11434` with embedding model `embeddinggemma:latest`
- **Spring Boot**: 4.0.6
- **Spring AI**: 2.0.0-M5

## 🏗️ Architecture

### System Design

```
┌───────────────────────────────────────────────────────────────┐
│                Spring Boot 4.0.6 (Port 8088)                  │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ ChatController (REST API)                               │  │
│  │ GET / → Store documents + similarity search             │  │
│  └────────────────┬────────────────────────────────────────┘  │
│                   │                                           │
│  ┌────────────────▼────────────────────────────────────────┐  │
│  │ Spring AI ChatClient (Auto-configured)                  │  │
│  │ • VectorStore Advisor (pgvector integration)            │  │
│  │ • Ollama EmbeddingModel (Auto-configured)               │  │
│  └────────────────┬────────────────────────────────────────┘  │
│                   │                                           │
└───────────────────┼───────────────────────────────────────────┘
                    │
        ┌───────────┴──────────────┐
        │                          │
        ▼                          ▼
┌──────────────────┐    ┌──────────────────┐
│ PostgreSQL 12+   │    │ Ollama           │
│ (pgvector)       │    │ localhost:11434  │
│ localhost:5432   │    │ embedding model  │
│ Vector embeddings│    │ embeddinggemma   │
└──────────────────┘    └──────────────────┘
```

### Key Components

| Component                   | Purpose                                    | Type            |
|-----------------------------|--------------------------------------------|-----------------|
| **ChatController**          | REST endpoint for RAG operations           | 1 endpoint      |
| **VectorStore (PgVector)**  | Persistent vector embeddings in PostgreSQL | Database-backed |
| **EmbeddingModel (Ollama)** | Generate embeddings from text              | Remote service  |
| **SearchRequest**           | Query specification for similarity search  | DAO             |
| **Document**                | Vector-annotated text with metadata        | Data model      |

## 📡 API Endpoints

### Store Documents & Similarity Search

**Endpoint**: `GET /`

**Description**: Adds sample documents to pgvector vector store, then performs similarity search for "spring" query

**Request**:
```bash
curl http://localhost:8088/
```

**Response**:
```json
[
  {
    "id": "abc123",
    "content": "Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!",
    "metadata": {
      "meta1": "meta1"
    }
  },
  {
    "id": "def456",
    "content": "The World is Big and Salvation Lurks Around the Corner",
    "metadata": {
      "meta2": "meta2"
    }
  }
]
```

**Process Flow**:
1. Creates 3 sample Document objects with content and metadata
2. Stores documents in pgvector via `vectorStore.accept(documents)`
3. Generates embeddings using Ollama `embeddinggemma:latest` model
4. Stores embeddings in PostgreSQL with pgvector
5. Executes similarity search with query "spring" and topK=2
6. Returns top 2 most similar documents ranked by cosine distance

## ⚙️ Configuration

### application.yaml

```yaml
spring:
  application:
    name: spring-ai-rag-pgvector
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/mydatabase
    username: myuser
    password: secret
  
  # Spring AI Configuration
  ai:
    # Ollama Embedding Model
    ollama:
      embedding:
        model: "embeddinggemma:latest"    # Ollama embedding model
    
    # PgVector Vector Store Configuration
    vectorstore:
      pgvector:
        index-type: hnsw                  # Hierarchical Navigable Small World index
        distance-type: cosine-distance    # Similarity metric (cosine, l2, inner-product)
        max-document-batch-size: 1000     # Batch size for document insertion
        initialize-schema: true           # Auto-create tables on startup

# Server Configuration
server:
  port: 8088                              # Application port

# Logging Configuration
logging:
  level:
    root: info
    org.springframework.ai: debug          # Spring AI framework debug logs
    org.springframework.ai.chat.client.advisor: debug  # Advisor debug logs
```

### Key Configuration Parameters

| Parameter                 | Value             | Purpose                                                        |
|---------------------------|-------------------|----------------------------------------------------------------|
| `index-type`              | `hnsw`            | Approximate nearest neighbor indexing (fast, memory-efficient) |
| `distance-type`           | `cosine-distance` | Similarity metric (alternatives: l2, inner-product)            |
| `max-document-batch-size` | 1000              | Database batch insert performance tuning                       |
| `initialize-schema`       | true              | Auto-create pgvector tables (embeddings, metadata)             |

### Environment Variables (Optional)

```bash
# PostgreSQL Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=secret

# Ollama Endpoint
SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434

# Embedding Model
SPRING_AI_OLLAMA_EMBEDDING_MODEL=embeddinggemma:latest
```

## 🔧 Code Patterns

### Pattern 1: Document Storage with Metadata

```java
List<Document> documents = List.of(
    new Document(
        "Spring AI rocks!! Spring AI rocks!!",
        Map.of("meta1", "meta1")  // Metadata key-value pairs
    ),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document(
        "You walk forward facing the past...",
        Map.of("meta2", "meta2")
    )
);
vectorStore.accept(documents);  // Store in pgvector
```

**Use Case**: Adding documents with associated metadata (author, source, date, category)

**Key Points**:
- `Document` wraps content + metadata map
- `vectorStore.accept()` generates embeddings and stores in PostgreSQL
- Metadata is indexed and searchable via queries

### Pattern 2: Similarity Search with topK

```java
SearchRequest request = SearchRequest.builder()
    .topK(2)                           // Return top 2 results
    .query("spring")                   // Query text (auto-embedded)
    .build();

List<Document> results = vectorStore.similaritySearch(request);
```

**Use Case**: Finding most relevant documents based on user query

**Key Points**:
- Query is automatically converted to embeddings
- `topK=2` returns 2 most similar documents by cosine distance
- Results ranked by similarity score (descending)
- Scalable to millions of documents with pgvector indexing

### Pattern 3: Advanced Search with Metadata Filtering

```java
SearchRequest request = SearchRequest.builder()
    .topK(5)
    .query("AI patterns")
    .withFilterExpression("'meta1' == 'relevant'")  // Optional filter
    .build();
```

**Use Case**: Similarity search with filtering (role-based, category-based)

**Key Points**:
- Filter expression syntax depends on pgvector backend
- Combines vector similarity with metadata predicates
- Reduces search space before embedding comparison

## 🚀 Extension Points

### 1. Multi-Turn RAG Chat

Add QuestionAnswerAdvisor to chatClient for document-grounded conversation:

```java
@GetMapping("/chat")
public String chat(@RequestParam String question) {
    return chatClient.prompt()
        .user(question)
        .advisors(
            QuestionAnswerAdvisor.builder(vectorStore).build()
        )
        .call()
        .content();
}
```

### 2. Hybrid Search (Vector + Full-Text)

Combine vector similarity with PostgreSQL full-text search:

```java
@GetMapping("/hybrid-search")
public List<Document> hybridSearch(
    @RequestParam String query) {
    
    // Vector similarity search (pgvector)
    List<Document> vectorResults = vectorStore.similaritySearch(
        SearchRequest.builder().topK(5).query(query).build()
    );
    
    // Full-text search (PostgreSQL native) on results
    return vectorResults.stream()
        .filter(doc -> doc.getContent().toLowerCase().contains(query.toLowerCase()))
        .toList();
}
```

### 3. Custom Embedding Model

Switch to different embedding model (gemma, nomic, etc.):

**application.yaml**:
```yaml
spring:
  ai:
    ollama:
      embedding:
        model: "nomic-embed-text:latest"  # Alternative embedding model
```

### 4. Batch Similarity Search

Search for multiple queries efficiently:

```java
@PostMapping("/batch-search")
public Map<String, List<Document>> batchSearch(
    @RequestBody List<String> queries) {
    
    return queries.stream().collect(Collectors.toMap(
        q -> q,
        q -> vectorStore.similaritySearch(
            SearchRequest.builder().topK(3).query(q).build()
        )
    ));
}
```

### 5. Streaming RAG Responses

Stream document-grounded responses for long content:

```java
@GetMapping("/stream")
public Flux<String> streamAnswer(@RequestParam String question) {
    List<Document> context = vectorStore.similaritySearch(
        SearchRequest.builder().topK(3).query(question).build()
    );
    
    String contextStr = context.stream()
        .map(Document::getContent)
        .collect(Collectors.joining("\n\n"));
    
    return chatClient.prompt()
        .system("Context: " + contextStr)
        .user(question)
        .stream()
        .content();
}
```

## 📊 Technology Stack

| Component       | Version  | Purpose                  |
|-----------------|----------|--------------------------|
| **Spring Boot** | 4.0.6    | Application framework    |
| **Spring AI**   | 2.0.0-M5 | AI/ML integration        |
| **PostgreSQL**  | 12+      | Relational database      |
| **pgvector**    | Latest   | Vector storage extension |
| **Ollama**      | Latest   | Embedding model service  |
| **Java**        | 21+      | Language runtime         |
| **Maven**       | 4.0+     | Build tool               |


## 📚 Related Modules

- **spring-ai-ollama** (Port 8083) - Core AI patterns with Ollama LLM
- **spring-ai-rag** (Port 8087) - RAG with SimpleVectorStore (in-memory)
- **spring-ai-redis-chat-memory** (Port 8084) - Chat memory with Redis
- **spring-ai-jdbc-chat-memory** (Port 8085) - Chat memory with PostgreSQL
- **spring-ai-tools** (Port 8086) - Tool calling and dynamic function selection

## 🔗 Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Ollama Models](https://ollama.ai/library)
- [Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)

