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

- **Goya/** - Java 后端框架
- **Goya-Web/** - Vue 3 前端管理系统

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
- **缓存**：Caffeine / Redis / 多级缓存
- **消息总线**：Kafka / Stream 抽象
- **数据库**：MyBatis Plus / JPA 双引擎
- **对象存储**：阿里云 OSS / MinIO / S3
- **验证码**：算术 / 滑块 / 拼图多种类型
- **社交登录**：微信小程序 / 第三方平台
- **日志审计**：操作日志 / 审计追踪

### 🏗️ 技术架构
- **Java 25** + **Spring Boot 4.0.1** + **Spring Cloud 2025.1.0**
- **Spring Security 7** + **Spring Authorization Server**
- **MyBatis Plus 3.5.15** / **Spring Data JPA**
- **Redisson 4.0.0** + **Caffeine**
- **MapStruct 1.6.3** + **Lombok 1.18.42**
- **Nacos 3.1.1** 注册中心 + 配置中心

## 📦 项目结构

```
Goya/
├── Goya/                        # 后端框架
│   ├── bom/                     # 依赖版本管理 BOM
│   ├── component/               # 公共组件
│   │   ├── component-core/      # 核心工具类
│   │   ├── component-framework/ # 框架基础
│   │   ├── component-web/       # Web 增强
│   │   ├── component-security/  # 安全模块
│   │   │   ├── security-core/           # 核心领域模型
│   │   │   ├── security-authentication/ # 认证
│   │   │   ├── security-authorization/  # 资源服务器
│   │   │   └── security-oauth2/         # 授权服务器
│   │   ├── component-cache/     # 缓存模块
│   │   ├── component-bus/       # 消息总线
│   │   ├── component-database/  # 数据库增强
│   │   ├── component-oss/       # 对象存储
│   │   ├── component-captcha/   # 验证码
│   │   ├── component-social/    # 社交登录
│   │   └── component-log/       # 日志模块
│   ├── ai/                      # AI 模块
│   │   ├── ai-spring/           # Spring AI 集成
│   │   ├── ai-model/            # 模型管理
│   │   ├── ai-rag/              # RAG 实现
│   │   ├── ai-mcp/              # MCP 协议
│   │   └── ai-video/            # 视频处理
│   ├── platform/                # 平台应用
│   │   ├── platform-monolith/   # 单体应用
│   │   └── platform-distributed/# 分布式应用
│   ├── cloud/                   # 云原生支持
│   └── doc/                     # 文档
│       ├── docker/              # Docker 编排
│       ├── maven/               # Maven 配置
│       └── security/            # 安全方案文档
└── Goya-Web/                    # 前端管理系统（Vue 3）
    ├── apps/                    # 应用
    │   ├── web-antd/            # Ant Design Vue 版本
    │   ├── web-ele/             # Element Plus 版本
    │   ├── web-naive/           # Naive UI 版本
    │   └── backend-mock/        # Mock 服务
    ├── packages/                # 共享包
    │   ├── @core/               # 核心包
    │   ├── effects/             # 副作用
    │   ├── stores/              # 状态管理
    │   ├── types/               # 类型定义
    │   └── utils/               # 工具函数
    └── internal/                # 内部工具
        ├── lint-configs/        # Lint 配置
        ├── vite-config/         # Vite 配置
        └── tsconfig/            # TypeScript 配置
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
cd Goya

# 安装依赖
mvn clean install -DskipTests

# 启动认证服务器
cd platform/platform-monolith/auth-server
mvn spring-boot:run
```

访问：`http://localhost:8080`

### 前端启动

```bash
cd Goya-Web

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
cd Goya/doc/docker/docker-compose/basic
docker-compose up -d
```

包含：MySQL、Redis、MongoDB、Nacos 等基础设施。

## 🎯 核心模块详解

### 安全模块 (component-security)

基于 **Spring Security 7** 和 **Spring Authorization Server** 构建的完整认证授权解决方案。

**核心能力**：
- OAuth2.1 授权服务器（Authorization Code + PKCE）
- OIDC Provider（支持 Discovery）
- 资源服务器（JWT 验证 + 黑名单）
- 多种登录方式（密码 / 短信 / 社交）
- 多租户 Issuer 隔离
- Token 管理（JWT Access Token + Opaque Refresh Token）

**详细文档**：[企业级认证授权方案](./Goya/doc/security/enterprise-auth-solution.md)

### AI 模块 (ai/)

整合 **Spring AI** 和 **LangChain4j**，提供开箱即用的 AI 能力。

**核心能力**：
- 多模型统一接口
- RAG 检索增强生成
- Prompt 管理
- Function Calling
- MCP 协议支持
- 视频 AI 分析（基于 FFmpeg + OpenCV）

### 缓存模块 (component-cache)

多级缓存解决方案，支持 Caffeine 本地缓存 + Redis 分布式缓存。

**核心能力**：
- 统一缓存接口
- 自动缓存同步（Redis Pub/Sub）
- 缓存预热和失效策略
- 缓存穿透/击穿/雪崩防护

### 数据库模块 (component-database)

提供 MyBatis Plus 和 JPA 双引擎支持。

**核心能力**：
- 动态数据源切换
- 多租户数据隔离
- 审计字段自动填充
- SQL 监控（P6Spy）
- 支持 MySQL、PostgreSQL、OpenGauss、TDengine 等

## 📚 文档

- [架构设计](./docs/architecture/overview.md)
- [快速开始](./docs/guides/quick-start.md)
- [开发指南](./docs/guides/development.md)
- [部署指南](./docs/guides/deployment.md)
- [API 文档](./docs/api/rest-api.md)
- [产品需求](./docs/requirements/product-requirements.md)
- [技术需求](./docs/requirements/technical-requirements.md)
- [开发路线图](./docs/progress/roadmap.md)

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [贡献指南](./CONTRIBUTING.md) 了解详情。

### 贡献者

感谢所有为 Goya 做出贡献的开发者！

## 📄 开源协议

本项目采用 [Apache License 2.0](./Goya/LICENSE) 开源协议。

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
