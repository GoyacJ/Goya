# 模块详解 | Modules Guide

本文档详细介绍 Goya 框架的各个模块及其职责。

## 模块总览

```
Goya/
├── bom/                       # 依赖版本管理
├── component/                 # 公共组件（12个模块）
│   ├── component-framework/   # 框架基础（11个子模块）
│   ├── component-redis/       # Redis 实现
│   ├── component-kafka/       # Kafka 消息
│   ├── component-rabbitmq/    # RabbitMQ 消息
│   ├── component-mybatisplus/ # MyBatis Plus
│   ├── component-captcha/     # 验证码
│   ├── component-security/    # 安全模块（4个子模块）
│   ├── component-social/      # 社交登录
│   ├── component-oss-aliyun/  # 阿里云 OSS
│   ├── component-oss-s3/      # AWS S3
│   ├── component-oss-minio/   # MinIO
│   └── component-service/     # 服务抽象
├── ai/                        # AI 模块（5个子模块）
├── platform/                  # 平台应用（2个子模块）
└── cloud/                     # 云原生支持
```

---

## 一、BOM 模块

### 📦 bom

**职责**：统一管理项目所有依赖的版本

**Maven Coordinates**：
```xml
<dependency>
    <groupId>com.ysmjjsy.goya</groupId>
    <artifactId>bom</artifactId>
    <version>1.0.0</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

**核心依赖**：
- Spring Boot 4.0.1
- Spring Cloud 2025.1.0
- Spring AI 2.0.0-M1
- LangChain4j 1.9.1
- MyBatis Plus 3.5.16
- Redisson 4.0.0

---

## 二、Framework 框架基础模块

### 🛠️ component-framework

**职责**：框架基础设施聚合模块，包含 11 个子模块

#### framework-core

**职责**：核心工具类和基础定义

**主要功能**：
- 常用工具类（日期、字符串、集合等）
- 业务异常基类
- 结果响应封装（Response）
- 基础接口定义

**核心类**：
- `ApiRes<T>`：统一响应封装（Record 类型，支持 Builder 模式）
- `BaseException`：业务异常基类
- `IStrategy` + `StrategyChoose`：策略模式实现
- `IChainHandler` + `ChainContext`：责任链模式实现

**使用示例**：
```java
@RestController
public class UserController {
    
    public ApiRes<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return ApiRes.ok(user);
    }
}
```

---

#### framework-common

**职责**：公共组件和通用工具

**主要功能**：
- IP 地理位置解析（ip2region）
- 常用工具类扩展
- 通用数据结构

---

#### framework-masker

**职责**：数据脱敏

**主要功能**：
- 手机号脱敏（`138****8888`）
- 身份证脱敏（`110***********1234`）
- 邮箱脱敏（`a***@example.com`）
- 银行卡脱敏
- 自定义脱敏规则

**使用示例**：
```java
@Data
public class UserVO {
    
    private String username;
    
    @Mask(type = MaskType.MOBILE)
    private String mobile;
    
    @Mask(type = MaskType.ID_CARD)
    private String idCard;
}
```

---

#### framework-crypto

**职责**：加密解密工具

**主要功能**：
- 对称加密（AES、SM4）
- 非对称加密（RSA、SM2）
- 摘要算法（MD5、SHA、SM3）
- 国密算法支持

**使用示例**：
```java
@Service
public class SecurityService {
    
    @Autowired
    private CryptoService cryptoService;
    
    public String encrypt(String data) {
        return cryptoService.aesEncrypt(data, secretKey);
    }
}
```

---

#### framework-cache

**职责**：缓存抽象层

**主要功能**：
- 统一缓存接口定义
- 缓存配置抽象
- 缓存事件

**核心接口**：
```java
public interface ICache {
    <T> T get(String key, Class<T> type);
    void put(String key, Object value);
    void put(String key, Object value, Duration ttl);
    void evict(String key);
    void clear();
}
```

---

#### framework-bus

**职责**：消息总线抽象

**主要功能**：
- 消息发布订阅抽象
- 事件总线接口
- 消息处理器定义

**核心接口**：
```java
public interface IMessageBus {
    void publish(String topic, Object message);
    void subscribe(String topic, Consumer<Object> handler);
}

public interface IntegrationBusBinder extends IMessageBus {
    // Spring Integration 集成
}
```

---

#### framework-log

**职责**：日志增强

**主要功能**：
- 操作日志（`@OperationLog`）
- 审计日志
- 慢查询日志
- 日志脱敏

**使用示例**：
```java
@RestController
public class UserController {
    
    @OperationLog(module = "用户管理", operation = "创建用户")
    @PostMapping("/users")
    public User createUser(@RequestBody UserDTO dto) {
        return userService.create(dto);
    }
}
```

---

#### framework-oss

**职责**：对象存储抽象

**主要功能**：
- 统一存储接口
- 文件操作抽象
- 元数据管理

**核心接口**：
```java
public interface IOssService {
    String upload(InputStream inputStream, String fileName);
    InputStream download(String key);
    void delete(String key);
    String getUrl(String key, Duration expiration);
}
```

---

#### framework-security

**职责**：权限决策内核（SRA 策略模型）

**主要功能**：
- SRA 模型（Subject / Resource / Action）
- 策略决策（ALLOW / DENY）
- 行级过滤（JSON DSL → SQL）
- 列级约束（允许/拒绝字段）
- 权限变更事件（发布/订阅，基于 framework-bus）

**核心类**：
- `AuthorizationService`：鉴权入口
- `PolicyEngine` / `DefaultPolicyEngine`：策略评估
- `DecisionEvaluator`：决策合并
- `RangeDslParser` / `RangeFilterBuilder`：DSL 解析与过滤器构建
- `PermissionChangePublisher` / `PermissionChangeSubscriber`：权限变更事件

---

#### framework-servlet

**职责**：Servlet 增强

**主要功能**：
- 统一异常处理（`@ControllerAdvice`）
- 全局响应包装
- XSS 防护
- 参数加解密
- 接口限流
- 跨域配置
- Swagger 文档增强

**核心组件**：
- `GlobalExceptionHandler`：全局异常处理
- `ResponseBodyAdvice`：响应包装
- `XssFilter`：XSS 过滤器
- `DecryptRequestParamResolver`：参数解密

---

#### framework-boot-starter

**职责**：自动配置启动器

**主要功能**：
- 框架自动配置
- Bean 注册
- 默认配置

---

## 三、Redis 模块

### 💾 component-redis

**职责**：基于 Redisson 的 Redis 增强实现

**主要功能**：

| 功能 | 类 | 说明 |
|------|---|------|
| 缓存服务 | `RedissonCacheService` | 统一缓存操作 |
| 分布式锁 | `RedissonLockService` | 可重入锁/公平锁/读写锁 |
| 布隆过滤器 | `RedissonBloomFilterService` | 防止缓存穿透 |
| 延迟队列 | `RedissonDelayedQueueService` | 延迟任务 |
| 可靠延迟队列 | `RedissonReliableDelayedQueueService` | 带确认机制 |
| 限流器 | `RedissonRateLimiterService` | 令牌桶限流 |
| Topic 消息 | `RedissonTopicService` | 发布订阅 |
| 原子操作 | `RedissonAtomicService` | 原子计数器 |

**使用示例**：
```java
@Service
public class OrderService {
    
    @Autowired
    private RedisLockService lockService;
    
    public void createOrder(Order order) {
        String lockKey = "order:create:" + order.getUserId();
        
        lockService.tryLock(lockKey, 10, TimeUnit.SECONDS, () -> {
            // 业务逻辑
            orderRepository.save(order);
        });
    }
}
```

**配置**：
```yaml
goya:
  redis:
    enabled: true
    address: redis://localhost:6379
```

---

## 四、消息总线模块

### 📮 component-kafka

**职责**：Kafka 消息总线实现

**核心类**：
- `KafkaIntegrationBusBinder`：Kafka Binder 实现

**配置**：
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

### 📮 component-rabbitmq

**职责**：RabbitMQ 消息总线实现

**核心类**：
- `RabbitIntegrationBusBinder`：RabbitMQ Binder 实现

**配置**：
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
```

---

## 五、数据库模块

### 🗄️ component-mybatisplus

**职责**：MyBatis Plus 增强

**主要功能**：
- 动态数据源
- 多租户数据隔离（TenantLine）
- 数据权限执行（SRA 策略 → JSON DSL → SQL）
- 列级约束（SELECT/WHERE/ORDER/GROUP/HAVING）
- 权限变更拦截并发布事件（framework-bus）
- 审计字段自动填充
- 分页插件
- SQL 监控（P6Spy）
- 逻辑删除
- 乐观锁

**使用示例**：
```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    @TableLogic
    private Boolean deleted;
}

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 CRUD 方法
}
```

---

## 六、安全模块

### 🔐 component-security

**职责**：安全认证授权体系

包含 4 个子模块：

#### security-core

**职责**：安全核心领域模型和 SPI 定义

**核心类**：
- `SecurityUser`：安全用户模型（Builder 模式）
- `SecurityPermission`：权限模型
- `SecurityTenant`：租户模型
- `SecurityAttribute`：安全属性
- `GoyaSecurityContext`：安全上下文

**SPI 接口**：
```java
public interface IUserService {
    SecurityUser loadUserByUsername(String username);
    SecurityUser loadUserByMobile(String mobile);
}

public interface ITenantService {
    SecurityTenant getTenant(String tenantId);
}

public interface IRolePermissionService {
    List<SecurityPermission> getPermissions(String userId);
}

public interface IOtpService {
    void sendOtp(String mobile);
    boolean verifyOtp(String mobile, String code);
}

public interface ISocialUserService {
    SecurityUser loadUserBySocialId(String socialType, String socialId);
}
```

---

#### security-authentication

**职责**：认证层，支持多种登录方式

**核心功能**：
- 用户名密码登录
- 短信验证码登录
- 第三方登录
- 验证码校验
- 登录失败处理
- 设备管理

**核心类**：
- `UsernamePasswordAuthenticationProvider`：用户名密码认证
- `SmsAuthenticationProvider`：短信认证
- `SocialAuthenticationProvider`：社交登录认证
- `CaptchaValidationFilter`：验证码校验过滤器
- `DeviceManagementFilter`：设备管理过滤器
- `LoginFailureCacheManger`：登录失败次数管理

**配置**：
```yaml
goya:
  security:
    authentication:
      login:
        allow-password-login: true
        allow-sms-login: true
```

---

#### security-authorization

**职责**：资源服务器，JWT 验证和权限控制

**核心功能**：
- JWT Token 验证
- Token 黑名单
- 多租户 Issuer 解析
- Scope/Role 鉴权
- DPoP 支持

**核心类**：
- `JwtAuthenticationFilter`：JWT 认证过滤器
- `JwtAuthorityConverter`：JWT 权限转换
- `JwtBlacklistValidator`：黑名单校验
- `ResourceServerDPoPValidator`：DPoP 验证

**配置**：
```yaml
goya:
  security:
    resource:
      jwt:
        issuer-uri: https://auth.example.com
      token-blacklist:
        enabled: true
```

---

#### security-oauth2

**职责**：OAuth2.1 授权服务器

**核心功能**：
- OAuth2.1 授权流程（Authorization Code + PKCE）
- OIDC Provider（Discovery / UserInfo / JWK）
- Token 定制（自定义 Claims）
- 多租户 Issuer
- 授权存储 SPI

---

## 七、OSS 对象存储模块

### 📦 component-oss-aliyun

**职责**：阿里云 OSS 实现

**主要功能**：
- 文件上传/下载
- Bucket 管理
- 访问控制
- 生命周期管理
- 图片处理
- 视频处理

---

### 📦 component-oss-s3

**职责**：AWS S3 实现

**主要功能**：
- S3 兼容 API
- 分片上传
- 预签名 URL

---

### 📦 component-oss-minio

**职责**：MinIO 实现

**主要功能**：
- MinIO 原生 API
- 私有化部署支持

---

## 八、其他模块

### 🔢 component-captcha

**职责**：验证码生成与校验

**支持类型**：
- 算术验证码
- 滑块验证码
- 拼图验证码
- 文字点选验证码

---

### 👥 component-social

**职责**：社交登录集成

**支持平台**：
- 微信小程序
- 微信公众号
- 第三方平台（基于 JustAuth）

---

### 📋 component-service

**职责**：服务抽象层

**主要功能**：
- 服务接口定义
- 远程调用抽象

---

## 九、AI 模块

### 🤖 ai-spring

**职责**：Spring AI 集成

**核心功能**：
- 多模型统一接口（OpenAI / Qwen / Local）
- ChatClient / EmbeddingClient
- Prompt 模板管理
- Function Calling

---

### 🧠 ai-model

**职责**：模型管理

**核心功能**：
- 模型配置管理
- 模型切换
- 模型监控

---

### 📚 ai-rag

**职责**：检索增强生成

**核心功能**：
- 文档向量化
- 向量存储
- 语义检索
- 答案生成

---

### 🔌 ai-mcp

**职责**：Model Context Protocol 支持

**核心功能**：
- MCP 协议实现
- 上下文管理
- 工具调用

---

### 🎬 ai-video

**职责**：视频 AI 处理

**核心功能**：
- 视频帧提取（FFmpeg）
- 图像识别（OpenCV）
- 视频分析
- 目标检测

---

## 十、Platform 平台应用

### 🏢 platform-monolith

**职责**：单体应用

**适用场景**：
- 小型项目
- 快速原型
- 开发测试

---

### ☁️ platform-distributed

**职责**：微服务应用

**适用场景**：
- 大型项目
- 高并发
- 云原生部署

---

## 模块依赖关系

```
           ┌──────────────┐
           │   Platform   │
           │ (Application)│
           └──────┬───────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
 ┌──▼───┐    ┌───▼───┐    ┌────▼────┐
 │ AI   │    │Security│    │Component│
 └──┬───┘    └───┬───┘    └────┬────┘
    │            │             │
    └────────────┼─────────────┘
                 │
        ┌────────▼────────┐
        │    Framework    │
        │ (core/common/   │
        │  servlet/cache) │
        └─────────────────┘
```

## 下一步阅读

- [模块详细文档](./modules-detailed.md) - 深入了解各模块的技术细节和使用示例
- [架构概览](./overview.md)
- [设计模式](./design-patterns.md)
- [开发指南](../guides/development.md)
