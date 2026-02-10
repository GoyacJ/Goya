# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-green.svg)](https://spring.io/projects/spring-cloud)

**新一代企业级微服务开发框架**

[English](./README.en-US.md) | 简体中文

[快速开始](#快速开始) • [核心特性](#核心特性) • [项目结构](#项目结构) • [文档](#文档) • [贡献指南](./CONTRIBUTING.md)

</div>

---

## 📖 项目简介

Goya 是一个基于 **Spring Boot 4.0.1** 和 **Java 25** 构建的企业级微服务开发框架，采用前后端分离架构，提供完整的安全认证授权、AI 集成、多租户、缓存、消息总线等企业级能力。

### 架构组成

- **Goya/** - Java 后端框架（本目录）
- **goya-web-ui/** - 前端独立仓库（当前仓库不包含）

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
├── docs/                          # 文档目录（当前分支待补充）
├── .cursor/                       # Cursor 开发工具
│   ├── rules/                     # 开发规则（.mdc）
│   ├── skills/                    # 开发技能（SKILL.md）
│   └── hooks/                     # Git Hooks
└── logs/                          # 本地运行日志
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

访问：`http://localhost:8101`

### 前端说明

```bash
# 当前仓库不包含前端代码
# 请在前端独立仓库执行安装和启动
```

### 配置模板

```bash
# Docker 基础设施配置模板
cp deploy/docker/docker-compose/basic/.env.example deploy/docker/docker-compose/basic/.env

# Maven 私有仓库配置模板（按需）
cp deploy/maven/conf/settings.xml.example deploy/maven/conf/settings.xml

# 应用配置模板
cp platform/platform-monolith/src/main/resources/application-dev.example.yml platform/platform-monolith/src/main/resources/application-dev.yml
cp platform/platform-monolith/src/main/resources/application-prod.example.yml platform/platform-monolith/src/main/resources/application-prod.yml
```

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

**详细文档**：正在整理中（当前分支暂未包含该文档）

### AI 模块 (ai/)

整合 **Spring AI** 和 **LangChain4j**，提供开箱即用的 AI 能力：

- 多模型统一接口
- RAG 检索增强生成
- Prompt 管理
- Function Calling
- MCP 协议支持
- 视频 AI 分析

## 📚 文档

当前分支 `docs/` 目录尚未补齐。可参考以下已有文档：

- [README](./README.md) - 当前模块结构与启动方式
- [README.en-US](./README.en-US.md) - 英文说明
- [CONTRIBUTING](./CONTRIBUTING.md) - 贡献与协作规范
- [AI 助手使用指南](./.cursor/AI_ASSISTANT_GUIDE.md) - AI 协作流程

### 开发规范体系
- [AI 助手使用指南](./.cursor/AI_ASSISTANT_GUIDE.md) - **AI 助手必读**，定义 AI 开发规范和工作流程
- [开发工作流规范](./.cursor/rules/development-workflow.mdc) - 标准开发流程（Rules 格式）

### 规划文档
- 规划文档将在 `docs/` 目录补齐后更新索引

## 🛠️ 开发规范

Goya 项目建立了完整的开发规范体系，确保每次开发都遵循统一的流程和标准。

### 开发工作流

**每次开发前必须**：
1. 查阅项目文档（本 README.md 与 CONTRIBUTING.md）
2. 查阅当前分支进度（Issue、PR 或项目看板）
3. 激活相关 Cursor Rules 和 Skills

**开发过程中**：
1. 遵循代码规范（`.cursorrules`、Rules）
2. 使用相关 Skills 辅助开发
3. 编写测试

**开发完成后必须**：
1. 更新受影响模块说明（README、注释或文档）
2. 补充或更新测试
3. 使用规范的 Git 提交信息提交代码

**详细流程**：
- 📖 [AI 助手使用指南](./.cursor/AI_ASSISTANT_GUIDE.md) - **AI 助手必读**，完整的开发流程说明
- ✅ [开发前检查清单](./.cursor/rules/pre-development-checklist.mdc) - 开发前必须完成的检查项
- ✅ [开发后检查清单](./.cursor/rules/post-development-checklist.mdc) - 开发后必须完成的工作

### Cursor 开发工具

- **Rules**：代码规范和开发流程规则（`.cursor/rules/*.mdc`）
  - `ai-assistant-guide.mdc` - **AI 助手开发规范（AI 助手必读）**
  - `development-workflow.mdc` - 开发工作流规范
  - `pre-development-checklist.mdc` - 开发前检查清单
  - `post-development-checklist.mdc` - 开发后检查清单
- **Skills**：开发辅助工具（`.cursor/skills/*/SKILL.md`）
  - `goya-development-workflow` - 开发工作流助手（**必须使用**）
- **Hooks**：Git 提交前自动检查（`.cursor/hooks/*.sh` 和 `.cursor/hooks.json`）

**AI 助手必读**：
- [AI 助手使用指南](./.cursor/AI_ASSISTANT_GUIDE.md) - **AI 助手必读**，定义 AI 开发规范和工作流程

**安装 Hooks**（可选）：
```bash
cp .cursor/hooks/pre-commit.sh .git/hooks/pre-commit
cp .cursor/hooks/commit-msg.sh .git/hooks/commit-msg
chmod +x .git/hooks/pre-commit .git/hooks/commit-msg
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [贡献指南](./CONTRIBUTING.md) 了解详情。

**重要提示**：在提交代码前，请确保已遵循 [开发工作流规范](./.cursor/rules/development-workflow.mdc) 完成所有必要的工作。

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
