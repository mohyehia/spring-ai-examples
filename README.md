# spring-ai-examples

A Spring Boot repository demonstrating **AI integration patterns** using [Spring AI](https://spring.io/projects/spring-ai) with [Ollama](https://ollama.ai/), a local LLM orchestration framework.

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Modules](#-modules)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)

## 🎯 Project Overview

This repository contains **Spring AI integration examples** demonstrating practical patterns for building AI-powered REST applications using **Spring Boot 4.0.5** and **Spring AI 2.0.0-M4** with **Ollama** as the local LLM provider.

## 📦 Modules

### 1. **spring-ai-ollama** - Core AI Integration Module
Comprehensive Spring AI patterns for LLM interactions, including chat, prompts, output parsing, memory management, and multimodal support.

**Key Features:**
- ✨ Simple and advanced ChatClient patterns
- ✨ Prompt templates and system messages
- ✨ Output parsing (List, Map, DTO conversions)
- ✨ Multi-turn conversation memory management
- ✨ Multimodal image analysis
- ✨ Streaming responses
- ✨ Custom metadata and advisor patterns

**📍 Port:** 8083  
**📚 Full Documentation:** See [spring-ai-ollama/README.md](spring-ai-ollama/README.md)

---

### 2. **spring-ai-redis-chat-memory** - Redis Distributed Chat Memory
Advanced chat memory management using Redis for persistent, distributed conversation storage across multiple instances.

**Key Features:**
- ✨ In-memory and Redis-backed chat implementations
- ✨ Persistent conversation history with TTL
- ✨ Multi-user conversation tracking
- ✨ Distributed chat memory repository
- ✨ Conversation retrieval and history

**📍 Port:** 8084  
**📍 Redis:** localhost:6379 (v7.0+)  
**📚 Full Documentation:** See [spring-ai-redis-chat-memory/README.md](spring-ai-redis-chat-memory/README.md)

## 🔧 Prerequisites

### System Requirements
- **Java 21+** installed
- **Maven 3.8+** installed
- **Ollama** running on localhost:11434
- **Redis 7.0+** (required only for `spring-ai-redis-chat-memory` module)

### Quick Setup

1. **Install & Start Ollama:**
   ```bash
   # Install from ollama.ai
   # Start Ollama service (default port: 11434)
   ollama pull gemma4:e2b      # For multimodal support
   ollama pull llama3.2:3b     # Lightweight alternative
   ```

2. **Install & Start Redis** (optional, only if using redis-chat-memory):
   ```bash
   # Ensure Redis 7.0 or higher is running on localhost:6379
   ```

3. **Verify Services:**
   ```bash
   curl http://localhost:11434/api/tags   # Check Ollama
   redis-cli ping                          # Check Redis (if using redis-memory)
   ```

## 🚀 Quick Start

### Option 1: Run spring-ai-ollama Module
```bash
cd spring-ai-ollama
mvn clean install
mvn spring-boot:run
# Access at http://localhost:8083
```

### Option 2: Run spring-ai-redis-chat-memory Module
```bash
cd spring-ai-redis-chat-memory
mvn clean install
mvn spring-boot:run
# Access at http://localhost:8084
```

### Option 3: Run Both Modules
```bash
mvn clean install -DskipTests     # Build all modules
# Then run each in separate terminals
```

---

**⚠️ For detailed setup, configuration, and API documentation, see the module-specific README.md files above.**

## 📁 Project Structure

```
spring-ai-examples/
├── README.md                                    # This file (overview only)
├── AGENTS.md                                    # AI developer guide
├── LICENSE
└── Modules:
    ├── spring-ai-ollama/                        # Core AI integration
    │   ├── README.md                            # Full documentation
    │   ├── pom.xml
    │   └── src/
    │
    └── spring-ai-redis-chat-memory/             # Redis-based memory
        ├── README.md                            # Full documentation
        ├── pom.xml
        └── src/
```

## 📚 Module-Specific Documentation

Each module provides comprehensive documentation with detailed API endpoints, configuration options, and troubleshooting guides:

| Module                          | Documentation                                      | Focus Areas                                                    |
|---------------------------------|----------------------------------------------------|----------------------------------------------------------------|
| **spring-ai-ollama**            | [README.md](spring-ai-ollama/README.md)            | ChatClient, Output Parsing, Memory, Multimodal, Streaming      |
| **spring-ai-redis-chat-memory** | [README.md](spring-ai-redis-chat-memory/README.md) | Redis Integration, Persistent Memory, Distributed Architecture |

## 🎓 Developer Resources

- **[AGENTS.md](AGENTS.md)** - AI developer guide for coding patterns and architecture overview
- **[Spring AI Official](https://spring.io/projects/spring-ai)** - Official Spring AI documentation
- **[Ollama GitHub](https://github.com/ollama/ollama)** - Ollama LLM orchestration framework
- **[Redis Documentation](https://redis.io)** - Redis data structures and configuration
- **[Spring Boot 4.0.5](https://docs.spring.io/spring-boot/4.0.5)** - Spring Boot reference guide

## 🏗️ Architecture Highlights

```
┌─────────────────────────────────────────────────────────┐
│          Spring Boot 4.0.5 REST APIs                    │
│                                                         │
│  Port 8083                              Port 8084       │
│  ┌──────────────────────┐         ┌──────────────────┐  │
│  │ spring-ai-ollama     │         │ spring-ai-redis  │  │
│  │                      │         │ -chat-memory     │  │
│  │ • ChatClient         │         │                  │  │
│  │ • Prompt Templates   │         │ • In-Memory      │  │
│  │ • Output Parsing     │         │ • Redis Backed   │  │
│  │ • Multimodal         │         │ • Distributed    │  │
│  │ • Memory Mgmt        │         │ • Persistent     │  │
│  └──────────┬───────────┘         └────────┬─────────┘  │
└─────────────┼────────────────────────────┼──────────────┘
              │                            │
        ┌─────▼──────┐              ┌──────▼────────┐
        │   Ollama   │              │    Redis      │
        │ Port 11434 │              │  Port 6379    │
        └────────────┘              └───────────────┘
```

## 🚀 Getting Started

### 1. **Prerequisites Check**
Ensure all required services are running before starting the application:

```bash
# Check Ollama
curl http://localhost:11434/api/tags

# Check Redis (for redis-chat-memory module only)
redis-cli ping
```

### 2. **Choose Your Module**

- **New to Spring AI?** Start with [spring-ai-ollama](spring-ai-ollama/README.md)
- **Need persistent memory?** Use [spring-ai-redis-chat-memory](spring-ai-redis-chat-memory/README.md)

### 3. **Build & Run**

Each module can be built and run independently. Refer to the module-specific README for detailed instructions.

## 📊 Quick Comparison

| Feature            | spring-ai-ollama | spring-ai-redis-chat-memory |
|--------------------|------------------|-----------------------------|
| **Memory Type**    | In-memory        | Redis (persistent)          |
| **Multi-Instance** | ❌ No             | ✅ Yes                       |
| **Scalability**    | Single instance  | Distributed                 |
| **API Endpoints**  | 22+              | 1+                          |
| **Image Support**  | ✅ Multimodal     | ⏳ Planned                   |
| **TTL Support**    | ❌ No             | ✅ Yes (24h default)         |

## 💡 Use Cases

**spring-ai-ollama:**
- Learning Spring AI patterns
- Prototyping AI applications
- Single-instance deployments
- Image/document analysis
- Complex output parsing

**spring-ai-redis-chat-memory:**
- Multi-user chat applications
- Distributed deployments
- Persistent conversation history
- Microservices with shared memory
- Production scalability

## ⚙️ Technology Stack

| Component   | Version  | Purpose                            |
|-------------|----------|------------------------------------|
| Java        | 21       | Runtime environment                |
| Spring Boot | 4.0.5    | Application framework              |
| Spring AI   | 2.0.0-M4 | AI abstraction layer               |
| Ollama      | Latest   | Local LLM orchestration            |
| Redis       | 7.0+     | Distributed chat memory (optional) |
| Maven       | 3.8+     | Build tool                         |

## 📝 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please ensure:
- ✅ Code follows existing patterns
- ✅ Tests pass before submitting
- ✅ Documentation is updated
- ✅ Commit messages are clear

---

**Repository maintained as educational material for Spring AI integration patterns with Ollama.**
