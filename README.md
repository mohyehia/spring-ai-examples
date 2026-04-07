# spring-ai-examples

A Spring Boot repository demonstrating **AI integration patterns** using [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/), a local LLM orchestration framework.

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Spring AI Integration](#-spring-ai-integration)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Development](#-development)
- [Troubleshooting](#-troubleshooting)

## 🎯 Project Overview

This repository contains practical examples of integrating **Spring Boot 4.0.5** with **Spring AI 2.0.0-M4** to build AI-powered REST applications. Currently, it demonstrates integration with **Ollama**, a local LLM provider, allowing you to run language models locally without cloud dependencies.

### Current Modules

#### `spring-ai-ollama`
A minimalist Spring Boot REST service that showcases Spring AI's ChatClient abstraction layer for LLM interactions.

- **Language:** Java 21
- **Framework:** Spring Boot 4.0.5
- **AI Library:** Spring AI 2.0.0-M4
- **LLM Provider:** Ollama with llama3.2:3b model (configurable)
- **Port:** 8083

## 🔧 Prerequisites

### System Requirements
- **Java 21** or higher
- **Maven 3.8+** (or use bundled mvnw/mvnw.cmd)
- **Ollama** installed and running locally

### Ollama Setup
1. **Install Ollama** from [ollama.ai](https://ollama.ai/)
2. **Start Ollama service** (runs on `localhost:11434` by default)
3. **Pull the required model:**
   ```bash
   ollama pull llama3.2:3b
   ```
   Or for better quality (requires more resources):
   ```bash
   ollama pull llama3.1:8b
   ```
4. **Verify Ollama is running:**
   ```bash
   curl http://localhost:11434/api/tags
   ```

⚠️ **Critical:** Ollama must be running before starting the Spring Boot application. The application will start but fail silently when making requests if Ollama is unavailable.

## 🚀 Quick Start

### Build the Project
```bash
cd spring-ai-ollama
mvn clean install
```

### Run the Application
```bash
# Option 1: Using Maven plugin
mvn spring-boot:run

# Option 2: Using wrapper (cross-platform)
./mvnw spring-boot:run

# Option 3: Run JAR directly
mvn clean package
java -jar target/spring-ai-ollama-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8083`

### Test the Application
```bash
# Get a joke from the AI model
curl http://localhost:8083/

# Response example:
# ChatClientResponse{content='Why did the programmer go broke? He used up all his cache!', metadata=...}
```

## 📁 Project Structure

```
spring-ai-examples/
├── README.md                          # This file
├── AGENTS.md                          # AI coding agent guide
├── LICENSE
└── spring-ai-ollama/                  # Main module
    ├── pom.xml                        # Maven configuration
    ├── HELP.md                        # Spring Boot generated help
    ├── generated-http-requests.http   # HTTP client test file
    └── src/
        ├── main/
        │   ├── java/com/moh/yehia/ollama/
        │   │   ├── ChatController.java              # Simple prompt endpoint
        │   │   ├── PromptController.java            # Advanced prompting patterns
        │   │   ├── OutputParserController.java      # Output parsing & conversion
        │   │   ├── MetadataController.java          # Metadata & advisors management
        │   │   ├── SongDTO.java                     # Data transfer object
        │   │   └── SpringAiOllamaApplication.java   # Entry point
        │   └── resources/
        │       ├── application.yaml                 # Configuration
        │       └── prompts/                         # StringTemplate prompt files
        │           ├── youtube-prompt.st            # Parameterized template
        │           ├── system-prompt.st             # System message template
        │           ├── song-text-prompt.st          # Song list (text format)
        │           └── song-structured-prompt.st    # Song list (structured format)
        └── test/
            └── java/com/moh/yehia/ollama/
                └── SpringAiOllamaApplicationTests.java  # Context load test
```

## 🤖 Spring AI Integration

### What is Spring AI?

Spring AI is a Spring Framework project that provides a **unified abstraction layer** for AI/LLM operations. It allows developers to:

- **Abstract away provider differences:** Switch between OpenAI, Azure, Ollama, etc., with minimal code changes
- **Use a consistent API:** ChatClient provides a fluent API for all LLM interactions
- **Convert unstructured to structured data:** Output converters transform LLM text into Java objects
- **Build enterprise AI applications:** Prompt templates, message composition, and response parsing
- **Focus on business logic:** Not on low-level HTTP or API details

### Architecture

```
┌─────────────────────────────────────┐
│      Spring Boot Application        │
│                                     │
│  ┌───────────────────────────────┐  │
│  │    ChatController (REST)      │  │
│  │  GET /                        │  │
│  └────────┬────────────────────┬─┘  │
│           │                    │    │
│  ┌────────▼────────────────────▼──┐ │
│  │   Spring AI ChatClient         │ │
│  │  (Auto-configured by Spring)   │ │
│  └────────┬──────────────────────┘  │
│           │                         │
└───────────┼──────────────────────┬──┘
            │                      │
      ┌─────▼───────┐      ┌──────▼────────┐
      │   Ollama    │      │  application  │
      │ localhost   │      │  .yaml config │
      │  :11434     │      └───────────────┘
      └─────────────┘
```

### ChatClient Pattern

This project demonstrates the standard Spring AI ChatClient usage pattern:

```java
// Constructor injection of ChatClient.Builder (auto-configured)
public ChatController(ChatClient.Builder builder) {
    this.chatClient = builder.build();
}

// Usage pattern in endpoint
ChatClientResponse response = chatClient.prompt()
    .user("Tell me a joke about computers")
    .call()
    .chatClientResponse();
```

**Key Points:**
- `ChatClient.Builder` is autoconfigured by Spring AI
- No manual HttpClient or API call construction needed
- Response returns `ChatClientResponse` object with metadata
- This pattern works across different LLM providers (swap provider = change config)

### Advanced Patterns: Prompt Templates & System Messages

The `PromptController` introduces more sophisticated patterns for enterprise scenarios:

**Pattern 1: Parameterized Prompt Templates**
```java
Prompt prompt = new PromptTemplate(templateResource)
    .create(Map.of("genre", "tech"));
ChatResponse response = chatClient.prompt(prompt)
    .call()
    .chatResponse();
return response.getResults().getFirst().getOutput().getText();
```

**Pattern 2: System Prompts + User Input**
```java
SystemMessage systemMessage = new SystemMessage(systemPromptResource);
UserMessage userMessage = new UserMessage(userInput);
Prompt prompt = new Prompt(systemMessage, userMessage);
ChatResponse response = chatClient.prompt(prompt)
    .call()
    .chatResponse();
return response.getResults().getFirst().getOutput().getText();
```

**Differences from Simple Pattern:**
- `ChatResponse` vs `ChatClientResponse` return types
- `.getResults()` to extract list of results
- `.getFirst().getOutput().getText()` to get raw text (not toString())
- Support for variable interpolation in templates
- Ability to define AI behavior with system messages

### Output Parsing & Data Conversion

One of Spring AI's most powerful features is **output parsing** - converting unstructured LLM text responses into structured Java objects. This project demonstrates three approaches:

**Pattern 1: ListOutputConverter (Unstructured → List)**
```java
ListOutputConverter converter = new ListOutputConverter();
Prompt prompt = new PromptTemplate(resource)
    .create(Map.of("artist", "Adele", "format", converter.getFormat()));
List<String> songs = converter.convert(
    chatClient.prompt(prompt).call().content()
);
// Result: ["Song 1", "Song 2", ...]
```

**Pattern 2: MapOutputConverter (Unstructured → Map)**
```java
MapOutputConverter converter = new MapOutputConverter();
Prompt prompt = new PromptTemplate(resource)
    .create(Map.of("artist", "Adele", "format", converter.getFormat()));
Map<String, Object> songs = converter.convert(
    chatClient.prompt(prompt).call().content()
);
// Result: {"1": "Song description", "2": "Song description", ...}
```

**Pattern 3: BeanOutputConverter (Unstructured → POJO)**
```java
BeanOutputConverter<List<SongDTO>> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});
Prompt prompt = new PromptTemplate(resource)
    .create(Map.of("artist", "Adele", "format", converter.getFormat()));
List<SongDTO> songs = converter.convert(
    chatClient.prompt(prompt).call().content()
);
// Result: [SongDTO(artist="Adele", title="...", description="..."), ...]
```

**Key Insight:** The converter's `getFormat()` method injects formatting instructions into the prompt, guiding the LLM to produce output parseable by the converter. This makes structured data extraction reliable and type-safe.

## ⚙️ Configuration

### application.yaml

```yaml
spring:
  application:
    name: spring-ai-ollama
  ai:
    ollama:
      chat:
        model: "llama3.2:3b"        # Model to use (must be pulled in Ollama)
server:
  port: 8083                        # Spring Boot server port
logging:
  level:
    root: info                      # Logging level
    org.springframework.ai: debug    # Enable debug logging for Spring AI
```

### Customization Options

| Configuration                 | Default                  | Purpose            |
|-------------------------------|--------------------------|--------------------|
| `spring.ai.ollama.chat.model` | `llama3.1:8b`            | Ollama model name  |
| `spring.ai.ollama.base-url`   | `http://localhost:11434` | Ollama service URL |
| `server.port`                 | `8083`                   | Application port   |

### Prompt Templates

The application uses **StringTemplate** files (`.st` format) for parameterized prompts:

#### youtube-prompt.st
```
List 10 of the most popular YouTubers in {genre} along with their current subscriber counts. 
If you don't know the answer, just say "I don't know"
```

Used by `/prompts/popular?genre=tech` endpoint. The `{genre}` placeholder is replaced dynamically.

#### system-prompt.st
```
You are a helpful assistant that provides information about popular YouTubers. 
When asked about a genre, you will list 10 of the most popular YouTubers in that genre 
along with their current subscriber counts.
If you don't know the answer, just say "I don't know".
When you are asked about anything else, Tell them that this is not your Job.
```

Used by `/prompts/instruct` endpoint to establish AI behavior. Works with any user input.

**Creating New Templates:**
1. Add a `.st` file to `src/main/resources/prompts/`
2. Inject with `@Value("classpath:prompts/yourfile.st")`
3. Use `new PromptTemplate(resourceFile).create(Map.of("key", value))`
4. Call `chatClient.prompt(prompt).call().chatResponse()`

### Changing the Model

To use a different Ollama model:

1. Pull the model: `ollama pull mistral`
2. Update `application.yaml`:
   ```yaml
   spring:
     ai:
       ollama:
         chat:
           model: "mistral"
   ```
3. Restart the application

Available models can be listed via: `ollama list`

## 📡 API Endpoints

This section documents all REST endpoints available in the application, organized by controller.

### ChatController Endpoints

#### GET /

Returns a joke generated by the AI model using a simple user prompt.

**Request:**
```bash
curl http://localhost:8083/
```

**Response:**
```
ChatClientResponse{content='Why did the programmer quit his job? Because he didn't get arrays!', metadata=AiResponseMetadata{...}}
```

**Implementation Pattern:**
- Uses `ChatClient.Builder` auto-configured by Spring
- Simple fluent API: `.prompt()` → `.user()` → `.call()` → `.chatClientResponse()`
- Returns `ChatClientResponse` with content and metadata

### PromptController Endpoints

The PromptController (`/prompts` base path) demonstrates advanced Spring AI patterns using prompt templates and system messages.

#### GET /prompts

Returns response to a simple hardcoded prompt.

**Request:**
```bash
curl http://localhost:8083/prompts
```

**Response:**
```
The capital of France is Paris.
```

**Pattern:** Basic `ChatResponse` usage with extracted text from results.

#### GET /prompts/popular

Returns a list of popular YouTubers in a specified genre using **parameterized prompt templates**.

**Request:**
```bash
curl "http://localhost:8083/prompts/popular?genre=gaming"
```

**Response:**
```
Here are 10 of the most popular YouTubers in gaming:
1. PewDiePie - ~111M subscribers
2. SET India - ~180M subscribers
...
```

**Key Features:**
- **Prompt Template:** Uses `youtube-prompt.st` with `{genre}` placeholder
- **Parameter Binding:** `Map.of("genre", "gaming")` replaces template variables
- **Template Location:** `src/main/resources/prompts/youtube-prompt.st`

**Implementation:**
```java
Prompt prompt = new PromptTemplate(toutubePromptResource)
    .create(Map.of("genre", genre));
ChatResponse chatResponse = chatClient.prompt(prompt)
    .call()
    .chatResponse();
return chatResponse.getResults().getFirst().getOutput().getText();
```

#### GET /prompts/instruct

Returns AI response using **system prompts** combined with user input.

**Request:**
```bash
curl "http://localhost:8083/prompts/instruct?userInput=Tell%20me%20a%20Joke"
```

**Response:**
```
Why did the AI go to school? To improve its learning model!
```

**Key Features:**
- **System Message:** Establishes AI behavior/role via `system-prompt.st`
- **User Message:** Dynamic input from request parameter
- **Prompt Stacking:** Both system and user messages combined into single `Prompt`

**Implementation:**
```java
SystemMessage systemMessage = new SystemMessage(systemPromptResource);
UserMessage userMessage = new UserMessage(userInput);
Prompt prompt = new Prompt(systemMessage, userMessage);
ChatResponse chatResponse = chatClient.prompt(prompt)
    .call()
    .chatResponse();
return chatResponse.getResults().getFirst().getOutput().getText();
```

**System Prompt Content** (`system-prompt.st`):
```
You are a helpful assistant that provides information about popular YouTubers. 
When asked about a genre, you will list 10 of the most popular YouTubers in that genre along with their current subscriber counts.
If you don't know the answer, just say "I don't know".
When you are asked about anything else, Tell them that this is not your Job.
```

### OutputParserController Endpoints

The `OutputParserController` (`/parser` base path) demonstrates **Spring AI's output parsing and conversion** capabilities. This is crucial for structured data extraction from LLM responses.

#### GET /parser

Returns a list of top 5 songs for an artist as plain text using the song-text-prompt template.

**Request:**
```bash
curl "http://localhost:8083/parser?artist=Adele"
```

**Response:**
```
Here are the top 5 songs by Adele:
1. Hello - A powerful ballad about reconnection
2. Someone Like You - An emotional piano-driven song
3. Skyfall - A dramatic song for the James Bond soundtrack
4. Easy On Me - A piano ballad about moving forward
5. Rolling In The Deep - An upbeat breakup anthem
```

**Key Features:**
- **Prompt Template:** Uses `song-text-prompt.st` with `{artist}` parameter
- **Plain Text Response:** Returns raw text output from the model
- **Default Parameter:** Uses "Adele" if no artist is specified

**Implementation:**
```java
Prompt prompt = new PromptTemplate(textPrompt)
    .create(Map.of("artist", artist));
ChatResponse chatResponse = chatClient.prompt(prompt)
    .call()
    .chatResponse();
return chatResponse.getResults()
    .getFirst()
    .getOutput()
    .getText();
```

#### GET /parser/list

Returns a list of top 5 songs for an artist as a **List<String>** using ListOutputConverter.

**Request:**
```bash
curl "http://localhost:8083/parser/list?artist=Taylor%20Swift"
```

**Response:**
```json
[
  "1. Anti-Hero - A introspective pop song",
  "2. Blank Space - An upbeat pop anthem",
  "3. Love Story - A romantic country-pop crossover",
  "4. You Belong With Me - A catchy pop-country track",
  "5. Shake It Off - An energetic pop song"
]
```

**Key Features:**
- **Output Converter:** `ListOutputConverter` parses LLM text output into a Java List
- **Format Instructions:** Converter automatically injects format requirements into prompt
- **Structured Response:** JSON array format makes it easy for frontend consumption

**Implementation:**
```java
ListOutputConverter listOutputConverter = new ListOutputConverter();
Prompt prompt = new PromptTemplate(structuredPrompt)
    .create(Map.of("artist", artist, "format", listOutputConverter.getFormat()));
String content = chatClient.prompt(prompt)
    .call()
    .content();
return listOutputConverter.convert(content);
```

#### GET /parser/map

Returns song data as a **Map<String, Object>** using MapOutputConverter.

**Request:**
```bash
curl "http://localhost:8083/parser/map?artist=The%20Beatles"
```

**Response:**
```json
{
  "1": "Let It Be - A philosophical rock ballad",
  "2": "Hey Jude - An epic anthemic pop-rock song",
  "3": "Yesterday - A melancholic string-backed ballad",
  "4": "A Day In The Life - An experimental rock masterpiece",
  "5": "Come Together - A funk-driven rock song"
}
```

**Key Features:**
- **Output Converter:** `MapOutputConverter` converts LLM output to key-value pairs
- **Flexible Structure:** Map allows arbitrary keys and values
- **Queryable Format:** Easy to access specific entries programmatically

**Implementation:**
```java
MapOutputConverter mapOutputConverter = new MapOutputConverter();
Prompt prompt = new PromptTemplate(structuredPrompt)
    .create(Map.of("artist", artist, "format", mapOutputConverter.getFormat()));
String content = chatClient.prompt(prompt)
    .call()
    .content();
return mapOutputConverter.convert(content);
```

#### GET /parser/dto

Returns song data as a **List<SongDTO>** using BeanOutputConverter for strongly-typed objects.

**Request:**
```bash
curl "http://localhost:8083/parser/dto?artist=Billie%20Eilish"
```

**Response:**
```json
[
  {
    "artist": "Billie Eilish",
    "title": "Bad Guy",
    "description": "A dark, minimalist pop track with a heavy baseline"
  },
  {
    "artist": "Billie Eilish",
    "title": "When We All Fall Asleep",
    "description": "A contemplative ballad about collective vulnerability"
  },
  {
    "artist": "Billie Eilish",
    "title": "Ocean Eyes",
    "description": "A melodic indie-pop song about admiration"
  },
  {
    "artist": "Billie Eilish",
    "title": "Therefore I Am",
    "description": "A confident pop anthem about self-awareness"
  },
  {
    "artist": "Billie Eilish",
    "title": "Happier Than Ever",
    "description": "An emotional rock-pop ballad about heartbreak"
  }
]
```

**Key Features:**
- **Strongly-Typed Objects:** `BeanOutputConverter<List<SongDTO>>` converts to Java POJOs
- **Type Safety:** Compiler-checked field names and types
- **DTO Usage:** `SongDTO` is a Java record with fields: `artist`, `title`, `description`
- **Best for Production:** Recommended pattern for API responses

**SongDTO Record Definition:**
```java
public record SongDTO(String artist, String title, String description) {
}
```

**Implementation:**
```java
BeanOutputConverter<List<SongDTO>> beanOutputConverter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

Prompt prompt = new PromptTemplate(structuredPrompt)
    .create(Map.of("artist", artist, "format", beanOutputConverter.getFormat()));
String content = chatClient.prompt(prompt)
    .call()
    .content();
return beanOutputConverter.convert(content);
```

#### GET /parser/entity

Returns song data as a **List<SongDTO>** using the ChatClient's native `.entity()` method for direct type conversion.

**Request:**
```bash
curl "http://localhost:8083/parser/entity?artist=Adele"
```

**Response:**
```json
[
  {
    "artist": "Adele",
    "title": "Hello",
    "description": "A powerful ballad about reconnection"
  },
  {
    "artist": "Adele",
    "title": "Someone Like You",
    "description": "An emotional piano-driven song"
  },
  {
    "artist": "Adele",
    "title": "Skyfall",
    "description": "A dramatic song for the James Bond soundtrack"
  },
  {
    "artist": "Adele",
    "title": "Easy On Me",
    "description": "A piano ballad about moving forward"
  },
  {
    "artist": "Adele",
    "title": "Rolling In The Deep",
    "description": "An upbeat breakup anthem"
  }
]
```

**Key Features:**
- **Native ChatClient Method:** Uses `.entity()` for direct type conversion
- **Simplified Pattern:** No need for manual BeanOutputConverter instantiation
- **Type Reference:** Still requires `ParameterizedTypeReference<>()` for generic types
- **Cleaner Code:** More idiomatic Spring AI pattern

**Implementation:**
```java
public List<SongDTO> getNativeSongs(@RequestParam(defaultValue = "Adele") String artist) {
    return chatClient.prompt(new PromptTemplate(textPrompt)
            .create(Map.of("artist", artist)))
        .call()
        .entity(new ParameterizedTypeReference<>() {});
}
```

#### GET /parser/response-entity

Returns both the **ChatResponse** metadata and parsed song data as **List<SongDTO>** using the ResponseEntity wrapper.

**Request:**
```bash
curl "http://localhost:8083/parser/response-entity?artist=Taylor%20Swift"
```

**Response:**
```json
{
  "chatResponse": {
    "metadata": {
      "model": "llama3.2:3b",
      "tokens": {
        "inputTokens": 45,
        "outputTokens": 238
      }
    },
    "results": [...]
  },
  "entity": [
    {
      "artist": "Taylor Swift",
      "title": "Anti-Hero",
      "description": "A introspective pop song"
    },
    {
      "artist": "Taylor Swift",
      "title": "Blank Space",
      "description": "An upbeat pop anthem"
    },
    {
      "artist": "Taylor Swift",
      "title": "Love Story",
      "description": "A romantic country-pop crossover"
    },
    {
      "artist": "Taylor Swift",
      "title": "You Belong With Me",
      "description": "A catchy pop-country track"
    },
    {
      "artist": "Taylor Swift",
      "title": "Shake It Off",
      "description": "An energetic pop song"
    }
  ]
}
```

**Key Features:**
- **Dual Response:** Returns both response metadata and parsed entity in one call
- **Type Reference:** Uses `ResponseEntity<ChatResponse, List<SongDTO>>` wrapper
- **Metadata Access:** Access token counts, model info, and other metadata alongside data
- **Advanced Pattern:** Most feature-rich output handling approach

**Implementation:**
```java
public ResponseEntity<ChatResponse, List<SongDTO>> getResponseEntitySongs(@RequestParam(defaultValue = "Adele") String artist) {
    return chatClient.prompt(new PromptTemplate(textPrompt)
            .create(Map.of("artist", artist)))
        .call()
        .responseEntity(new ParameterizedTypeReference<>() {});
}
```

#### GET /parser/stream

Returns song data as a **Flux<String>** stream for real-time, non-blocking response streaming.

**Request:**
```bash
curl "http://localhost:8083/parser/stream?artist=The%20Beatles"
```

**Response (Streaming):**
```
Here are the top 5 songs by The Beatles:
1. Let It Be - A philosophical rock ballad
2. Hey Jude - An epic anthemic pop-rock song
3. Yesterday - A melancholic string-backed ballad
4. A Day In The Life - An experimental rock masterpiece
5. Come Together - A funk-driven rock song
```

**Key Features:**
- **Server-Sent Events (SSE) Support:** Real-time streaming responses
- **Non-Blocking:** Uses Project Reactor's `Flux<String>` for async streaming
- **Token-by-Token:** LLM output sent as chunks for faster perceived response time
- **Large Response Handling:** Ideal for long-form content generation
- **Frontend Integration:** Compatible with JavaScript's EventSource API

**Implementation:**
```java
@GetMapping("/stream")
public Flux<String> getSongsAsStream(@RequestParam(defaultValue = "Adele") String artist) {
    Prompt prompt = new PromptTemplate(textPrompt)
        .create(Map.of("artist", artist));
    return chatClient.prompt(prompt)
        .stream()
        .content();
}
```

**Client-Side Streaming Example (JavaScript):**
```javascript
const eventSource = new EventSource('http://localhost:8083/parser/stream?artist=Adele');
eventSource.onmessage = (event) => {
    document.getElementById('response').innerHTML += event.data;
};
eventSource.onerror = () => eventSource.close();
```

### MetadataController Endpoints

The `MetadataController` (`/metadata` base path) demonstrates **Spring AI's metadata management** and **advisor patterns** for request/response logging and processing.

#### GET /metadata

Returns a ChatResponse with custom metadata attached to both system and user prompts.

**Request:**
```bash
curl http://localhost:8083/metadata
```

**Response:**
```json
{
  "metadata": {
    "version": "1.0",
    "messageId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "logged-in-userId"
  },
  "results": [
    {
      "output": {
        "text": "Why did the programmer go broke? Because he used up all his cache!"
      }
    }
  ]
}
```

**Key Features:**
- **System Metadata:** Version tracking (`version: "1.0"`)
- **User Metadata:** Message tracking with UUID, user ID, priority levels
- **Metadata Propagation:** Metadata attached to prompts carries through to response
- **Structured Logging:** Custom metadata fields for better traceability

**Implementation:**
```java
@GetMapping
public ChatResponse testMetadata() {
    ChatResponse chatResponse = chatClient.prompt()
            .system(promptSystemSpec -> promptSystemSpec.text("You are a helpful assistant")
                    .metadata("version", "1.0")
            )
            .user(promptUserSpec -> promptUserSpec.text("Tell me a joke about programming")
                    .metadata("messageId", UUID.randomUUID().toString())
                    .metadata("userId", "logged-in-userId")
                    .metadata("priority", "high")
            ).call()
            .chatResponse();
    log.info("ChatResponse =>{}", chatResponse);
    return chatResponse;
}
```

**Metadata Use Cases:**
- Request tracing and correlation IDs
- User identification and session tracking
- Priority/importance levels
- Request source tracking
- Custom logging fields

#### GET /metadata/default

Returns a ChatResponse using pre-configured default system and user context with metadata through the ChatClient builder.

**Request:**
```bash
curl http://localhost:8083/metadata/default
```

**Response:**
```json
{
  "metadata": {
    "assistantType": "general",
    "version": "2.0",
    "sessionId": "default-session-id"
  },
  "results": [
    {
      "output": {
        "text": "Why did the computer go to school? To improve its byte!"
      }
    }
  ]
}
```

**Key Features:**
- **Default System Context:** "You are a helpful assistant" (always included)
- **Default User Context:** "Default user context" (always included)
- **Pre-configured Metadata:** Assistant type and version metadata set at builder level
- **Consistent Behavior:** Same metadata applied to all requests using this client
- **SimpleLoggerAdvisor:** Built-in logging advisor for request/response tracing

**Implementation:**
```java
this.defaultChatClient = builder
    .defaultSystem(promptSystemSpec -> promptSystemSpec.text("You are a helpful assistant")
            .metadata("assistantType", "general")
            .metadata("version", "2.0")
    ).defaultUser(promptUserSpec -> promptUserSpec.text("Default user context")
            .metadata("sessionId", "default-session-id")
    )
    .build();

// Usage:
defaultChatClient.prompt("Tell me a joke about computers")
    .advisors(new SimpleLoggerAdvisor())
    .call()
    .chatResponse();
```

**Default Context Benefits:**
- Consistent system prompts across all requests
- Automatic metadata injection
- Reduced boilerplate in endpoints
- Centralized configuration
- Built-in logging through advisors

#### GET /metadata/custom-logger-advisor

Returns a string response using a **custom SimpleLoggerAdvisor** with custom request/response logging logic.

**Request:**
```bash
curl http://localhost:8083/metadata/custom-logger-advisor
```

**Response:**
```
Custom response: Why did the AI go to the gym? To work on its neural networks!
```

**Key Features:**
- **Custom Request Logging:** Logs entire prompt with custom formatting
- **Custom Response Logging:** Logs response content with custom formatting
- **Advisor Pattern:** Intercepts and processes request/response lifecycle
- **Pluggable Logic:** Custom advisor can implement any logging/processing strategy
- **Request/Response Hooks:** Two hook points for custom processing

**Implementation:**
```java
@GetMapping("/custom-logger-advisor")
public String testCustomLoggerAdvisor() {
    SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
            request -> {
                assert request != null;
                return "Custom request: " + request.prompt();
            },
            response -> {
                assert response != null;
                return "Custom response: " + response.getResult();
            },
            0  // Priority level (0 = highest)
    );
    return chatClient.prompt("Tell me a joke about computers")
            .advisors(customLoggerAdvisor)
            .call()
            .content();
}
```

**Advisor Pattern Benefits:**
- **Request Interception:** Process/log requests before sending to LLM
- **Response Interception:** Process/log responses after receiving from LLM
- **Cross-Cutting Concerns:** Logging, monitoring, tracing, metrics
- **Reusable Logic:** Define advisor once, use across multiple requests
- **Priority-Based:** Multiple advisors can be chained with priority ordering

**Custom Advisor Use Cases:**
- Request/response auditing
- Performance monitoring
- Token counting and cost tracking
- Security validation (content filtering)
- Request transformation/enrichment
- Response post-processing
- Error handling and retry logic

### Output Conversion Patterns Overview

Spring AI provides **multiple approaches** for converting unstructured LLM text into structured Java objects:

| Pattern              | Method                          | Output Type                       | Type Safety | Complexity | Best For          |
|----------------------|---------------------------------|-----------------------------------|-------------|------------|-------------------|
| **Manual Converter** | `.call().content()` + converter | `T`                               | ⭐⭐⭐         | Medium     | Explicit control  |
| **Entity Method**    | `.call().entity()`              | `T`                               | ⭐⭐⭐         | Low        | Direct conversion |
| **Response Entity**  | `.call().responseEntity()`      | `ResponseEntity<ChatResponse, T>` | ⭐⭐⭐         | Medium     | Metadata + data   |
| **Streaming**        | `.stream().content()`           | `Flux<T>`                         | ⭐⭐⭐         | Medium     | Real-time output  |

#### Pattern Comparison

**1. Manual Converter (Traditional)**
```java
// Requires explicit converter instantiation
ListOutputConverter converter = new ListOutputConverter();
Prompt prompt = new PromptTemplate(resource)
    .create(Map.of("format", converter.getFormat()));
List<String> result = converter.convert(
    chatClient.prompt(prompt).call().content()
);
```
- ✅ Full control over conversion process
- ✅ Can use `ListOutputConverter`, `MapOutputConverter`, `BeanOutputConverter`
- ❌ More boilerplate code
- **Use Case:** When you need specific converter behavior

**2. Native Entity Method (Recommended)**
```java
// Direct conversion without manual converter
List<SongDTO> result = chatClient.prompt(prompt)
    .call()
    .entity(new ParameterizedTypeReference<>() {});
```
- ✅ Minimal code, cleaner syntax
- ✅ Auto-detection of output format
- ✅ Spring AI handles conversion internally
- **Use Case:** Most common pattern for strongly-typed responses

**3. Response Entity (Advanced)**
```java
// Get both metadata and parsed entity
ResponseEntity<ChatResponse, List<SongDTO>> result = 
    chatClient.prompt(prompt).call()
        .responseEntity(new ParameterizedTypeReference<>() {});
```
- ✅ Access token counts and model metadata
- ✅ Full response information available
- ✅ Useful for monitoring and debugging
- **Use Case:** When you need response metadata for logging/analytics

**4. Streaming (Real-time)**
```java
// Non-blocking stream of response chunks
Flux<String> stream = chatClient.prompt(prompt)
    .stream()
    .content();
```
- ✅ Real-time token-by-token streaming
- ✅ Better perceived performance
- ✅ Handles large responses efficiently
- **Use Case:** Web applications, Server-Sent Events (SSE)

#### Creating Custom Output Types

Define a record or class for structured output:

```java
// Record-based DTO (recommended for modern Java)
public record SongDTO(String artist, String title, String description) {}

// Or traditional class
public class SongDTO {
    public String artist;
    public String title;
    public String description;
}
```

Then use with any conversion pattern:

```java
// Using entity method
List<SongDTO> songs = chatClient.prompt(prompt)
    .call()
    .entity(new ParameterizedTypeReference<List<SongDTO>>() {});

// Using manual converter
BeanOutputConverter<List<SongDTO>> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});
List<SongDTO> songs = converter.convert(content);
```

#### Adding New Output Converters

To extend with custom converter types:

```java
// Example: Custom converter for a specialized type
BeanOutputConverter<MyCustomClass> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

// Include format in prompt
Prompt prompt = new PromptTemplate(resource)
    .create(Map.of("format", converter.getFormat()));

// Parse response
MyCustomClass result = converter.convert(
    chatClient.prompt(prompt).call().content()
);
```

### Streaming & Real-Time Features

The `/parser/stream` endpoint demonstrates Spring AI's streaming capabilities:

**Key Benefits:**
- **Token-by-Token Streaming:** LLM output sent in real-time chunks
- **Reactive Architecture:** Built on Project Reactor's `Flux<T>` for non-blocking I/O
- **Server-Sent Events:** Compatible with HTML5 EventSource for browser clients
- **Reduced Latency:** Users see output faster vs. waiting for complete response
- **Resource Efficient:** Memory-friendly handling of large response streams

**When to Use Streaming:**
- Long-form content generation (articles, stories, code)
- Real-time chat applications
- Large data processing with incremental results
- Resource-constrained environments

### Future Endpoints

Beyond the current implementations, this codebase is ready to extend with:

- **POST /chat** - Multi-turn conversation state management with history
- **POST /summarize** - Document summarization with configurable length and style
- **POST /code-review** - Code analysis with detailed feedback and suggestions
- **POST /function-call** - Function calling / Tool invocation patterns
- **POST /image-generation** - Image synthesis endpoints (with compatible providers)
- **POST /embeddings** - Vector embeddings for semantic search and similarity
- **WebSocket /chat-ws** - Real-time bidirectional chat with persistent connections
- **POST /batch** - Batch processing with streaming progress updates
- **GET /stream/events** - Event streaming with multiple content types

## 🛠️ Development

### Recent Updates & New Features

#### V1.2 Release Updates:
- **Metadata Management:** Added custom metadata attachment to system and user prompts
- **Request Tracing:** Correlation IDs, user identification, priority levels, and source tracking
- **Default Context:** Pre-configured default system/user context with automatic metadata injection
- **Advisor Pattern:** Built-in and custom advisors for request/response logging and processing
- **Cross-Cutting Concerns:** Framework for logging, monitoring, metrics, auditing, and transformation
- **Enhanced MetadataController:** Three endpoints demonstrating metadata and advisor patterns

#### V1.1 Release Updates:
- **Enhanced Output Parsing:** Added three new output conversion methods (`.entity()`, `.responseEntity()`, `.stream()`)
- **Streaming Support:** Implemented Flux-based streaming for real-time response handling via `/parser/stream`
- **Response Metadata:** New `/parser/response-entity` endpoint provides access to token counts and model metadata
- **Native Type Conversion:** Simplified pattern using `.entity()` method for direct type conversion
- **Model Optimization:** Updated to `llama3.2:3b` (faster, lighter weight alternative to `llama3.1:8b`)
- **Improved Documentation:** Comprehensive endpoint documentation with pattern comparisons

#### New Endpoints Added:
**V1.2:**
1. **GET /metadata** - Custom metadata attachment to prompts with tracing capabilities
2. **GET /metadata/default** - Pre-configured default context and metadata management
3. **GET /metadata/custom-logger-advisor** - Custom advisor implementation example

**V1.1:**
1. **GET /parser/entity** - Native SpringAI entity conversion without manual converters
2. **GET /parser/response-entity** - Dual response with metadata and parsed data
3. **GET /parser/stream** - Real-time token streaming for large responses

### Build Commands

```bash
# Full build with tests
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Package without running
mvn clean package

# Run with Spring Boot plugin
mvn spring-boot:run

# Debug mode
mvnw spring-boot:run --debug
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=SpringAiOllamaApplicationTests

# Current test coverage
mvn clean verify -Pcode-coverage
```

**Note:** Current tests are minimal (context load only). Mock-based tests for ChatClient responses can be added as the project grows.

### Using HTTP Client Files

The `generated-http-requests.http` file provides a convenient way to test endpoints in IDEs like IntelliJ IDEA:

```http
### Get Joke
GET http://localhost:8083/
```

### Logging

The application uses **SLF4J with Logback** (default in Spring Boot):

```java
private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
// Log model responses at INFO level
LOGGER.info("Received response from model: {}", chatClientResponse);
```

To adjust log levels, add to `application.yaml`:
```yaml
logging:
  level:
    root: INFO
    org.springframework.ai: DEBUG
```

## 🐛 Troubleshooting

### Issue: Connection refused or timeout

**Symptom:** Requests hang or return connection errors

**Solution:**
1. Verify Ollama is running: `curl http://localhost:11434/api/tags`
2. Ensure correct port in `application.yaml`: `spring.ai.ollama.base-url: http://localhost:11434`
3. Check firewall settings

### Issue: Model not found error

**Symptom:** Runtime error about missing model

**Solution:**
1. List available models: `ollama list`
2. Pull missing model: `ollama pull llama3.1:8b`
3. Verify model name spelling in `application.yaml`

### Issue: Port 8083 already in use

**Symptom:** `Address already in use` error

**Solution:**
1. Change port in `application.yaml`:
   ```yaml
   server:
     port: 8084
   ```
2. Or kill existing process on port 8083

### Issue: Slow responses or memory issues

**Symptom:** First request is very slow, or system freezes

**Explanation:** LLM inference is resource-intensive. The first request loads the model into memory.

**Solution:**
- Ensure sufficient RAM (llama3.1:8b requires ~8GB)
- Use a smaller model: `ollama pull mistral` (4B parameters)
- Reduce concurrent requests

### Issue: Streaming endpoint returns no data or incomplete chunks

**Symptom:** `/parser/stream` returns empty or cuts off mid-response

**Solution:**
1. Ensure Spring Boot is configured for streaming: 
   ```yaml
   spring.mvc.async.request-timeout: 30000
   ```
2. Check that Ollama model supports streaming (llama3.2:3b and llama3.1:8b both support it)
3. Test with curl using `-N` flag to disable buffering:
   ```bash
   curl -N http://localhost:8083/parser/stream?artist=Adele
   ```
4. Verify browser client is using EventSource API correctly
5. Check Ollama logs for streaming errors

### Issue: Entity conversion fails with "Cannot deserialize"

**Symptom:** `/parser/entity` or `/parser/response-entity` throws deserialization error

**Solution:**
1. Ensure `SongDTO` fields match the JSON structure returned by LLM
2. Use `@JsonProperty` annotation if field names differ:
   ```java
   public record SongDTO(
       @JsonProperty("artist") String artist,
       @JsonProperty("title") String title,
       @JsonProperty("description") String description
   ) {}
   ```
3. Add jackson dependency if missing: `spring-boot-starter-json`
4. Check LLM response format matches expected JSON structure
5. Use `/parser` endpoint first to see actual LLM output for debugging

## 📚 Learning Resources

### Spring AI Documentation
- [Spring AI Official Guide](https://spring.io/projects/spring-ai)
- [ChatClient API Reference](https://docs.spring.io/spring-ai/reference/api/chat/chat-client.html)
- [Ollama Integration Guide](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)

### Ollama Documentation
- [Ollama GitHub](https://github.com/ollama/ollama)
- [Available Models](https://ollama.ai/library)

### Spring Boot Resources
- [Spring Boot 4.0.5 Reference](https://docs.spring.io/spring-boot/4.0.5/reference/)
- [Spring Web MVC Guide](https://docs.spring.io/spring-boot/4.0.5/reference/web/servlet.html)

## 📝 Version Information

| Component   | Version          | Status                  | Notes                          |
|-------------|------------------|-------------------------|--------------------------------|
| Spring Boot | 4.0.5            | Stable                  | Java 21 required               |
| Spring AI   | 2.0.0-M4         | Milestone (pre-release) | API subject to change          |
| Java        | 21               | Current LTS             | Min 21, recommended 21+        |
| Ollama      | Latest           | Self-managed            | Tested with llama3.2:3b        |
| llama3.2    | 3b (default)     | Configurable            | Lightweight, fast inference    |
| llama3.1    | 8b (optional)    | Alternative             | Better quality, higher compute |

### Release Notes - V1.2

**New Features:**
- ✨ **Metadata Management** - Custom metadata attachment to system/user prompts via `MetadataController`
- ✨ **Request/Response Tracing** - Built-in correlation IDs, user tracking, and priority levels
- ✨ **Default Chat Context** - Pre-configured system and user context with automatic metadata injection
- ✨ **Advisor Pattern** - SimpleLoggerAdvisor for custom request/response processing and logging
- ✨ **Custom Advisor Implementation** - Full control over logging/monitoring/transformation logic
- ✨ **Cross-Cutting Concerns** - Extensible advisor framework for logging, metrics, auditing

**Breaking Changes:** None - all previous endpoints maintained compatibility

**Deprecated:** None

### Release Notes - V1.1

**New Features:**
- ✨ **Native Entity Conversion** - Direct type conversion via `.entity()` method
- ✨ **Response Metadata Access** - Get token counts via `.responseEntity()`
- ✨ **Streaming Responses** - Real-time streaming via Flux with `/parser/stream`
- ✨ **Pattern Documentation** - Comprehensive comparison of 4 output conversion approaches
- ⚡ **Model Optimization** - Switched to llama3.2:3b for better performance


⚠️ **Note:** Spring AI 2.0.0-M4 is a milestone release. APIs may change in future versions. Refer to official Spring AI documentation for updates.

## 🚀 Next Steps

1. **Explore ChatClient capabilities:**
   - System prompts
   - Streaming responses
   - Tool calling / Function invocation

2. **Add more endpoints:**
   - Multi-turn conversations
   - Document processing
   - Code analysis

3. **Integrate persistence:**
   - Save conversations to database
   - Store user preferences

4. **Deploy to production:**
   - Docker containerization
   - Cloud deployment with managed LLM providers
   - API authentication and rate limiting

## 📄 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions welcome! Please follow existing code patterns and ensure tests pass.

---

**Repository maintained as educational material for Spring AI integration patterns.**
