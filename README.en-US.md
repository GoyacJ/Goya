# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-green.svg)](https://spring.io/projects/spring-cloud)

**Next-Generation Enterprise Microservices Framework**

English | [简体中文](./README.md)

[Quick Start](#quick-start) • [Core Features](#core-features) • [Project Structure](#project-structure) • [Documentation](#documentation) • [Contributing](./CONTRIBUTING.md)

</div>

---

## 📖 Introduction

Goya is an enterprise-grade microservices development framework built on **Spring Boot 4.0.1** and **Java 25**, featuring a separated frontend-backend architecture with complete capabilities for security authentication & authorization, AI integration, multi-tenancy, caching, message bus, and more.

### Architecture Components

- **Goya/** - Java Backend Framework (this directory)
- **goya-web-ui/** - Frontend lives in a separate repository (not included here)

## ✨ Core Features

### 🛡️ Security System
- **OAuth2.1 + OIDC** Authorization Server
- **Multiple Login Methods**: Username/Password / SMS OTP / Social Login (JustAuth)
- **Multi-Tenancy**: Isolated Issuer, JWK, Client Data
- **JWT + Opaque Token**: Access Token (JWT) + Refresh Token (Opaque)
- **SSO**: Single Sign-On across applications
- **Fine-Grained Permissions**: RBAC role-based access control

### 🤖 AI Capabilities
- **Spring AI 2.0.0-M1** Integration
- **LangChain4j 1.9.1** Orchestration
- **RAG** Retrieval Augmented Generation
- **MCP** (Model Context Protocol) Support
- **Multi-Model Support**: OpenAI, Qwen, Local Models

### 🚀 Microservices Components
- **Cache**: Redis (Distributed Lock / Bloom Filter / Delayed Queue / Rate Limiter)
- **Message Bus**: Kafka / RabbitMQ
- **Database**: MyBatis Plus Enhancement
- **Object Storage**: Aliyun OSS / MinIO / AWS S3
- **CAPTCHA**: Arithmetic / Slider / Jigsaw Types
- **Social Login**: WeChat Mini Program / Third-Party Platforms
- **Audit Logging**: Operation Logs / Audit Trails

### 🏗️ Tech Stack
- **Java 25** + **Spring Boot 4.0.1** + **Spring Cloud 2025.1.0**
- **Spring Security 7** + **Spring Authorization Server**
- **MyBatis Plus 3.5.15**
- **Redisson 4.0.0**
- **MapStruct 1.6.3** + **Lombok 1.18.42**
- **Nacos 3.1.1** Service Discovery + Config Center

## 📦 Project Structure

```
Goya/
├── bom/                           # Dependency Management BOM
├── component/                     # Common Components
│   ├── component-framework/       # Framework Foundation (Aggregation)
│   │   ├── framework-core/        # Core Utilities
│   │   ├── framework-common/      # Common Components
│   │   ├── framework-masker/      # Data Masking
│   │   ├── framework-crypto/      # Encryption/Decryption
│   │   ├── framework-cache/       # Cache Abstraction
│   │   ├── framework-bus/         # Message Bus Abstraction
│   │   ├── framework-log/         # Logging Enhancement
│   │   ├── framework-oss/         # Object Storage Abstraction
│   │   ├── framework-servlet/     # Servlet Enhancement
│   │   └── framework-boot-starter/# Auto Configuration Starter
│   ├── component-redis/           # Redis Implementation (Redisson)
│   ├── component-kafka/           # Kafka Message Bus
│   ├── component-rabbitmq/        # RabbitMQ Message Bus
│   ├── component-mybatisplus/     # MyBatis Plus Enhancement
│   ├── component-captcha/         # CAPTCHA
│   ├── component-security/        # Security Module
│   │   ├── security-core/         # Core Domain Models
│   │   ├── security-authentication/ # Authentication
│   │   ├── security-authorization/  # Resource Server
│   │   └── security-oauth2/       # Authorization Server
│   ├── component-social/          # Social Login
│   ├── component-oss-aliyun/      # Aliyun OSS Implementation
│   ├── component-oss-s3/          # AWS S3 Implementation
│   └── component-oss-minio/       # MinIO Implementation
├── ai/                            # AI Module
│   ├── ai-spring/                 # Spring AI Integration
│   ├── ai-model/                  # Model Management
│   ├── ai-rag/                    # RAG Implementation
│   ├── ai-mcp/                    # MCP Protocol
│   └── ai-video/                  # Video Processing
├── platform/                      # Platform Applications
│   ├── platform-monolith/         # Monolithic App
│   └── platform-distributed/      # Distributed App
├── cloud/                         # Cloud Native Support
├── deploy/                        # Deployment Configs
│   ├── docker/                    # Docker Compose
│   └── maven/                     # Maven Config
├── docs/                          # Documentation directory (currently pending in this branch)
└── logs/                          # Local runtime logs
```

## 🚀 Quick Start

### Requirements

- **JDK 25+**
- **Maven 3.9+**
- **Node.js 20+** & **pnpm 10+**
- **Docker** (Optional)
- **Redis 7+** (For cache and session)
- **MySQL 8+** or **PostgreSQL 15+**

### Backend Setup

```bash
# Install dependencies
mvn clean install -DskipTests

# Start auth server
cd platform/platform-monolith
mvn spring-boot:run
```

Access: `http://localhost:8101`

### Frontend Note

```bash
# Frontend code is not in this repository.
# Use the dedicated frontend repository for setup and run.
```

### Configuration Templates

```bash
# Docker infrastructure env template
cp deploy/docker/docker-compose/basic/.env.example deploy/docker/docker-compose/basic/.env

# Maven private repository settings template (optional)
cp deploy/maven/conf/settings.xml.example deploy/maven/conf/settings.xml

# Application config templates
cp platform/platform-monolith/src/main/resources/application-dev.example.yml platform/platform-monolith/src/main/resources/application-dev.yml
cp platform/platform-monolith/src/main/resources/application-prod.example.yml platform/platform-monolith/src/main/resources/application-prod.yml
```

### Docker Quick Start

```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```

## 🎯 Core Modules

### Framework Foundation (component-framework)

| Module | Description |
|--------|-------------|
| framework-core | Core utilities, base definitions, response wrapper |
| framework-common | Common components, utility classes |
| framework-masker | Data masking (phone, ID card, email, etc.) |
| framework-crypto | Encryption/Decryption (AES, RSA, SM4, etc.) |
| framework-cache | Cache abstraction layer |
| framework-bus | Message bus abstraction |
| framework-log | Logging enhancement, audit logs |
| framework-oss | Object storage abstraction |
| framework-servlet | Servlet enhancement, XSS protection |
| framework-boot-starter | Auto configuration starter |

### Redis Module (component-redis)

- **Cache Service**: Unified cache operations
- **Distributed Lock**: Reentrant, Fair, Read/Write locks
- **Bloom Filter**: Prevent cache penetration
- **Delayed Queue**: Reliable delayed queue
- **Rate Limiter**: Token bucket based rate limiting
- **Topic Message**: Pub/Sub pattern

### Security Module (component-security)

- **security-core**: Core domain models, SPI interfaces
- **security-authentication**: Multiple auth methods
- **security-authorization**: Resource server (JWT)
- **security-oauth2**: Authorization server (OAuth2.1 + OIDC)

## 📚 Documentation

The `docs/` directory is currently being rebuilt in this branch. Available references:

- [README](./README.md)
- [README.en-US](./README.en-US.md)
- [Contributing Guide](./CONTRIBUTING.md)
- [AI Assistant Guide](./.cursor/AI_ASSISTANT_GUIDE.md)

## 🤝 Contributing

We welcome all contributions! Please read the [Contributing Guide](./CONTRIBUTING.md).

## 📄 License

This project is licensed under the [Apache License 2.0](./LICENSE).

## 🔗 Links

- Website: https://www.ysmjjsy.com
- GitHub: https://github.com/GoyaDo/Goya
- Issues: https://github.com/GoyaDo/Goya/issues

---

<div align="center">

**Built with ❤️ by Goya Team**

</div>
