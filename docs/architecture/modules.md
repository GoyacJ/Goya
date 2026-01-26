# 模块详解 | Modules Guide

本文档详细介绍 Goya 框架的各个模块及其职责。

## 模块总览

```
Goya/
├── bom/                       # 依赖版本管理
├── component/                 # 公共组件（11个子模块）
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
- MyBatis Plus 3.5.15
- Redisson 4.0.0

---

## 二、Component 组件模块

### 🛠️ component-core

**职责**：核心工具类和基础定义

**主要功能**：
- 常用工具类（日期、字符串、集合等）
- 业务异常基类
- 结果响应封装
- IP 地理位置解析（ip2region）

**使用示例**：
```java
@Slf4j
@RestController
public class UserController {
    
    public Response<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Response.ok(user);
    }
}
```

---

### 🏗️ component-framework

**职责**：框架基础设施

**主要功能**：
- 应用上下文工具
- Bean 工厂增强
- 事件总线
- 策略模式支持

**核心类**：
- `ApplicationContextHolder`：Spring 上下文持有者
- `IStrategy` + `StrategyChoose`：策略模式实现
- `IChainHandler` + `ChainContext`：责任链模式实现

**使用示例**：
```java
// 策略模式
public interface PaymentStrategy extends IStrategy {
    void pay(Order order);
}

@Component("alipayStrategy")
public class AlipayStrategy implements PaymentStrategy {
    @Override
    public void pay(Order order) {
        // 支付宝支付逻辑
    }
}

// 使用
PaymentStrategy strategy = StrategyChoose.choose("alipay", PaymentStrategy.class);
strategy.pay(order);
```

---

### 🌐 component-web

**职责**：Web 层增强

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

**使用示例**：
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // 自动包装为 Response<User>
        return userService.getById(id);
    }
    
    @PostMapping
    public User createUser(@Decrypt @RequestBody UserDTO dto) {
        // 自动解密
        return userService.create(dto);
    }
}
```

---

### 🔐 component-security

**职责**：安全认证授权体系

包含 4 个子模块：

#### security-core

**职责**：安全核心领域模型和 SPI 定义

**核心类**：
- `SecurityUser`：安全用户模型（Builder 模式）
- `SecurityPermission`：权限模型
- `IUserService`：用户服务 SPI
- `IConstants`：安全常量

**使用示例**：
```java
public interface IUserService {
    SecurityUser loadUserByUsername(String username);
    SecurityUser loadUserByMobile(String mobile);
}

@Service
public class UserServiceImpl implements IUserService {
    @Override
    public SecurityUser loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return SecurityUser.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getRoles())
            .build();
    }
}
```

#### security-authentication

**职责**：认证层，支持多种登录方式

**核心功能**：
- 用户名密码登录
- 短信验证码登录
- 第三方登录（预留接口）
- 验证码校验
- 登录失败处理

**核心类**：
- `LoginAuthenticationConverter`：登录请求转换器
- `UsernamePasswordAuthenticationProvider`：用户名密码认证
- `SmsAuthenticationProvider`：短信认证
- `CaptchaValidator`：验证码校验

**配置**：
```yaml
goya:
  security:
    authentication:
      login:
        allow-password-login: true
        allow-sms-login: true
        login-url: /login
        success-url: /
        failure-url: /login?error
```

#### security-authorization

**职责**：资源服务器，JWT 验证和权限控制

**核心功能**：
- JWT Token 验证
- Token 黑名单
- 多租户 Issuer 解析
- Scope/Role 鉴权
- DPoP 支持（可选）

**核心类**：
- `JwtAuthenticationConverter`：JWT 转换
- `JwtBlacklistValidator`：黑名单校验
- `MultiTenantJwtDecoder`：多租户 JWT 解码

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

#### security-oauth2

**职责**：OAuth2.1 授权服务器

**核心功能**：
- OAuth2.1 授权流程（Authorization Code + PKCE）
- OIDC Provider（Discovery / UserInfo / JWK）
- Token 定制（自定义 Claims）
- 多租户 Issuer
- 授权存储 SPI

**核心类**：
- `SecurityAuthorizationServerAutoConfiguration`：授权服务器自动配置
- `OAuth2TokenCustomizer`：Token 定制
- `MultiTenantIssuerResolver`：多租户 Issuer 解析

**SPI 接口**：
```java
public interface IRegisteredClientService {
    RegisteredClient findByClientId(String clientId);
}

public interface IOAuth2AuthorizationService {
    void save(OAuth2Authorization authorization);
    OAuth2Authorization findByToken(String token, TokenType tokenType);
}
```

---

### 💾 component-cache

**职责**：多级缓存解决方案

包含 4 个子模块：

#### cache-core

**职责**：缓存抽象层

**核心接口**：
```java
public interface ICache {
    <T> T get(String key, Class<T> type);
    void put(String key, Object value);
    void evict(String key);
    void clear();
}
```

#### cache-caffeine

**职责**：Caffeine 本地缓存实现

**特性**：
- 高性能本地缓存
- LRU / LFU 淘汰策略
- 过期时间配置
- 缓存统计

**配置**：
```yaml
goya:
  cache:
    caffeine:
      enabled: true
      maximum-size: 10000
      expire-after-write: 10m
```

#### cache-redis

**职责**：Redis 分布式缓存实现

**特性**：
- Redisson 客户端
- 多集群支持
- Pub/Sub 缓存同步
- 序列化定制（JSON/Protobuf）

**配置**：
```yaml
goya:
  cache:
    redis:
      enabled: true
      cluster:
        - redis://localhost:6379
      codec: json
```

#### cache-multi-level

**职责**：多级缓存（L1 + L2）

**特性**：
- L1 (Caffeine) + L2 (Redis)
- 自动缓存同步（Redis Pub/Sub）
- 缓存穿透/击穿/雪崩防护

**使用示例**：
```java
@Service
public class UserService {
    
    @Autowired
    private ICache cache;
    
    @Cacheable(key = "'user:' + #id")
    public User getUser(Long id) {
        // L1 Miss -> L2 -> DB
        return userRepository.findById(id).orElse(null);
    }
}
```

---

### 📮 component-bus

**职责**：消息总线抽象

包含 3 个子模块：

#### bus-core

**职责**：消息总线抽象接口

**核心接口**：
```java
public interface IMessageBus {
    void publish(String topic, Object message);
    void subscribe(String topic, Consumer<Object> handler);
}
```

#### bus-stream

**职责**：Spring Cloud Stream 抽象

**特性**：
- Binder 抽象（Kafka / RabbitMQ）
- 消息路由
- 错误处理

#### bus-kafka-boot-starter

**职责**：Kafka 实现的 Starter

**使用示例**：
```java
@Service
public class OrderService {
    
    @Autowired
    private IMessageBus messageBus;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        messageBus.publish("order-created", order);
    }
}

@Component
public class OrderEventListener {
    
    @PostConstruct
    public void init() {
        messageBus.subscribe("order-created", this::handleOrderCreated);
    }
    
    private void handleOrderCreated(Order order) {
        // 处理订单创建事件
    }
}
```

---

### 🗄️ component-database

**职责**：数据库增强

包含 3 个子模块：

#### database-core

**职责**：数据库核心抽象

**核心功能**：
- 审计字段自动填充
- 多租户数据隔离
- 逻辑删除
- 乐观锁

#### database-mybatisplus-boot-starter

**职责**：MyBatis Plus 增强

**特性**：
- 动态数据源
- 分页插件
- SQL 监控（P6Spy）
- 字段加密

**使用示例**：
```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    @FieldEncrypt
    private String mobile;
    
    @TableLogic
    private Boolean deleted;
}

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 CRUD 方法
}
```

#### database-jpa-boot-starter

**职责**：Spring Data JPA 增强

**特性**：
- QueryDSL 支持
- Specification 增强
- 审计字段

---

### 📦 component-oss

**职责**：对象存储统一接口

包含 4 个子模块：

#### oss-core

**职责**：OSS 抽象接口

**核心接口**：
```java
public interface IOssService {
    String upload(InputStream inputStream, String fileName);
    InputStream download(String key);
    void delete(String key);
    String getUrl(String key, Duration expiration);
}
```

#### oss-aliyun / oss-minio / oss-s3

**职责**：阿里云 OSS / MinIO / AWS S3 实现

**使用示例**：
```java
@Service
public class FileService {
    
    @Autowired
    private IOssService ossService;
    
    public String uploadFile(MultipartFile file) {
        return ossService.upload(file.getInputStream(), file.getOriginalFilename());
    }
}
```

---

### 🔢 component-captcha

**职责**：验证码生成与校验

**支持类型**：
- 算术验证码
- 滑块验证码
- 拼图验证码
- 文字点选验证码

**使用示例**：
```java
@RestController
public class CaptchaController {
    
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/captcha")
    public CaptchaVO getCaptcha() {
        return captchaService.generate(CaptchaTypeEnum.SLIDER);
    }
    
    @PostMapping("/captcha/verify")
    public boolean verifyCaptcha(@RequestBody CaptchaVerifyDTO dto) {
        return captchaService.verify(dto);
    }
}
```

---

### 👥 component-social

**职责**：社交登录集成

**支持平台**：
- 微信小程序
- 微信公众号
- 第三方平台（基于 JustAuth）

**使用示例**：
```java
@Service
public class SocialLoginService {
    
    @Autowired
    private SocialManager socialManager;
    
    public SocialUser wechatLogin(String code) {
        return socialManager.login(SocialTypeEnum.WECHAT_MINI, code);
    }
}
```

---

### 📝 component-log

**职责**：日志增强和操作审计

**核心功能**：
- 操作日志（`@OperationLog`）
- 审计日志
- 慢查询日志
- 敏感信息脱敏

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

## 三、AI 模块

### 🤖 ai-spring

**职责**：Spring AI 集成

**核心功能**：
- 多模型统一接口（OpenAI / Qwen / Local）
- ChatClient / EmbeddingClient
- Prompt 模板管理
- Function Calling

**使用示例**：
```java
@Service
public class AiService {
    
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String userMessage) {
        return chatClient.call(userMessage);
    }
}
```

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
- 向量存储（Milvus / Qdrant）
- 语义检索
- 答案生成

**使用示例**：
```java
@Service
public class KnowledgeService {
    
    @Autowired
    private RagService ragService;
    
    public String query(String question) {
        return ragService.query(question);
    }
}
```

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

## 四、Platform 平台应用

### 🏢 platform-monolith

**职责**：单体应用

**子模块**：
- `auth-server`：认证服务器

**适用场景**：
- 小型项目
- 快速原型
- 开发测试

---

### ☁️ platform-distributed

**职责**：微服务应用

**子模块**：
- `auth-cloud-server`：微服务版认证服务

**适用场景**：
- 大型项目
- 高并发
- 云原生部署

---

## 五、Cloud 模块

### ☁️ cloud

**职责**：云原生支持

**核心功能**：
- Kubernetes 部署配置
- Service Mesh 集成
- Istio 配置

---

## 模块依赖关系

```
           ┌──────────────┐
           │   Platform   │
           │ (Application)│
           └──────┬───────┘
                  │
        ┌─────────┼─────────┐
        │         │         │
   ┌────▼────┐ ┌─▼──┐ ┌────▼────┐
   │Security │ │ AI │ │Component│
   └────┬────┘ └─┬──┘ └────┬────┘
        │        │         │
        └────────┼─────────┘
                 │
           ┌─────▼─────┐
           │Framework  │
           │   Core    │
           └───────────┘
```

## 下一步阅读

- [架构概览](./overview.md)
- [设计模式](./design-patterns.md)
- [开发指南](../guides/development.md)
