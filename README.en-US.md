# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-green.svg)](https://spring.io/projects/spring-cloud)

**Next-Generation Enterprise Microservices Framework**

English | [简体中文](./README.md)

[Quick Start](#quick-start) • [Core Features](#core-features) • [Architecture](#architecture) • [Documentation](#documentation) • [Contributing](./CONTRIBUTING.md)

</div>

---

## 📖 Introduction

Goya is an enterprise-grade microservices development framework built on **Spring Boot 4.0.1** and **Java 25**, featuring a separated frontend-backend architecture with complete capabilities for security authentication & authorization, AI integration, multi-tenancy, caching, message bus, and more.

### Architecture Components

- **Goya/** - Java Backend Framework
- **Goya-Web/** - Vue 3 Admin System

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
- **Cache**: Caffeine / Redis / Multi-Level Cache
- **Message Bus**: Kafka / Stream Abstraction
- **Database**: MyBatis Plus / JPA Dual Engine
- **Object Storage**: Aliyun OSS / MinIO / S3
- **CAPTCHA**: Arithmetic / Slider / Jigsaw Types
- **Social Login**: WeChat Mini Program / Third-Party Platforms
- **Audit Logging**: Operation Logs / Audit Trails

### 🏗️ Tech Stack
- **Java 25** + **Spring Boot 4.0.1** + **Spring Cloud 2025.1.0**
- **Spring Security 7** + **Spring Authorization Server**
- **MyBatis Plus 3.5.15** / **Spring Data JPA**
- **Redisson 4.0.0** + **Caffeine**
- **MapStruct 1.6.3** + **Lombok 1.18.42**
- **Nacos 3.1.1** Service Discovery + Config Center

## 📦 Project Structure

```
Goya/
├── Goya/                        # Backend Framework
│   ├── bom/                     # Dependency Management BOM
│   ├── component/               # Common Components
│   │   ├── component-core/      # Core Utilities
│   │   ├── component-framework/ # Framework Foundation
│   │   ├── component-web/       # Web Enhancements
│   │   ├── component-security/  # Security Module
│   │   │   ├── security-core/           # Core Domain Models
│   │   │   ├── security-authentication/ # Authentication
│   │   │   ├── security-authorization/  # Resource Server
│   │   │   └── security-oauth2/         # Authorization Server
│   │   ├── component-cache/     # Cache Module
│   │   ├── component-bus/       # Message Bus
│   │   ├── component-database/  # Database Enhancement
│   │   ├── component-oss/       # Object Storage
│   │   ├── component-captcha/   # CAPTCHA
│   │   ├── component-social/    # Social Login
│   │   └── component-log/       # Logging Module
│   ├── ai/                      # AI Module
│   │   ├── ai-spring/           # Spring AI Integration
│   │   ├── ai-model/            # Model Management
│   │   ├── ai-rag/              # RAG Implementation
│   │   ├── ai-mcp/              # MCP Protocol
│   │   └── ai-video/            # Video Processing
│   ├── platform/                # Platform Applications
│   │   ├── platform-monolith/   # Monolithic App
│   │   └── platform-distributed/# Distributed App
│   ├── cloud/                   # Cloud Native Support
│   └── doc/                     # Documentation
│       ├── docker/              # Docker Compose
│       ├── maven/               # Maven Config
│       └── security/            # Security Solution Docs
└── Goya-Web/                    # Frontend Admin System (Vue 3)
    ├── apps/                    # Applications
    │   ├── web-antd/            # Ant Design Vue Version
    │   ├── web-ele/             # Element Plus Version
    │   ├── web-naive/           # Naive UI Version
    │   └── backend-mock/        # Mock Service
    ├── packages/                # Shared Packages
    │   ├── @core/               # Core Package
    │   ├── effects/             # Side Effects
    │   ├── stores/              # State Management
    │   ├── types/               # Type Definitions
    │   └── utils/               # Utility Functions
    └── internal/                # Internal Tools
        ├── lint-configs/        # Lint Configurations
        ├── vite-config/         # Vite Config
        └── tsconfig/            # TypeScript Config
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
cd Goya

# Install dependencies
mvn clean install -DskipTests

# Start auth server
cd platform/platform-monolith/auth-server
mvn spring-boot:run
```

Access: `http://localhost:8080`

### Frontend Setup

```bash
cd Goya-Web

# Install dependencies
pnpm install

# Start dev server (Ant Design Vue)
pnpm dev:antd

# Or other versions
pnpm dev:ele     # Element Plus
pnpm dev:naive   # Naive UI
```

Access: `http://localhost:5555`

### Docker Quick Start

```bash
cd Goya/doc/docker/docker-compose/basic
docker-compose up -d
```

Includes: MySQL, Redis, MongoDB, Nacos, and other infrastructure.

## 🎯 Core Modules

### Security Module (component-security)

Complete authentication and authorization solution built on **Spring Security 7** and **Spring Authorization Server**.

**Key Features**:
- OAuth2.1 Authorization Server (Authorization Code + PKCE)
- OIDC Provider (with Discovery)
- Resource Server (JWT Validation + Blacklist)
- Multiple Login Methods (Password / SMS / Social)
- Multi-Tenant Issuer Isolation
- Token Management (JWT Access Token + Opaque Refresh Token)

**Documentation**: [Enterprise Auth Solution](./Goya/doc/security/enterprise-auth-solution.md)

### AI Module (ai/)

Integrates **Spring AI** and **LangChain4j** for out-of-the-box AI capabilities.

**Key Features**:
- Unified Multi-Model Interface
- RAG Retrieval Augmented Generation
- Prompt Management
- Function Calling
- MCP Protocol Support
- Video AI Analysis (FFmpeg + OpenCV)

### Cache Module (component-cache)

Multi-level caching solution with Caffeine local cache + Redis distributed cache.

**Key Features**:
- Unified Cache Interface
- Automatic Cache Sync (Redis Pub/Sub)
- Cache Warm-up and Invalidation
- Protection against Cache Penetration/Breakdown/Avalanche

### Database Module (component-database)

Dual-engine support for MyBatis Plus and JPA.

**Key Features**:
- Dynamic DataSource Switching
- Multi-Tenant Data Isolation
- Auto-fill Audit Fields
- SQL Monitoring (P6Spy)
- Support for MySQL, PostgreSQL, OpenGauss, TDengine, etc.

## 📚 Documentation

- [Architecture Design](./docs/architecture/overview.md)
- [Quick Start](./docs/guides/quick-start.md)
- [Development Guide](./docs/guides/development.md)
- [Deployment Guide](./docs/guides/deployment.md)
- [API Documentation](./docs/api/rest-api.md)
- [Product Requirements](./docs/requirements/product-requirements.md)
- [Technical Requirements](./docs/requirements/technical-requirements.md)
- [Roadmap](./docs/progress/roadmap.md)

## 🤝 Contributing

We welcome all forms of contributions! Please read the [Contributing Guide](./CONTRIBUTING.md) for details.

### Contributors

Thanks to all developers who contributed to Goya!

## 📄 License

This project is licensed under the [Apache License 2.0](./Goya/LICENSE).

## 🔗 Links

- Website: https://www.ysmjjsy.com
- GitHub: https://github.com/GoyaDo/Goya
- Issues: https://github.com/GoyaDo/Goya/issues

## ⭐ Star History

If this project helps you, please give it a Star ⭐

---

<div align="center">

**Built with ❤️ by Goya Team**

</div>
