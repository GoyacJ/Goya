# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)

**新一代企业级微服务开发框架（Codex 维护模式）**

[English](./README.en-US.md) | 简体中文

</div>

## 项目说明

Goya 是基于 **Java 25** 和 **Spring Boot 4.0.1** 的企业级后端框架仓库。
当前仓库以后端为主，前端代码不在本仓库中维护。

## 当前状态

- 主运行入口：`platform/platform-monolith`
- 默认端口：`8101`
- 模块总量：多聚合模块 + 细分组件模块
- 开发模式：**Codex 优先**
- 质量策略：**无测试文件策略**（不新增 `src/test/**`）

## 快速开始

### 1) 环境要求

- JDK 25+
- Maven 3.9+
- Docker（可选）

### 2) 构建（跳过测试）

```bash
mvn -DskipTests compile
```

### 3) 启动单体应用

```bash
cd platform/platform-monolith
mvn spring-boot:run
```

访问：`http://localhost:8101`

### 4) 启动基础设施（可选）

```bash
cd deploy/docker/docker-compose/basic
docker-compose up -d
```

## 项目结构

```text
Goya/
├── AGENTS.md                 # Codex 项目级执行规范
├── .agents/skills/           # 项目专属 Codex skills
├── docs/                     # 文档体系（总入口见 docs/SUMMARY.md）
├── bom/                      # 依赖版本管理
├── component/                # 基础框架与通用组件
├── ai/                       # AI 相关模块
├── platform/                 # 平台应用（含 monolith）
├── cloud/                    # 云原生占位模块
└── deploy/                   # 部署与配置
```

## 文档入口

请从总索引开始：

- [docs/SUMMARY.md](./docs/SUMMARY.md)

核心文档：

- [架构概览](./docs/architecture/overview.md)
- [模块地图](./docs/architecture/module-map.md)
- [Codex 开发工作流](./docs/development/codex-workflow.md)
- [无测试策略](./docs/development/no-test-policy.md)
- [构建与发布](./docs/operations/build-and-release.md)
- [变更日志](./docs/progress/changelog.md)

## Codex 维护约束

- 开发前先读 `docs/SUMMARY.md`
- 变更后至少执行 `mvn -DskipTests compile` 或模块级 `validate`
- 禁止新增测试文件与测试依赖
- 任何变更必须同步更新文档（至少 `docs/progress/changelog.md`）

## 贡献

请阅读：[CONTRIBUTING.md](./CONTRIBUTING.md)

## 许可证

[Apache License 2.0](./LICENSE)
