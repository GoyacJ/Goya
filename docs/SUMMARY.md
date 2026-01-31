# Goya 项目文档总结

> 最后更新：2026-01-29  
> 版本：0.4.0

## 📋 项目概览

Goya 是一个基于 **Spring Boot 4.0.1** 和 **Java 25** 构建的企业级微服务开发框架。

### 技术栈

- **Java 25** + **Spring Boot 4.0.1** + **Spring Cloud 2025.1.0**
- **Spring Security 7** + **Spring Authorization Server**
- **MyBatis Plus 3.5.15** + **Redisson 4.0.0**
- **Spring AI 2.0.0-M1** + **LangChain4j 1.9.1**

## 📦 模块结构（v0.4.0）

### Component 组件模块（12个）

```
component/
├── component-framework/       # 框架基础（聚合，10个子模块）
│   ├── framework-core/        # 核心工具类
│   ├── framework-common/      # 公共组件
│   ├── framework-masker/      # 数据脱敏
│   ├── framework-crypto/      # 加密解密
│   ├── framework-cache/       # 缓存抽象
│   ├── framework-bus/         # 消息总线抽象
│   ├── framework-log/         # 日志增强
│   ├── framework-oss/         # OSS 抽象
│   ├── framework-servlet/     # Servlet 增强
│   └── framework-boot-starter/# 自动配置
├── component-redis/           # Redis 实现（Redisson）
├── component-kafka/           # Kafka 消息
├── component-rabbitmq/        # RabbitMQ 消息
├── component-mybatisplus/     # MyBatis Plus 增强
├── component-captcha/         # 验证码
├── component-security/        # 安全模块（4个子模块）
│   ├── security-core/         # 核心领域模型
│   ├── security-authentication/ # 认证
│   ├── security-authorization/  # 资源服务器
│   └── security-oauth2/       # 授权服务器
├── component-social/          # 社交登录
├── component-oss-aliyun/      # 阿里云 OSS
├── component-oss-s3/          # AWS S3
├── component-oss-minio/       # MinIO
└── component-service/         # 服务抽象
```

### AI 模块（5个）

```
ai/
├── ai-spring/     # Spring AI 集成
├── ai-model/      # 模型管理
├── ai-rag/        # RAG 实现
├── ai-mcp/        # MCP 协议
└── ai-video/      # 视频处理
```

### 平台应用（2个）

```
platform/
├── platform-monolith/    # 单体应用
└── platform-distributed/ # 分布式应用
```

## 📚 文档结构

```
docs/
├── architecture/                  # 架构文档
│   ├── overview.md               # 架构概览
│   ├── modules.md                # 模块详解
│   └── design-patterns.md        # 设计模式
├── guides/                       # 开发指南
│   ├── quick-start.md            # 快速开始
│   ├── development.md            # 开发规范
│   └── deployment.md             # 部署指南
├── requirements/                 # 需求文档
│   ├── product-requirements.md   # PRD
│   ├── technical-requirements.md # TRD
│   └── features/                 # 功能需求
│       ├── auth-system.md        # 认证授权
│       ├── ai-integration.md     # AI 集成
│       └── multi-tenant.md       # 多租户
└── progress/                     # 开发进度
    ├── roadmap.md                # 路线图
    ├── milestones.md             # 里程碑
    └── changelog.md              # 更新日志
```

## 🎯 开发规范

### Rules 规则文件

位于 `../.cursor/rules/` 目录：

| 文件 | 说明 |
|------|------|
| goya-rules.mdc | 通用规则 |
| java-backend.mdc | Java 后端规范 |
| security-module.mdc | 安全模块规范 |
| ai-module.mdc | AI 模块规范 |
| database.mdc | 数据库规范 |
| testing.mdc | 测试规范 |

### Skills 开发工具

位于 `../.cursor/skills/` 目录：

| 工具 | 说明 |
|------|------|
| goya-component-generator | 组件脚手架生成器 |
| goya-security-helper | 安全模块开发辅助 |
| goya-ai-helper | AI 集成辅助 |
| goya-database-generator | 数据库代码生成器 |
| goya-api-designer | API 设计器 |
| goya-code-checker | 代码规范检查 |
| goya-doc-generator | 文档生成器 |
| goya-test-generator | 测试用例生成器 |

## 🚀 快速导航

### 新手入门
1. [README](../README.md) - 项目概览
2. [快速开始](./guides/quick-start.md) - 5分钟上手
3. [开发指南](./guides/development.md) - 开发规范

### 深入学习
1. [架构概览](./architecture/overview.md) - 理解架构设计
2. [模块详解](./architecture/modules.md) - 了解各模块功能
3. [设计模式](./architecture/design-patterns.md) - 学习最佳实践

### 贡献指南
1. [贡献指南](../CONTRIBUTING.md) - 如何贡献代码
2. [开发规范](../.cursor/rules/) - 代码规范要求

### 规划路线
1. [产品需求](./requirements/product-requirements.md) - 产品规划
2. [开发路线图](./progress/roadmap.md) - 未来规划
3. [里程碑](./progress/milestones.md) - 版本计划
4. [更新日志](./progress/changelog.md) - 版本变化

## 🔗 相关链接

- **官网**：https://www.ysmjjsy.com
- **GitHub**：https://github.com/GoyaDo/Goya
- **问题反馈**：https://github.com/GoyaDo/Goya/issues

---

**Built with ❤️ by Goya Team**
