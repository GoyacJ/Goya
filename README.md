# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-green.svg)](https://spring.io/projects/spring-cloud)

**新一代企业级微服务开发框架**

[English](./README.en-US.md) | 简体中文

[快速开始](#快速开始) • [核心特性](#核心特性) • [架构设计](#架构设计) • [文档](#文档) • [贡献指南](./CONTRIBUTING.md)

</div>

---

## 📖 项目简介

Goya 是一个基于 **Spring Boot 4.0.1** 和 **Java 25** 构建的企业级微服务开发框架，采用前后端分离架构，提供完整的安全认证授权、AI 集成、多租户、缓存、消息总线等企业级能力。

### 架构组成

- **Goya/** - Java 后端框架（本目录）
- **goya-web-ui/** - Vue 3 前端管理系统

## ✨ 核心特性

### 🛡️ 安全体系
- **OAuth2.1 + OIDC** 认证授权服务器
- **多登录方式**：用户名密码 / 短信 OTP / 第三方登录（JustAuth）
- **多租户隔离**：独立 Issuer、JWK、客户端数据
- **JWT + Opaque Token**：Access Token (JWT) + Refresh Token (Opaque)
- **SSO 单点登录**：跨应用统一认证
- **细粒度权限控制**：RBAC 角色权限模型

### 🤖 AI 能力
- **Spring AI 2.0.0-M1** 集成
- **LangChain4j 1.9.1** 编排
- **RAG** 检索增强生成
- **MCP** (Model Context Protocol) 支持
- **多模型支持**：OpenAI、通义千问、本地模型

### 🚀 微服务组件
- **缓存**：Redis（分布式锁、布隆过滤器、延迟队列、限流器）
- **消息总线**：Kafka / RabbitMQ
- **数据库**：MyBatis Plus 增强（多租户 + SRA 数据权限）
- **对象存储**：阿里云 OSS / MinIO / AWS S3
- **验证码**：算术 / 滑块 / 拼图多种类型
- **社交登录**：微信小程序 / 第三方平台
- **日志审计**：操作日志 / 审计追踪

### 🏗️ 技术架构
- **Java 25** + **Spring Boot 4.0.1** + **Spring Cloud 2025.1.0**
- **Spring Security 7** + **Spring Authorization Server**
- **MyBatis Plus 3.5.16**
- **Redisson 4.0.0**
- **MapStruct 1.6.3** + **Lombok 1.18.42**
- **Nacos 3.1.1** 注册中心 + 配置中心

## 📦 项目结构

```
Goya/
├── bom/                           # 依赖版本管理 BOM
├── component/                     # 公共组件
│   ├── component-framework/       # 框架基础（聚合模块）
│   │   ├── framework-core/        # 核心工具类、基础定义
│   │   ├── framework-common/      # 公共组件、工具类
│   │   ├── framework-masker/      # 数据脱敏
│   │   ├── framework-crypto/      # 加密解密工具
│   │   ├── framework-cache/       # 缓存抽象层
│   │   ├── framework-bus/         # 消息总线抽象
│   │   ├── framework-log/         # 日志增强
│   │   ├── framework-oss/         # 对象存储抽象
│   │   ├── framework-security/    # 权限决策内核
│   │   ├── framework-servlet/     # Servlet 增强
│   │   └── framework-boot-starter/# 自动配置启动器
│   ├── component-redis/           # Redis 实现（Redisson）
│   │   ├── cache/                 # 缓存服务
│   │   └── support/               # 分布式锁/布隆过滤器/延迟队列/限流器
│   ├── component-kafka/           # Kafka 消息总线实现
│   ├── component-rabbitmq/        # RabbitMQ 消息总线实现
│   ├── component-mybatisplus/     # MyBatis Plus 增强
│   ├── component-captcha/         # 验证码
│   ├── component-security/        # 安全模块
│   │   ├── security-core/         # 核心领域模型、SPI 接口
│   │   ├── security-authentication/ # 认证（密码/短信/社交）
│   │   ├── security-authorization/  # 资源服务器（JWT验证）
│   │   └── security-oauth2/       # 授权服务器（OAuth2.1）
│   ├── component-social/          # 社交登录
│   ├── component-oss-aliyun/      # 阿里云 OSS 实现
│   ├── component-oss-s3/          # AWS S3 实现
│   ├── component-oss-minio/       # MinIO 实现
│   └── component-service/         # 服务抽象
├── ai/                            # AI 模块
│   ├── ai-spring/                 # Spring AI 集成
│   ├── ai-model/                  # 模型管理
│   ├── ai-rag/                    # RAG 实现
│   ├── ai-mcp/                    # MCP 协议
│   └── ai-video/                  # 视频处理
├── platform/                      # 平台应用
│   ├── platform-monolith/         # 单体应用
│   └── platform-distributed/      # 分布式应用
├── cloud/                         # 云原生支持
├── deploy/                        # 部署配置
│   ├── docker/                    # Docker Compose 配置
│   └── maven/                     # Maven 配置
├── docs/                          # 项目文档
│   ├── architecture/              # 架构文档
│   ├── guides/                    # 开发指南
│   ├── requirements/              # 需求文档
│   └── progress/                  # 开发进度
└── goya-web-ui/                   # Vue 3 前端管理系统
    ├── apps/                      # 应用
    │   ├── web-antd/              # Ant Design Vue 版本
    │   ├── web-ele/               # Element Plus 版本
    │   ├── web-naive/             # Naive UI 版本
    │   └── backend-mock/          # Mock 服务
    ├── packages/                  # 共享包
    └── internal/                  # 内部工具
```

## 🚀 快速开始

### 环境要求

- **JDK 25+**
- **Maven 3.9+**
- **Node.js 20+** & **pnpm 10+**
- **Docker** (可选)
- **Redis 7+** (用于缓存和 Session)
- **MySQL 8+** 或 **PostgreSQL 15+**

### 后端启动

```bash
# 安装依赖
mvn clean install -DskipTests

# 启动认证服务器
cd platform/platform-monolith
mvn spring-boot:run
```

访问：`http://localhost:8080`

### 前端启动

```bash
cd goya-web-ui

# 安装依赖
pnpm install

# 启动开发服务器（Ant Design Vue 版本）
pnpm dev:antd

# 或启动其他版本
pnpm dev:ele     # Element Plus
pnpm dev:naive   # Naive UI
```

访问：`http://localhost:5555`

### Docker 快速启动

```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```

包含：MySQL、Redis、MongoDB、Nacos 等基础设施。

## 🎯 核心模块详解

### 框架基础 (component-framework)

提供框架核心能力，包含 11 个子模块：

| 模块 | 说明 |
|------|------|
| framework-core | 核心工具类、基础定义、响应封装 |
| framework-common | 公共组件、通用工具类 |
| framework-masker | 数据脱敏（手机号、身份证、邮箱等） |
| framework-crypto | 加密解密工具（AES、RSA、SM4等） |
| framework-cache | 缓存抽象层、统一缓存接口 |
| framework-bus | 消息总线抽象、事件发布订阅 |
| framework-log | 日志增强、操作日志、审计日志 |
| framework-oss | 对象存储抽象、统一存储接口 |
| framework-security | 权限决策内核、SRA 策略模型 |
| framework-servlet | Servlet 增强、XSS防护、请求加解密 |
| framework-boot-starter | 自动配置启动器 |

### Redis 模块 (component-redis)

基于 Redisson 实现的 Redis 增强功能：

- **缓存服务**：统一缓存操作接口
- **分布式锁**：可重入锁、公平锁、读写锁
- **布隆过滤器**：防止缓存穿透
- **延迟队列**：可靠延迟队列实现
- **限流器**：基于令牌桶的分布式限流
- **Topic 消息**：发布订阅模式

### 安全模块 (component-security)

基于 **Spring Security 7** 和 **Spring Authorization Server** 构建：

- **security-core**：核心领域模型（SecurityUser、SPI 接口）
- **security-authentication**：多种认证方式（密码/短信/社交）
- **security-authorization**：资源服务器（JWT 验证、黑名单）
- **security-oauth2**：授权服务器（OAuth2.1 + OIDC）

**详细文档**：[企业级认证授权方案](./docs/requirements/features/auth-system.md)

### AI 模块 (ai/)

整合 **Spring AI** 和 **LangChain4j**，提供开箱即用的 AI 能力：

- 多模型统一接口
- RAG 检索增强生成
- Prompt 管理
- Function Calling
- MCP 协议支持
- 视频 AI 分析

## 📚 文档

- [架构设计](./docs/architecture/overview.md)
- [快速开始](./docs/guides/quick-start.md)
- [开发指南](./docs/guides/development.md)
- [部署指南](./docs/guides/deployment.md)
- [产品需求](./docs/requirements/product-requirements.md)
- [技术需求](./docs/requirements/technical-requirements.md)
- [开发路线图](./docs/progress/roadmap.md)

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [贡献指南](./CONTRIBUTING.md) 了解详情。

## 📄 开源协议

本项目采用 [Apache License 2.0](./LICENSE) 开源协议。

## 🔗 相关链接

- 官网：https://www.ysmjjsy.com
- GitHub：https://github.com/GoyaDo/Goya
- 问题反馈：https://github.com/GoyaDo/Goya/issues

## ⭐ Star History

如果这个项目对你有帮助，欢迎 Star ⭐

---

<div align="center">

**Built with ❤️ by Goya Team**

</div>
