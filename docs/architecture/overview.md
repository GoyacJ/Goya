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
│  │  - OSS / Social / Captcha / Log                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   基础设施层 (Infrastructure)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Cache   │  │ Database │  │   Bus    │  │   Web    │  │
│  │  Redis   │  │ MyBatis+ │  │  Kafka   │  │  Spring  │  │
│  │ Caffeine │  │   JPA    │  │  Stream  │  │   MVC    │  │
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
| MyBatis Plus | 3.5.15 | ORM 框架（主要） |
| Spring Data JPA | 4.x | ORM 框架（可选） |
| Dynamic DataSource | 4.5.0 | 动态数据源 |
| P6Spy | 3.9.1 | SQL 监控 |

### 缓存

| 技术 | 版本 | 说明 |
|------|------|------|
| Caffeine | Latest | 本地缓存 |
| Redis | 7+ | 分布式缓存 |
| Redisson | 4.0.0 | Redis 客户端 |

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

### 云原生部署 (Kubernetes)

适用于云平台部署：

```
┌─────────────────────────────────────────────┐
│              Kubernetes Cluster             │
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │          Ingress Controller           │ │
│  └─────────────────┬─────────────────────┘ │
│                    │                       │
│  ┌─────────────────┼─────────────────────┐ │
│  │                 ▼                     │ │
│  │  ┌──────────┐ ┌──────────┐ ┌────────┐│ │
│  │  │  Auth    │ │  Admin   │ │  User  ││ │
│  │  │  Pod     │ │  Pod     │ │  Pod   ││ │
│  │  │  (x3)    │ │  (x3)    │ │  (x3)  ││ │
│  │  └────┬─────┘ └────┬─────┘ └────┬───┘│ │
│  │       │            │            │    │ │
│  │       └────────────┼────────────┘    │ │
│  │                    ▼                 │ │
│  │  ┌──────────────────────────────┐   │ │
│  │  │   Service Mesh (Istio)       │   │ │
│  │  └──────────────────────────────┘   │ │
│  └─────────────────────────────────────┘ │
│                                          │
│  ┌─────────────────────────────────────┐ │
│  │        StatefulSet Services         │ │
│  │  - MySQL Operator                   │ │
│  │  - Redis Operator                   │ │
│  │  - Kafka Operator                   │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
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

### 缓存数据流

```
┌─────────┐      ┌──────────┐      ┌─────────┐      ┌──────────┐
│ Request │─────>│ L1 Cache │─────>│ L2 Cache│─────>│ Database │
│         │      │(Caffeine)│      │ (Redis) │      │          │
└─────────┘      └──────────┘      └─────────┘      └──────────┘
                       │                 │                │
                       │  Cache Miss     │                │
                       │<────────────────┤                │
                       │                 │   DB Query     │
                       │                 │<───────────────┤
                       │  Update L1      │                │
                       │<────────────────┤                │
                       │                 │                │
                  ┌────┴─────┐      ┌────┴─────┐         │
                  │  Evict   │<─────│ Pub/Sub  │         │
                  │ Listener │      │ Channel  │         │
                  └──────────┘      └──────────┘         │
```

## 扩展机制

Goya 提供多种扩展机制：

### 1. SPI 扩展

通过定义 SPI 接口，业务系统可以注入自定义实现：

```java
// 框架定义 SPI
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
    resource:
      jwt:
        issuer-uri: https://auth.example.com
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

详见 [企业级认证授权方案](../../Goya/doc/security/enterprise-auth-solution.md)

核心要点：
- **无状态 API**：资源服务器仅验证 JWT，不维护 Session
- **多租户隔离**：通过 Issuer、JWK、Client 实现租户隔离
- **Token 策略**：JWT Access Token (短期) + Opaque Refresh Token (长期)
- **安全传输**：HTTPS + HSTS + CSP
- **审计追踪**：所有安全事件记录审计日志

## 性能优化

### 1. 虚拟线程

充分利用 Java 25 的虚拟线程，提升并发能力：

```java
@Bean
public AsyncTaskExecutor applicationTaskExecutor() {
    return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
}
```

### 2. 多级缓存

L1 (Caffeine) + L2 (Redis) 降低数据库压力：

```
平均响应时间：
- L1 Cache Hit: < 1ms
- L2 Cache Hit: < 10ms
- Database Query: 50-100ms
```

### 3. 连接池优化

- HikariCP：数据库连接池
- Lettuce/Redisson：Redis 连接池
- HTTP Client：连接复用

### 4. 数据库优化

- 读写分离
- 分库分表（ShardingSphere）
- 慢查询监控（P6Spy）

## 可观测性

### 1. 日志

- **应用日志**：Logback + MDC（Trace ID / User ID）
- **访问日志**：Access Log
- **审计日志**：Security Audit Log
- **日志聚合**：ELK / Loki

### 2. 指标

- **JVM 指标**：内存、GC、线程
- **应用指标**：QPS、延迟、错误率
- **业务指标**：用户活跃度、API 调用量
- **监控工具**：Micrometer + Prometheus

### 3. 链路追踪

- **Spring Cloud Sleuth**：分布式追踪
- **Zipkin / Jaeger**：链路可视化
- **Trace Context**：跨服务传播

### 4. 告警

- **阈值告警**：CPU、内存、QPS
- **异常告警**：Error 日志、业务异常
- **SLA 告警**：接口可用性
- **告警渠道**：钉钉、邮件、短信

## 最佳实践

### 1. 模块划分

- 按业务域划分模块
- 避免循环依赖
- 接口与实现分离

### 2. 异常处理

- 统一异常处理（`@ControllerAdvice`）
- 业务异常与系统异常分离
- 错误码规范

### 3. 事务管理

- 最小化事务范围
- 避免长事务
- 合理使用 `@Transactional` 传播级别

### 4. API 设计

- RESTful 风格
- 统一响应格式
- 版本控制（路径或 Header）

### 5. 配置管理

- 敏感信息加密（Jasypt）
- 配置中心化（Nacos Config）
- 环境隔离（dev / test / prod）

## 下一步阅读

- [模块详解](./modules.md)
- [设计模式](./design-patterns.md)
- [快速开始](../guides/quick-start.md)
- [开发指南](../guides/development.md)
