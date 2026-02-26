# Goya

<div align="center">

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)

**企业级后端全体系脚手架（Spring Boot 4.0 + JDK 25）**

[English](./README.en-US.md) | 简体中文

</div>

## 项目定位

Goya 是一个面向企业级场景的后端仓库，核心目标是提供可直接落地的：

- 统一认证授权（PC / 移动端 / 小程序）
- OAuth2.1 与 SSO
- 动态权限 + 数据权限
- 多租户与基础组件化能力

## 当前安全能力闭环

已在 `framework-security`、`component-mybatisplus`、`component-social`、`component-security` 完成以下闭环能力：

- 统一认证入口：密码、短信、社交、小程序、MFA
- OAuth2.1 扩展授权：`urn:goya:grant-type:pre-auth-code`
- 双 Token 策略：`AccessToken=JWT`、`RefreshToken=Opaque`（默认轮换，`reuse=false`）
- 全局会话撤销：`logout CURRENT_SESSION | ALL_SESSIONS | BY_CLIENT`、管理员 `kickout`
- OIDC 登出联动：`/connect/logout` 收敛到统一会话生命周期服务
- 撤销即时生效：资源侧 `RevokedTokenFilter` 拦截旧 token（401）
- 动态 API 权限：`PolicyAuthorizationFilter` 运行时策略即时生效
- 数据权限执行：`GoyaDataPermissionHandler`，生产建议 `fail-closed=true`
- 管理域默认实现：`component-admin`（RBAC、用户/角色/权限/菜单/部门/字典、策略管理）

## 模块总览

| 模块 | 说明 |
|---|---|
| `component/component-framework/framework-security` | SRA 策略模型与授权引擎 |
| `component/component-mybatisplus` | MyBatis Plus 企业配置 + 多租户 + 数据权限执行 |
| `component/component-admin` | 企业管理域（RBAC、用户、菜单、部门、字典、策略） |
| `component/component-social` | 短信/第三方/小程序能力与社交绑定 |
| `component/component-security/security-core` | 安全核心模型、SPI、会话生命周期抽象 |
| `component/component-security/security-authentication` | 统一认证 API 与会话命令入口 |
| `component/component-security/security-oauth2` | OAuth2.1 授权服务器与令牌治理 |
| `component/component-security/security-authorization` | 资源侧鉴权、吊销拦截、策略联动 |
| `platform/platform-monolith` | 默认运行入口 |

## 快速开始

### 1. 环境要求

- JDK 25
- Maven 3.9+
- Redis（认证临时态、预认证码、撤销索引）
- OAuth2 相关表（`registered_client`、`authorization`、`consent`、`oauth2_jwk`）

### 2. 构建校验（跳过测试）

```bash
mvn -DskipTests validate
```

全仓编译（需 JDK 25）：

```bash
mvn -DskipTests compile
```

### 3. 启动单体应用

```bash
cd platform/platform-monolith
mvn spring-boot:run
```

默认地址：`http://localhost:8101`

## 文档入口

- [文档总索引](./docs/SUMMARY.md)
- [安全架构设计](./docs/architecture/component-security-design.md)
- [管理域架构设计](./docs/architecture/component-admin-design.md)
- [安全部署指南](./docs/operations/security-deploy.md)
- [构建与发布](./docs/operations/build-and-release.md)
- [变更日志](./docs/progress/changelog.md)

## 开发与治理约束

- 依赖版本统一在 `bom/pom.xml` 管理
- 优先复用已有模块，不平行造轮子
- 不新增测试文件（`src/test/**`）与测试依赖
- 任何功能变更必须同步更新文档（至少 `docs/progress/changelog.md`）

## 许可证

[Apache License 2.0](./LICENSE)
