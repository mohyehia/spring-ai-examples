# spring-ai-tools

A Spring Boot 4.0.5 demonstration of **AI-powered tool calling (function calling) with Spring AI**. This module integrates [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/) for local LLM orchestration, showcasing advanced patterns for **tool calling**, **agentic workflows**, and **dynamic function execution** managed by LLMs.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Technologies](#key-technologies)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Tool Implementations](#tool-implementations)
- [Configuration](#configuration)
- [Development](#development)
- [Project Structure](#project-structure)
- [Common Patterns](#common-patterns)
- [Extension Points](#extension-points)
- [References](#references)

## Overview

This module showcases **Spring AI Tool Calling** - where LLMs intelligently invoke external tools to answer complex questions. Perfect for:

- ✨ **Agentic AI systems** - LLM-driven tool selection and orchestration
- ✨ **Extended capabilities** - Augment LLMs with real-world data/services
- ✨ **Reasoning pipelines** - Capture LLM reasoning before execution

**Key Concepts:**
- **ToolCallAdvisor** - Transparent advisor-based tool orchestration
- **Tool Registration** - Register multiple tools for dynamic invocation
- **Augmented Reasoning** - Capture agent thinking via `AugmentedToolCallbackProvider`

## Key Technologies

| Technology      | Version  | Purpose                                           |
|-----------------|----------|---------------------------------------------------|
| **Java**        | 21       | Language runtime                                  |
| **Spring Boot** | 4.0.5    | Application framework                             |
| **Spring AI**   | 2.0.0-M4 | AI abstraction with tool calling support          |
| **Ollama**      | Latest   | Local LLM orchestration (llama3.2:3b)             |
| **Maven**       | Latest   | Build & dependency management                     |

## Architecture

### Component Diagram

```
┌──────────────────────────────────────────────────────┐
│        Spring Boot Application (Port 8086)           │
│                                                      │
│  ┌────────────────────────────────────────────────┐  │
│  │     ChatController                             │  │
│  │  • GET /?question=... - Tool-driven chat       │  │
│  │  • GET /{customerNumber} - Tool + reasoning    │  │
│  └────────────┬───────────────────────────────────┘  │
│               │                                      │
│  ┌────────────▼──────────────────────────────────┐   │
│  │     Spring AI ChatClient                      │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────────┐  │   │
│  │  │ Advisors:                               │  │   │
│  │  │ • ToolCallAdvisor (tool orchestration)  │  │   │
│  │  │ • SimpleLoggerAdvisor (logging)         │  │   │
│  │  └─────────────────────────────────────────┘  │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────────┐  │   │
│  │  │ Tools:                                  │  │   │
│  │  │ • DateTimeTools (2 tools)               │  │   │
│  │  │ • WeatherTools (1 tool)                 │  │   │
│  │  │ • CustomerTools (1 tool)                │  │   │
│  │  └─────────────────────────────────────────┘  │   │
│  └────────────┬──────────────────────────────────┘   │
└───────────────┼──────────────────────────────────────┘
                │
        ┌───────▼──────────┐
        │  Ollama          │
        │  localhost:11434 │
        │  (llama3.2:3b)   │
        └──────────────────┘
```

### Data Flow

**Tool Calling:** Question → ChatClient registers tools → LLM analyzes → ToolCallAdvisor invokes → Tool executes → LLM formulates response

**Augmented Reasoning:** Question → AugmentedToolCallbackProvider captures reasoning → Tool executes → Result returned with reasoning trail

## Quick Start

### Prerequisites

1. **Java 21+** installed
2. **Maven 3.8+** installed
3. **Ollama** running on localhost:11434
4. **Ollama model installed**: `ollama pull llama3.2:3b`

### Setup Steps

```bash
# 1. Navigate to module directory
cd spring-ai-tools

# 2. Build the application
mvn clean install

# 3. Run the application
mvn spring-boot:run
```

Application starts on **http://localhost:8086**

### Test Endpoints

```bash
# Simple tool calling (LLM decides which tools to use)
curl "http://localhost:8086/?question=What%20time%20is%20it%20and%20what%27s%20the%20weather%20in%20Paris?"

# Tool calling with augmented reasoning
curl "http://localhost:8086/123"
```

## API Endpoints

### 1. Tool-Driven Chat Endpoint

**Request:**
```http
GET /?question=What time is it and what's the weather in Paris?
```

**Parameters:**
- `question` (query) - User question; LLM decides which tools to call

**Description:** Chat endpoint where the LLM analyzes the question and dynamically invokes appropriate tools. The ToolCallAdvisor orchestrates tool execution transparently within the advisor chain.

**Response:**
```
The current time is 2024-04-24T14:30:45.123456+02:00[Europe/Paris].
The current temperature in Paris is 18.5 degrees!
```

**Implementation Details:**
- Uses `ToolCallAdvisor` for framework-managed tool orchestration
- Tools registered: DateTimeTools, WeatherTools, CustomerTools
- LLM has full context and makes intelligent decisions
- Advisors: `SimpleLoggerAdvisor`, `ToolCallAdvisor`

**Tool Selection:** "What's the weather?" → `WeatherTools.getCurrentWeather()` | "What time?" → `DateTimeTools.getCurrentDateTime()` | "Customer 123?" → `CustomerTools.findByCustomerNumber()`

### 2. Tool Calling with Agent Reasoning Endpoint

**Request:**
```http
GET /{customerNumber}
```

**Parameters:**
- `customerNumber` (path) - Customer identifier (123, 456, 789)

**Description:** Endpoint demonstrating advanced tool calling with **captured reasoning**. Uses `AugmentedToolCallbackProvider` to capture the LLM's reasoning before tool execution. This allows observing why the LLM chose specific tools.

**Response:**
```
Tool: getCustomerInfo | Reasoning: I need to retrieve customer information for the provided customer number
Customer information retrieved successfully:
- Customer Number: 123
- Name: John Doe
- Address: 123 Main St
```

**Implementation Details:**
- Captures reasoning via `AugmentedToolCallbackProvider<AgentThinking>`
- `AgentThinking` record: reasoning (`innerThought`) + confidence level
- Reasoning logged at INFO level before removal
- `returnDirect = true` - Tool result becomes final response
- Hybrid approach: observes reasoning internally, returns clean result

**Customer Data:**
```
123 → John Doe, 123 Main St
456 → Jane Smith, 456 Elm St
789 → Bob Johnson, 789 Oak St
```

## Tool Implementations

| Tool Class        | Methods                                     | Purpose                                   |
|-------------------|---------------------------------------------|-------------------------------------------|
| **DateTimeTools** | `getCurrentDateTime()`, `setAlarm(minutes)` | Time/alarm functionality                  |
| **WeatherTools**  | `getCurrentWeather(location)`               | Simulated weather (0-40°C)                |
| **CustomerTools** | `findByCustomerNumber(id)`                  | In-memory customer lookup (123, 456, 789) |

## Configuration

### Application YAML (`application.yaml`)

```yaml
spring:
  application:
    name: spring-ai-tools
  ai:
    ollama:
      chat:
        model: "llama3.2:3b"       # Lightweight text model

server:
  port: 8086                        # Distinct from other modules

logging:
  level:
    root: info
    org.springframework.ai: debug                      # AI framework logs
    org.springframework.ai.chat.client.advisor: debug  # Tool advisor logs
```

### Environment Variables (Override YAML)

```bash
# Override Ollama model
export SPRING_AI_OLLAMA_CHAT_MODEL=llama3.1:8b

# Override server port
export SERVER_PORT=9086
```

## Development

### Building

```bash
# Full build with tests
mvn clean install

# Fast build (skip tests)
mvn clean install -DskipTests

# Build specific module from root
mvn -pl spring-ai-tools clean install
```

### Running

```bash
# Development mode with auto-reload
mvn spring-boot:run

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9086"
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SpringAiToolsApplicationTests
```

### Debugging

Enable detailed logging:

```yaml
logging:
  level:
    com.moh.yehia.tools: debug
    org.springframework.ai: debug
    org.springframework.ai.chat.client: debug
```

## Project Structure

```
spring-ai-tools/
├── src/
│   ├── main/
│   │   ├── java/com/moh/yehia/tools/
│   │   │   ├── SpringAiToolsApplication.java    # Entry point
│   │   │   ├── ChatController.java              # 2 tool-calling endpoints
│   │   │   ├── DateTimeTools.java               # DateTime tools
│   │   │   ├── WeatherTools.java                # Weather tool
│   │   │   └── CustomerTools.java               # Customer lookup tool
│   │   └── resources/
│   │       └── application.yaml                 # Spring configuration
│   └── test/
│       └── java/...
├── pom.xml                                      # Maven configuration
├── generated-requests.http                      # HTTP client test file
├── README.md                                    # This file
└── HELP.md                                      # Spring Boot help
```

## Common Patterns

### Pattern 1: Basic Tool Calling

```java
@GetMapping
public String chat(@RequestParam String question) {
    var toolCallAdvisor = ToolCallAdvisor.builder()
            .toolCallingManager(toolCallingManager)
            .build();

    return chatClient.prompt()
            .advisors(new SimpleLoggerAdvisor(), toolCallAdvisor)
            .tools(new DateTimeTools(), new WeatherTools(), new CustomerTools())
            .user(question)
            .call()
            .content();
}
```

**Key Points:**
- ToolCallAdvisor manages tool execution transparently
- Multiple tool classes registered with `.tools()`
- LLM analyzes question and invokes appropriate tools
- SimpleLoggerAdvisor provides observability

### Pattern 2: Tool Calling with Augmented Reasoning

```java
@GetMapping("/{customerNumber}")
String getCustomerInfo(@PathVariable String customerNumber) {
    var toolCallAdvisor = ToolCallAdvisor.builder()
            .toolCallingManager(toolCallingManager)
            .build();
    
    AugmentedToolCallbackProvider<AgentThinking> toolCallbackProvider = 
        AugmentedToolCallbackProvider.<AgentThinking>builder()
            .toolObject(new CustomerTools())
            .argumentType(AgentThinking.class)
            .argumentConsumer(event -> {
                AgentThinking agentThinking = event.arguments();
                log.info("Tool: {} | Reasoning: {}", 
                    event.toolDefinition().name(), 
                    agentThinking.innerThought());
            })
            .removeExtraArgumentsAfterProcessing(true)
            .build();
    
    return chatClient.prompt()
            .user("Retrieve the customer information for the customer number: %s".formatted(customerNumber))
            .advisors(new SimpleLoggerAdvisor(), toolCallAdvisor)
            .toolCallbacks(toolCallbackProvider)
            .call()
            .content();
}
```

**Key Points:**
- Capture agent reasoning via `AugmentedToolCallbackProvider`
- `AgentThinking` record holds reasoning + confidence
- Arguments consumed for logging/observability before removal
- `removeExtraArgumentsAfterProcessing(true)` cleans output

### Pattern 3: Tool Definition with Parameters

```java
@Tool(description = "Get the current weather information based on the given country")
String getCurrentWeather(
    @ToolParam(description = "country or city to retrieve the weather info for") 
    String countryOrCity) {
    return "The current temperature is %s degrees!".formatted(Math.random() * 40);
}
```

**Key Points:**
- `@Tool` annotation registers method as callable tool
- `@ToolParam` provides parameter documentation for LLM
- Description guides LLM on when/how to call tool
- Parameters extracted from user input by LLM

## Extension Points

1. **Add new tools** - New class with `@Tool` methods
2. **Tool chaining** - Multiple tools in sequence
3. **Memory integration** - Combine with ChatMemoryAdvisor
4. **Custom reasoning** - Extend AgentThinking record
5. **Tool metrics** - Track usage and performance
6. **Error handling** - Wrap tool calls with recovery logic
7. **Dynamic registration** - Load tools from configuration
8. **Tool validation** - Pre/post-processing
9. **Tool versioning** - Multiple tool versions
10. **Service integration** - Connect to real APIs/databases

## References

- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [Spring AI Tool Calling](https://docs.spring.io/spring-ai/reference/api/chatclient.html#function-calling)
- [Spring AI ToolCallAdvisor](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_tool_call_advisor)
- [Ollama GitHub](https://github.com/ollama/ollama)
- [Spring AI Tool Annotation](https://docs.spring.io/spring-ai/reference/api/tool-annotation.html)

