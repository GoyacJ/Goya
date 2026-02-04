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
├── SUMMARY.md                    # 文档总览（本文件）
├── DOCUMENTATION_STRUCTURE.md    # 文档结构说明
├── architecture/                  # 架构文档
│   ├── overview.md               # 架构概览
│   ├── modules.md                # 模块详解（概览）
│   ├── modules-detailed.md       # 模块详细文档（技术细节）
│   └── design-patterns.md        # 设计模式
├── guides/                       # 开发指南
│   ├── quick-start.md            # 快速开始
│   ├── development.md            # 开发规范
│   ├── deployment.md             # 部署指南
│   └── api-reference.md          # API 参考
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
    ├── changelog.md              # 更新日志
    └── PROGRESS_TEMPLATE.md      # 模块进度文档模板
```

**Cursor 工具目录**（供 Cursor AI 使用）：
```
.cursor/
├── AI_ASSISTANT_GUIDE.md         # AI 助手使用指南（AI 助手必读）
├── rules/                        # Rules（.mdc 格式）
├── skills/                       # Skills（SKILL.md 格式）
└── hooks/                        # Git Hooks
```

## 🎯 开发规范

### Cursor 开发工具

**AI 助手必读**：
- [AI 助手使用指南](../.cursor/AI_ASSISTANT_GUIDE.md) - **AI 助手必读**

**Rules**（`.cursor/rules/*.mdc`）：
- `ai-assistant-guide.mdc` - **AI 助手开发规范（AI 助手必读）** ⭐⭐⭐
- `development-workflow.mdc` - 开发工作流规范 ⭐⭐⭐
- `pre-development-checklist.mdc` - 开发前检查清单 ⭐⭐⭐
- `post-development-checklist.mdc` - 开发后检查清单 ⭐⭐⭐

**Skills**（`.cursor/skills/*/SKILL.md`）：
- `goya-development-workflow` - 开发工作流助手（**必须使用**）⭐⭐⭐

**Hooks**（`.cursor/hooks/*.sh`）：
- `pre-commit.sh` - 提交前检查（编译、测试、文档）
- `commit-msg.sh` - 提交信息格式检查

## 🚀 快速导航

### 新手入门
1. [README](../README.md) - 项目概览
2. [快速开始](./guides/quick-start.md) - 5分钟上手
3. [开发指南](./guides/development.md) - 开发规范

### 深入学习
1. [架构概览](./architecture/overview.md) - 理解架构设计
2. [模块详解](./architecture/modules.md) - 了解各模块功能
3. [模块详细文档](./architecture/modules-detailed.md) - 深入了解技术细节
4. [设计模式](./architecture/design-patterns.md) - 学习最佳实践
5. [API 参考](./guides/api-reference.md) - 核心 API 使用说明

### 贡献指南
1. [贡献指南](../CONTRIBUTING.md) - 如何贡献代码
2. [AI 助手使用指南](../.cursor/AI_ASSISTANT_GUIDE.md) - **AI 助手必读**，AI 开发规范
3. [开发工作流规范](../.cursor/rules/development-workflow.mdc) - 标准开发流程
4. [文档结构说明](./DOCUMENTATION_STRUCTURE.md) - 了解文档组织原则

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
