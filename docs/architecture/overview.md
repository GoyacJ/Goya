# 架构概览 | Architecture Overview

## 设计理念

Goya 框架遵循以下核心设计理念：

1. **模块化**：高内聚、低耦合的模块设计
2. **可扩展**：基于 SPI 的扩展机制
3. **开箱即用**：零配置或最小配置启动
4. **生产就绪**：内置企业级能力（安全、缓存、监控等）
5. **云原生友好**：支持容器化部署和微服务架构

## 整体架构

Goya 采用分层架构设计，从上到下分为：

```
┌─────────────────────────────────────────────────────────────┐
│                     应用层 (Applications)                    │
│  ┌──────────────────┐      ┌──────────────────┐            │
│  │  Monolith Apps   │      │ Distributed Apps │            │
│  │  - Auth Server   │      │ - Auth Service   │            │
│  │  - Admin Portal  │      │ - API Gateway    │            │
│  └──────────────────┘      └──────────────────┘            │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    领域层 (Domain Layer)                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  安全域 (Security Domain)                            │  │
│  │  - Authentication / Authorization / OAuth2           │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  AI 域 (AI Domain)                                   │  │
│  │  - Model / RAG / MCP / Video                         │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  业务域 (Business Domain)                            │  │
│  │  - OSS / Social / Captcha                            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    组件层 (Components)                       │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐             │
│  │   Redis    │ │   Kafka    │ │  RabbitMQ  │             │
│  │ (缓存/锁)  │ │  (消息)    │ │  (消息)    │             │
│  └────────────┘ └────────────┘ └────────────┘             │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐             │
│  │ MyBatisPlus│ │  OSS ALi   │ │ OSS MinIO  │             │
│  │ (数据访问) │ │  (存储)    │ │  (存储)    │             │
│  └────────────┘ └────────────┘ └────────────┘             │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   基础设施层 (Framework)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │   Core   │  │  Common  │  │ Servlet  │  │  Cache   │  │
│  │  (核心)  │  │  (公共)  │  │  (Web)   │  │  (缓存)  │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │   Bus    │  │   Log    │  │  Crypto  │  │  Masker  │  │
│  │  (消息)  │  │  (日志)  │  │  (加密)  │  │  (脱敏)  │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    核心层 (Core Layer)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Spring Boot 4.0.1 + Spring Cloud 2025.1.0          │  │
│  │  Spring Security 7 + Spring Authorization Server    │  │
│  │  Java 25 Virtual Threads + Modern Java APIs         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 技术栈

### 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | 最新 LTS 版本，支持虚拟线程 |
| Spring Boot | 4.0.1 | 企业级应用框架 |
| Spring Cloud | 2025.1.0 | 微服务生态 |
| Spring Security | 7.x | 安全框架 |
| Spring Authorization Server | 最新 | OAuth2.1 / OIDC 实现 |

### 数据访问

| 技术 | 版本 | 说明 |
|------|------|------|
| MyBatis Plus | 3.5.16 | ORM 框架 |
| Dynamic DataSource | 4.5.0 | 动态数据源 |
| P6Spy | 3.9.1 | SQL 监控 |

### 缓存 & 消息

| 技术 | 版本 | 说明 |
|------|------|------|
| Redis | 7+ | 分布式缓存 |
| Redisson | 4.0.0 | Redis 客户端 |
| Kafka | 最新 | 消息队列 |
| RabbitMQ | 最新 | 消息队列 |

### AI 能力

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring AI | 2.0.0-M1 | Spring AI 框架 |
| LangChain4j | 1.9.1 | AI 应用编排 |
| Spring AI Alibaba | 1.0.0.4 | 阿里云模型集成 |
| FFmpeg | 7.1.1 | 视频处理 |
| OpenCV | 4.11.0 | 计算机视觉 |

### 服务发现与配置

| 技术 | 版本 | 说明 |
|------|------|------|
| Nacos | 3.1.1 | 注册中心 + 配置中心 |

### 工具库

| 技术 | 版本 | 说明 |
|------|------|------|
| Guava | 33.5.0 | Google 工具库 |
| Apache Commons | Latest | Apache 工具集 |
| MapStruct | 1.6.3 | Bean 映射 |
| Lombok | 1.18.42 | 代码简化 |

## 模块架构

### Component 模块（12个）

```
component/
├── component-framework/       # 框架基础（聚合）
│   ├── framework-core/        # 核心工具
│   ├── framework-common/      # 公共组件
│   ├── framework-masker/      # 数据脱敏
│   ├── framework-crypto/      # 加密解密
│   ├── framework-cache/       # 缓存抽象
│   ├── framework-bus/         # 消息总线抽象
│   ├── framework-log/         # 日志增强
│   ├── framework-oss/         # OSS 抽象
│   ├── framework-security/    # 权限决策内核
│   ├── framework-servlet/     # Servlet 增强
│   └── framework-boot-starter/# 自动配置
├── component-redis/           # Redis 实现
├── component-kafka/           # Kafka 实现
├── component-rabbitmq/        # RabbitMQ 实现
├── component-mybatisplus/     # MyBatis Plus
├── component-captcha/         # 验证码
├── component-security/        # 安全模块（4子模块）
├── component-social/          # 社交登录
├── component-oss-aliyun/      # 阿里云 OSS
├── component-oss-s3/          # AWS S3
├── component-oss-minio/       # MinIO
└── component-service/         # 服务抽象
```

### Security 模块（4个子模块）

```
component-security/
├── security-core/             # 核心领域模型、SPI 接口
├── security-authentication/   # 认证（密码/短信/社交）
├── security-authorization/    # 资源服务器（JWT 验证）
└── security-oauth2/           # 授权服务器（OAuth2.1）
```

## 部署架构

### 单体部署

适用于小型项目或快速启动：

```
┌─────────────────────────────────────┐
│         Load Balancer (Nginx)       │
└──────────────┬──────────────────────┘
               │
       ┌───────┴───────┐
       ▼               ▼
┌─────────────┐ ┌─────────────┐
│  Instance 1 │ │  Instance 2 │
│             │ │             │
│  Auth       │ │  Auth       │
│  Admin      │ │  Admin      │
│  API        │ │  API        │
└──────┬──────┘ └──────┬──────┘
       │               │
       └───────┬───────┘
               ▼
    ┌─────────────────────┐
    │  Shared Services    │
    │  - MySQL/PostgreSQL │
    │  - Redis            │
    │  - Nacos            │
    └─────────────────────┘
```

### 微服务部署

适用于大型项目或高并发场景：

```
┌────────────────────────────────────────┐
│         API Gateway (Spring Cloud)     │
└────────────────┬───────────────────────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│  Auth   │ │  Admin  │ │  User   │
│ Service │ │ Service │ │ Service │
└────┬────┘ └────┬────┘ └────┬────┘
     │           │           │
     └───────────┼───────────┘
                 ▼
    ┌────────────────────────┐
    │   Service Registry     │
    │   (Nacos)              │
    └────────────────────────┘
                 │
                 ▼
    ┌────────────────────────┐
    │   Infrastructure       │
    │   - MySQL Cluster      │
    │   - Redis Cluster      │
    │   - Kafka Cluster      │
    │   - Object Storage     │
    └────────────────────────┘
```

## 数据流

### 认证授权流程

```
┌──────────┐                                    ┌──────────────┐
│  Client  │                                    │  Auth Server │
│ (Browser)│                                    │              │
└────┬─────┘                                    └──────┬───────┘
     │                                                 │
     │ 1. GET /oauth2/authorize?...                   │
     ├────────────────────────────────────────────────>│
     │                                                 │
     │ 2. 302 Redirect to /login                      │
     │<────────────────────────────────────────────────┤
     │                                                 │
     │ 3. POST /login (username/password)             │
     ├────────────────────────────────────────────────>│
     │                                                 │
     │ 4. 302 Redirect with authorization code        │
     │<────────────────────────────────────────────────┤
     │                                                 │
     │ 5. POST /oauth2/token (code + PKCE verifier)   │
     ├────────────────────────────────────────────────>│
     │                                                 │
     │ 6. Response with JWT access token              │
     │<────────────────────────────────────────────────┤
     │                                                 │
     ▼                                                 ▼
┌──────────────────────────────────────────────────────────┐
│  Resource Server validates JWT and checks permissions    │
└──────────────────────────────────────────────────────────┘
```

### Redis 功能矩阵

```
┌─────────────────────────────────────────────────────┐
│                  component-redis                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐│
│  │  缓存服务   │  │  分布式锁   │  │ 布隆过滤器  ││
│  │RedissonCache│  │RedissonLock │  │RedissonBloom││
│  └─────────────┘  └─────────────┘  └─────────────┘│
│                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐│
│  │  延迟队列   │  │   限流器    │  │ Topic消息   ││
│  │DelayedQueue │  │RateLimiter  │  │RedissonTopic││
│  └─────────────┘  └─────────────┘  └─────────────┘│
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 扩展机制

Goya 提供多种扩展机制：

### 1. SPI 扩展

通过定义 SPI 接口，业务系统可以注入自定义实现：

```java
// 框架定义 SPI（security-core）
public interface IUserService {
    SecurityUser loadUserByUsername(String username);
}

// 业务系统实现
@Component
public class CustomUserService implements IUserService {
    @Override
    public SecurityUser loadUserByUsername(String username) {
        // 自定义实现
    }
}
```

### 2. 配置驱动

通过 `application.yml` 配置开启或关闭功能：

```yaml
goya:
  security:
    authentication:
      login:
        allow-password-login: true
        allow-sms-login: true
  redis:
    enabled: true
    address: redis://localhost:6379
```

### 3. 自动配置

基于 Spring Boot 自动配置机制，按需加载组件：

```java
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(GoyaRedisProperties.class)
public class GoyaRedisAutoConfiguration {
    // 自动配置 Redis
}
```

## 安全架构

详见 [企业级认证授权方案](../requirements/features/auth-system.md)

核心要点：
- **无状态 API**：资源服务器仅验证 JWT，不维护 Session
- **多租户隔离**：通过 Issuer、JWK、Client 实现租户隔离
- **Token 策略**：JWT Access Token (短期) + Opaque Refresh Token (长期)
- **安全传输**：HTTPS + HSTS + CSP
- **审计追踪**：所有安全事件记录审计日志

## 下一步阅读

- [模块详解](./modules.md)
- [设计模式](./design-patterns.md)
- [快速开始](../guides/quick-start.md)
- [开发指南](../guides/development.md)
