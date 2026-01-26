# 技术需求文档 | Technical Requirements Document (TRD)

## 1. 技术架构

### 1.1 整体架构

- **架构模式**：分层架构 + 微服务架构（可选）
- **前后端分离**：RESTful API + Vue 3 SPA
- **数据存储**：MySQL/PostgreSQL + Redis
- **消息队列**：Kafka（可选）
- **服务注册**：Nacos（可选）

### 1.2 技术选型

| 层级 | 技术栈 | 版本 | 选型理由 |
|------|--------|------|----------|
| **核心框架** |
| Java | OpenJDK | 25 | 虚拟线程、最新 API |
| Spring Boot | Spring Boot | 4.0.1 | 生态成熟、自动配置 |
| Spring Cloud | Spring Cloud | 2025.1.0 | 微服务生态 |
| **Web** |
| Spring MVC | Spring MVC | 7.x | MVC 标准框架 |
| Spring Security | Spring Security | 7.x | 安全标准 |
| **数据访问** |
| MyBatis Plus | MyBatis Plus | 3.5.15 | 增强 MyBatis |
| Spring Data JPA | Spring Data JPA | 4.x | ORM 标准 |
| HikariCP | HikariCP | 最新 | 高性能连接池 |
| **缓存** |
| Caffeine | Caffeine | 最新 | 高性能本地缓存 |
| Redis | Redis | 7+ | 分布式缓存 |
| Redisson | Redisson | 4.0.0 | Redis 客户端 |
| **AI** |
| Spring AI | Spring AI | 2.0.0-M1 | Spring 官方 AI 框架 |
| LangChain4j | LangChain4j | 1.9.1 | AI 应用编排 |
| **工具** |
| Lombok | Lombok | 1.18.42 | 简化代码 |
| MapStruct | MapStruct | 1.6.3 | Bean 映射 |
| Guava | Guava | 33.5.0 | Google 工具库 |

## 2. 技术实现

### 2.1 认证授权

#### 2.1.1 OAuth2.1 授权服务器

**技术方案**：
- 基于 Spring Authorization Server
- 支持 Authorization Code + PKCE 流程
- JWT Access Token + Opaque Refresh Token
- 多租户 Issuer 隔离

**核心组件**：
```java
// 授权服务器配置
@Configuration
public class AuthorizationServerConfig {
    
    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
```

#### 2.1.2 多登录方式

**技术方案**：
- 用户名密码：BCrypt 加密
- 短信验证码：Redis 存储 + 6位数字码
- 社交登录：JustAuth 集成

### 2.2 多租户

#### 2.2.1 租户识别

**技术方案**：
- 主要方式：Path 路径（`/t/{tenant}/`）
- 备用方式：Subdomain 子域名
- 兜底方式：Header 请求头

**实现代码**：
```java
@Component
public class TenantResolver {
    
    public String resolve(HttpServletRequest request) {
        // 1. 从路径解析
        String tenant = extractFromPath(request.getRequestURI());
        if (tenant != null) {
            return tenant;
        }
        
        // 2. 从子域名解析
        tenant = extractFromSubdomain(request.getServerName());
        if (tenant != null) {
            return tenant;
        }
        
        // 3. 从 Header 解析
        return request.getHeader("X-Tenant-ID");
    }
}
```

#### 2.2.2 数据隔离

**技术方案**：
- **共享表**：添加 `tenant_id` 字段
- **独立表**：每个租户独立表（可选）
- **独立库**：每个租户独立数据库（可选）

### 2.3 缓存架构

#### 2.3.1 多级缓存

**技术方案**：
- **L1 缓存**：Caffeine（进程内）
- **L2 缓存**：Redis（分布式）
- **缓存同步**：Redis Pub/Sub

**缓存流程**：
```
1. 查询 L1 Cache (Caffeine)
   ├─ Hit: 返回数据
   └─ Miss: 继续
2. 查询 L2 Cache (Redis)
   ├─ Hit: 更新 L1 + 返回数据
   └─ Miss: 继续
3. 查询 Database
   ├─ 更新 L1 Cache
   ├─ 更新 L2 Cache
   └─ 返回数据
```

#### 2.3.2 缓存失效

**技术方案**：
- **主动失效**：更新/删除时发布失效事件
- **被动失效**：TTL 过期
- **同步机制**：Redis Pub/Sub 广播

### 2.4 AI 集成

#### 2.4.1 多模型支持

**技术方案**：
- **统一接口**：ChatClient / EmbeddingClient
- **模型配置**：application.yml 配置切换
- **负载均衡**：轮询/权重分配

#### 2.4.2 RAG 实现

**技术方案**：
- **文档加载**：PDF / Word / Markdown
- **向量化**：Embedding Model
- **向量存储**：Milvus / Qdrant / Chroma
- **检索**：语义相似度搜索
- **生成**：LLM + Context

**RAG 流程**：
```
1. 文档预处理
   ├─ 分块 (Chunking)
   ├─ 向量化 (Embedding)
   └─ 存储 (Vector Store)
   
2. 查询处理
   ├─ 向量化查询
   ├─ 检索相似文档
   ├─ 构建 Prompt
   └─ LLM 生成答案
```

## 3. 性能优化

### 3.1 数据库优化

- **连接池**：HikariCP（最小 10，最大 50）
- **索引优化**：主键 + 唯一索引 + 组合索引
- **查询优化**：避免 SELECT *，使用分页
- **读写分离**：主库写，从库读
- **分库分表**：ShardingSphere（可选）

### 3.2 缓存优化

- **缓存预热**：启动时加载热点数据
- **缓存穿透**：布隆过滤器
- **缓存击穿**：互斥锁
- **缓存雪崩**：过期时间随机化

### 3.3 并发优化

- **虚拟线程**：Java 25 Virtual Threads
- **异步处理**：@Async + CompletableFuture
- **线程池**：合理配置核心线程数

## 4. 安全要求

### 4.1 传输安全

- **HTTPS**：强制 HTTPS，禁用 HTTP
- **HSTS**：Strict-Transport-Security
- **CSP**：Content-Security-Policy

### 4.2 存储安全

- **密码**：BCrypt 加密（强度 10）
- **敏感数据**：AES-256 加密
- **数据库**：启用 TDE（可选）

### 4.3 接口安全

- **认证**：JWT Token 验证
- **授权**：RBAC 权限控制
- **限流**：Resilience4j RateLimiter
- **防 XSS**：输入过滤 + 输出转义
- **防 CSRF**：CSRF Token

## 5. 监控与日志

### 5.1 监控指标

| 类别 | 指标 | 工具 |
|------|------|------|
| **JVM** | 内存、GC、线程 | Micrometer |
| **应用** | QPS、延迟、错误率 | Micrometer |
| **数据库** | 连接数、慢查询 | P6Spy |
| **缓存** | 命中率、内存使用 | Redis Monitor |
| **业务** | 用户活跃度、订单量 | 自定义指标 |

### 5.2 日志规范

**日志级别**：
- **ERROR**：系统错误，需要立即处理
- **WARN**：警告信息，可能影响功能
- **INFO**：业务关键信息
- **DEBUG**：调试信息（开发环境）
- **TRACE**：详细追踪（开发环境）

**日志格式**：
```
[时间] [级别] [线程] [类名] [TraceID] - 日志内容
```

## 6. 测试要求

### 6.1 单元测试

- **覆盖率**：> 80%
- **工具**：JUnit 5 + Mockito
- **规范**：Given-When-Then

### 6.2 集成测试

- **工具**：Spring Boot Test + TestContainers
- **范围**：API + 数据库 + Redis

### 6.3 性能测试

- **工具**：JMeter / Gatling
- **指标**：TPS、响应时间、并发用户

## 7. 部署要求

### 7.1 环境要求

| 环境 | 配置 |
|------|------|
| **开发** | 2C4G |
| **测试** | 4C8G |
| **生产** | 8C16G（最小） |

### 7.2 部署方式

- **单体**：JAR + Systemd
- **容器**：Docker + Docker Compose
- **K8s**：Deployment + Service + Ingress

### 7.3 高可用

- **应用层**：多实例 + 负载均衡
- **数据层**：主从复制 + 读写分离
- **缓存层**：Redis Cluster + Sentinel

## 8. 开发工具

### 8.1 IDE

- **Java**：IntelliJ IDEA Ultimate
- **前端**：VS Code

### 8.2 插件

- Lombok Plugin
- MyBatis X
- Spring Tools
- Volar (Vue 3)

### 8.3 代码质量

- **静态检查**：SonarQube
- **代码格式**：Google Java Style
- **依赖检查**：OWASP Dependency-Check

## 9. 附录

### 9.1 相关文档

- [产品需求文档](./product-requirements.md)
- [架构设计文档](../architecture/overview.md)
- [开发指南](../guides/development.md)

### 9.2 参考资料

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Spring Security 文档](https://spring.io/projects/spring-security)
- [Spring Authorization Server 文档](https://spring.io/projects/spring-authorization-server)
