# 模块详细文档 | Detailed Modules Documentation

本文档提供 Goya 框架各模块的详细技术文档，包括核心类、接口、配置和使用示例。

## 目录

- [Framework 框架基础模块](#framework-框架基础模块)
- [Redis 模块](#redis-模块)
- [安全模块](#安全模块)
- [MyBatis Plus 模块](#mybatis-plus-模块)
- [验证码模块](#验证码模块)
- [OSS 对象存储模块](#oss-对象存储模块)
- [消息总线模块](#消息总线模块)
- [AI 模块](#ai-模块)

---

## Framework 框架基础模块

### framework-core

**职责**：核心工具类和基础定义

**核心类**：

#### 1. ApiRes<T> - 统一响应封装

```java
public record ApiRes<T>(
    boolean success,                    // 是否成功
    String code,                        // 稳定错误码/成功码（成功时固定 OK）
    String message,                     // 对外安全文案
    String traceId,                     // 链路追踪标识
    LocalDateTime timestamp,            // 响应时间（UTC）
    T data,                             // 成功数据
    String path,                        // 请求地址
    Map<String, Object> meta,           // 扩展信息（分页/统计等）
    List<ApiFieldError> fieldErrors    // 字段级错误
) implements IResponse {
    
    // 静态工厂方法
    public static <T> ApiRes<T> ok(T data) {
        return ok(data, null, null);
    }
    
    public static <T> ApiRes<T> ok(T data, String message) {
        return ok(data, message, null);
    }
    
    // Builder 模式
    public static OkBuilder okBuilder() {
        return new OkBuilder();
    }
    
    public static FailBuilder failBuilder(ErrorCode errorCode) {
        return new FailBuilder(errorCode);
    }
}
```

**使用示例**：
```java
@RestController
public class UserController {
    @GetMapping("/users/{id}")
    public ApiRes<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return ApiRes.ok(user);
    }
    
    // 使用 Builder
    @GetMapping("/users")
    public ApiRes<List<User>> listUsers() {
        List<User> users = userService.list();
        return ApiRes.okBuilder()
            .message("查询成功")
            .meta("total", users.size())
            .data(users);
    }
    
    // 分页响应
    @GetMapping("/users/page")
    public ApiRes<List<User>> pageUsers(PageQuery query) {
        PageVO<User> page = userService.page(query);
        return ApiRes.okPage(page.getRecords(), page.getPageMeta());
    }
}
```

#### 2. SpringContext - Spring 上下文工具

```java
public class SpringContext {
    private static ConfigurableApplicationContext context;
    
    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }
    
    public static <T> T getBean(String name, Class<T> requiredType) {
        return context.getBean(name, requiredType);
    }
    
    public static boolean isVirtual() {
        return Threading.VIRTUAL.isActive(SpringContext.getBean(Environment.class));
    }
}
```

#### 3. StrategyChoose - 策略选择器

```java
public class StrategyChoose {
    public static <T extends IStrategy> T choose(String strategyId, Class<T> strategyInterface) {
        // 根据策略 ID 选择策略实现
    }
}
```

**使用示例**：
```java
@Service
public class PaymentService {
    public void pay(Order order, String paymentMethod) {
        PaymentStrategy strategy = StrategyChoose.choose(paymentMethod, PaymentStrategy.class);
        strategy.pay(order);
    }
}
```

#### 4. ChainContext - 责任链上下文

```java
public class ChainContext {
    public <T, R> R next(T context) {
        // 执行下一个处理器
    }
}
```

**使用示例**：
```java
@Component
public class LoginPreValidationHandler implements ChainHandler<LoginRequest> {
    @Override
    public boolean handle(LoginRequest request) {
        // 验证逻辑
        return true; // 继续执行下一个处理器
    }
    
    @Override
    public String chainKey() {
        return "login";
    }
}
```

### framework-common

**职责**：公共组件和通用工具

**核心工具类**：

- `GoyaStringUtils`：字符串工具
- `GoyaDateUtils`：日期工具
- `GoyaCollectionUtils`：集合工具
- `GoyaMapUtils`：Map 工具
- `GoyaConvertUtils`：类型转换工具
- `GoyaRegionUtils`：IP 地理位置解析（基于 ip2region）

**使用示例**：
```java
// IP 地理位置解析
String ip = "114.114.114.114";
Region region = GoyaRegionUtils.getRegion(ip);
log.info("IP: {}, 地区: {}", ip, region.getRegion());
```

### framework-masker

**职责**：数据脱敏

**支持的脱敏类型**：
- `MOBILE`：手机号（`138****8888`）
- `ID_CARD`：身份证（`110***********1234`）
- `EMAIL`：邮箱（`a***@example.com`）
- `BANK_CARD`：银行卡
- `CUSTOM`：自定义规则

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

### framework-crypto

**职责**：加密解密工具

**支持的算法**：
- 对称加密：AES、SM4
- 非对称加密：RSA、SM2
- 摘要算法：MD5、SHA、SM3

**使用示例**：
```java
@Service
public class SecurityService {
    @Autowired
    private CryptoService cryptoService;
    
    public String encrypt(String data) {
        return cryptoService.aesEncrypt(data, secretKey);
    }
    
    public String decrypt(String encryptedData) {
        return cryptoService.aesDecrypt(encryptedData, secretKey);
    }
}
```

### framework-cache

**职责**：缓存抽象层

**核心接口**：
```java
public interface CacheService {
    // 获取缓存值
    <K, V> V get(String cacheName, K key, Class<V> type);
    
    // 写入缓存（使用默认 TTL）
    <K, V> void put(String cacheName, K key, V value);
    
    // 写入缓存（指定 TTL）
    <K, V> void put(String cacheName, K key, V value, Duration ttl);
    
    // 删除缓存项
    <K> boolean delete(String cacheName, K key);
    
    // 清空缓存区域
    void clear(String cacheName);
    
    // 获取或加载（缓存未命中则调用 loader）
    <K, V> V getOrLoad(String cacheName, K key, Supplier<V> loader);
    
    // 批量获取
    <K, V> Map<K, V> getAll(String cacheName, Collection<K> keys);
    
    // 是否存在
    <K> boolean exists(String cacheName, K key);
}
```

**使用示例**：
```java
@Service
public class UserService {
    @Autowired
    private CacheService cacheService;
    
    public User getUser(Long id) {
        // 获取缓存
        User user = cacheService.get("user", id, User.class);
        if (user != null) {
            return user;
        }
        
        // 缓存未命中，从数据库加载
        user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // 写入缓存（TTL 10 分钟）
            cacheService.put("user", id, user, Duration.ofMinutes(10));
        }
        return user;
    }
    
    // 使用 getOrLoad（自动处理缓存击穿）
    public User getUserWithLoader(Long id) {
        return cacheService.getOrLoad("user", id, () -> {
            return userRepository.findById(id).orElse(null);
        });
    }
}
```

### framework-bus

**职责**：消息总线抽象

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

### framework-log

**职责**：日志增强

**核心功能**：
- 操作日志（`@OperationLog`）
- 审计日志
- 慢查询日志
- 日志脱敏
- TraceId 追踪

**使用示例**：
```java
@RestController
public class UserController {
    @OperationLog(module = "用户管理", operation = "创建用户")
    @PostMapping("/users")
    public ApiRes<User> createUser(@RequestBody UserDTO dto) {
        User user = userService.create(dto);
        return ApiRes.ok(user);
    }
}
```

### framework-oss

**职责**：对象存储抽象

**核心接口**：
```java
public interface IOssService {
    String upload(InputStream inputStream, String fileName);
    InputStream download(String key);
    void delete(String key);
    String getUrl(String key, Duration expiration);
    boolean exists(String key);
}
```

### framework-security

**职责**：权限决策内核（SRA 策略模型）

**核心类**：
- `AuthorizationService`：鉴权入口
- `PolicyEngine`：策略评估
- `DecisionEvaluator`：决策合并
- `RangeDslParser`：DSL 解析
- `RangeFilterBuilder`：过滤器构建

**SRA 模型**：
- **Subject**：主体（用户/角色/团队/组织）
- **Resource**：资源（表/字段/API/文件）
- **Action**：操作（QUERY/CREATE/UPDATE/DELETE）

### framework-servlet

**职责**：Servlet 增强

**核心功能**：
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

## Redis 模块

### component-redis

**职责**：基于 Redisson 的 Redis 增强实现

**核心服务**：

#### 1. RedissonCacheService - 缓存服务

```java
@Service
public class UserService {
    @Autowired
    private CacheService cacheService;  // 注入 CacheService 接口
    
    public User getUser(Long id) {
        // 使用 cacheName + key 的方式
        return cacheService.getOrLoad("user", id, () -> {
            return userRepository.findById(id).orElse(null);
        });
    }
    
    public void updateUser(User user) {
        userRepository.save(user);
        // 更新缓存
        cacheService.put("user", user.getId(), user, Duration.ofMinutes(10));
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        // 删除缓存
        cacheService.delete("user", id);
    }
}
```

#### 2. RedisLockService - 分布式锁

```java
public interface RedisLockService {
    /**
     * 执行互斥逻辑（阻塞获取锁）
     */
    <T> T withLock(String lockName, Duration lease, Callable<T> task);
    
    /**
     * 尝试执行互斥逻辑（带等待时间）
     */
    <T> T tryWithLock(String lockName, Duration wait, Duration lease, Callable<T> task);
}
```

**使用示例**：
```java
@Service
public class OrderService {
    @Autowired
    private RedisLockService lockService;
    
    public Order createOrder(Order order) {
        String lockName = "order:create:" + order.getUserId();
        
        // 阻塞获取锁（锁持有时间 30 秒）
        return lockService.withLock(lockName, Duration.ofSeconds(30), () -> {
            // 业务逻辑
            return orderRepository.save(order);
        });
    }
    
    public Order tryCreateOrder(Order order) {
        String lockName = "order:create:" + order.getUserId();
        
        // 尝试获取锁（等待 10 秒，锁持有时间 30 秒）
        Order result = lockService.tryWithLock(
            lockName, 
            Duration.ofSeconds(10), 
            Duration.ofSeconds(30), 
            () -> {
                return orderRepository.save(order);
            }
        );
        
        if (result == null) {
            throw new BizException("获取锁失败，请稍后重试");
        }
        return result;
    }
}
```

**支持的锁类型**：
- 可重入锁（`RLock`）- 默认实现
- 公平锁（`RFairLock`）
- 读写锁（`RReadWriteLock`）

#### 3. RedissonBloomFilterService - 布隆过滤器

```java
@Service
public class UserService {
    @Autowired
    private RedisBloomFilterService bloomFilterService;
    
    public boolean userExists(Long userId) {
        String filterName = "user:exists";
        return bloomFilterService.contains(filterName, userId);
    }
    
    public void addUser(Long userId) {
        String filterName = "user:exists";
        bloomFilterService.add(filterName, userId);
    }
}
```

#### 4. RedissonDelayedQueueService - 延迟队列

```java
@Service
public class OrderService {
    @Autowired
    private RedisDelayedQueueService delayedQueueService;
    
    public void scheduleOrderTimeout(Order order, Duration delay) {
        delayedQueueService.enqueue("order:timeout", order, delay);
    }
    
    @Scheduled(fixedDelay = 1000)
    public void processOrderTimeout() {
        Optional<Order> order = delayedQueueService.poll("order:timeout");
        order.ifPresent(this::handleOrderTimeout);
    }
}
```

#### 5. RedissonRateLimiterService - 限流器

```java
@Service
public class ApiService {
    @Autowired
    private RedisRateLimiterService rateLimiterService;
    
    public boolean checkRateLimit(String apiKey, int permits, Duration window) {
        return rateLimiterService.tryAcquire("api:limit:" + apiKey, permits, window);
    }
}
```

#### 6. RedissonTopicService - Topic 消息

```java
@Service
public class NotificationService {
    @Autowired
    private RedisTopicService topicService;
    
    public void publishNotification(String userId, Notification notification) {
        topicService.publish("notification:" + userId, notification);
    }
    
    @PostConstruct
    public void subscribe() {
        topicService.subscribe("notification:*", (channel, message) -> {
            // 处理通知
        });
    }
}
```

#### 7. RedissonAtomicService - 原子操作

```java
@Service
public class CounterService {
    @Autowired
    private RedisAtomicService atomicService;
    
    public long increment(String counterName) {
        return atomicService.incrementAndGet(counterName);
    }
    
    public boolean compareAndSet(String counterName, long expect, long update) {
        return atomicService.compareAndSet(counterName, expect, update);
    }
}
```

**配置**：
```yaml
goya:
  redis:
    enabled: true
    address: redis://localhost:6379
    password: # 可选
    database: 0
    connection-pool-size: 64
    connection-minimum-idle-size: 24
```

---

## 安全模块

### component-security

**职责**：安全认证授权体系

包含 4 个子模块：

#### security-core

**职责**：安全核心领域模型和 SPI 定义

**核心类**：

##### SecurityUser - 安全用户模型

```java
public class SecurityUser implements UserDetails {
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
}
```

**使用示例**：
```java
SecurityUser user = SecurityUser.builder()
    .username("admin")
    .password("{bcrypt}$2a$10...")
    .authorities(authorities)
    .attribute("tenantId", "tenant-001")
    .attribute("userId", 1L)
    .build();
```

**SPI 接口**：

```java
// 用户服务 SPI
public interface IUserService {
    SecurityUser loadUserByUsername(String username);
    SecurityUser loadUserByMobile(String mobile);
}

// 租户服务 SPI
public interface ITenantService {
    SecurityTenant getTenant(String tenantId);
}

// 角色权限服务 SPI
public interface IRolePermissionService {
    List<SecurityPermission> getPermissions(String userId);
}

// OTP 服务 SPI
public interface IOtpService {
    void sendOtp(String mobile);
    boolean verifyOtp(String mobile, String code);
}

// 社交登录服务 SPI
public interface ISocialUserService {
    SecurityUser loadUserBySocialId(String socialType, String socialId);
}
```

#### security-authentication

**职责**：认证层，支持多种登录方式

**核心认证提供者**：

##### 1. PasswordAuthenticationProvider - 用户名密码认证

```java
public class PasswordAuthenticationProvider extends AbstractAuthenticationProvider {
    @Override
    protected Authentication doAuthenticate(Authentication authentication) {
        PasswordAuthenticationToken token = (PasswordAuthenticationToken) authentication;
        String username = (String) token.getPrincipal();
        String password = (String) token.getCredentials();
        
        UserDetails user = retrieveUser(username);
        if (!passwordPolicyValidator.matched(password, user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
```

##### 2. SmsAuthenticationProvider - 短信认证

```java
public class SmsAuthenticationProvider extends AbstractAuthenticationProvider {
    @Override
    protected Authentication doAuthenticate(Authentication authentication) {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        String mobile = (String) token.getPrincipal();
        String code = (String) token.getCredentials();
        
        if (!otpService.verifyOtp(mobile, code)) {
            throw new BadCredentialsException("验证码错误");
        }
        
        UserDetails user = retrieveUserByPhone(mobile);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
```

##### 3. SocialAuthenticationProvider - 社交登录认证

```java
public class SocialAuthenticationProvider extends AbstractAuthenticationProvider {
    @Override
    protected Authentication doAuthenticate(Authentication authentication) {
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
        String socialType = token.getSocialType();
        String socialId = token.getSocialId();
        
        UserDetails user = retrieveUserBySocialId(socialType, socialId);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
```

**配置**：
```yaml
goya:
  security:
    authentication:
      login:
        allow-password-login: true
        allow-sms-login: true
        allow-social-login: true
```

#### security-authorization

**职责**：资源服务器，JWT 验证和权限控制

**核心组件**：

##### JwtAuthenticationFilter - JWT 认证过滤器

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtValidator.validate(token)) {
            Authentication authentication = jwtConverter.convert(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
```

##### JwtBlacklistValidator - 黑名单校验

```java
public class JwtBlacklistValidator {
    public boolean validate(String token) {
        String jti = extractJti(token);
        return !blacklistService.isBlacklisted(jti);
    }
}
```

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

**OAuth2.1 流程**：

```
1. Client → GET /oauth2/authorize?... (Authorization Request)
2. Auth Server → 302 Redirect to /login
3. User → POST /login (username/password)
4. Auth Server → 302 Redirect with authorization code
5. Client → POST /oauth2/token (code + PKCE verifier)
6. Auth Server → Response with JWT access token
```

---

## MyBatis Plus 模块

### component-mybatisplus

**职责**：MyBatis Plus 增强

**核心功能**：

#### 1. 多租户数据隔离

**支持模式**：
- **共享库模式**：所有租户共享数据库，通过 `tenant_id` 列隔离
- **独立库模式**：大租户独享数据库，通过动态数据源路由
- **混合模式**：不同租户采用不同模式

**使用示例**：
```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    // tenant_id 会自动填充和过滤
}
```

#### 2. 数据权限执行（SRA 策略）

**核心流程**：
1. 请求进入后构建 `SubjectContext`（来自 `AccessContext`）
2. 查询发生时，根据表名与语句 ID 构建 `ResourceContext`
3. 通过 `AuthorizationService` 调用策略引擎完成鉴权
4. 若允许且存在 DSL，生成 SQL 条件并追加到 WHERE
5. 若拒绝，返回 `1=0`（安全默认）

**DSL 示例**：
```json
{
  "type": "AND",
  "left": {
    "field": "dept_id",
    "operator": "IN",
    "values": [1, 2, 3]
  },
  "right": {
    "field": "created_at",
    "operator": "GTE",
    "value": { "type": "datetime", "value": "2026-01-31T00:00:00" }
  }
}
```

#### 3. 审计字段自动填充

```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    // created_by, created_at, updated_by, updated_at 自动填充
    // tenant_id 自动填充
}
```

**配置**：
```yaml
goya:
  mybatis-plus:
    tenant:
      enabled: true
      require-tenant: true
      default-mode: CORE_SHARED
    permission:
      enabled: true
      fail-closed: true
      apply-to-write: false
    safety:
      block-attack: true
```

---

## 验证码模块

### component-captcha

**职责**：验证码生成与校验

**支持的验证码类型**：

| 类型 | 枚举值 | 说明 |
|------|--------|------|
| 滑块拼图 | `JIGSAW` | 滑块拼图验证码 |
| 文字点选 | `WORD_CLICK` | 文字点选验证码 |
| 算术 | `ARITHMETIC` | 算术类型验证码 |
| 中文 | `CHINESE` | 中文类型验证码 |
| 中文 GIF | `CHINESE_GIF` | 中文 GIF 类型验证码 |
| GIF | `SPEC_GIF` | GIF 类型验证码 |
| PNG | `SPEC` | PNG 类型验证码 |
| Hutool 线段干扰 | `HUTOOL_LINE` | Hutool 线段干扰验证码 |
| Hutool 圆圈干扰 | `HUTOOL_CIRCLE` | Hutool 圆圈干扰验证码 |
| Hutool 扭曲干扰 | `HUTOOL_SHEAR` | Hutool 扭曲干扰验证码 |
| Hutool GIF | `HUTOOL_GIF` | Hutool GIF 验证码 |

**使用示例**：
```java
@RestController
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/captcha")
    public ApiRes<AbstractCaptcha> getCaptcha(@RequestParam String identity) {
        AbstractCaptcha captcha = captchaService.getCaptcha(
            identity, 
            CaptchaCategoryEnum.JIGSAW
        );
        return ApiRes.ok(captcha);
    }
    
    @PostMapping("/captcha/verify")
    public ApiRes<Boolean> verify(@RequestBody Verification verification) {
        boolean result = captchaService.verify(verification);
        return ApiRes.ok(result);
    }
}
```

**配置**：
```yaml
goya:
  captcha:
    enabled: true
    cache:
      type: redis
      ttl: 300
```

---

## OSS 对象存储模块

### component-oss-aliyun

**职责**：阿里云 OSS 实现

**核心服务**：

#### 1. AliyunObjectService - 对象操作

```java
@Service
public class FileService {
    @Autowired
    private AliyunObjectService objectService;
    
    public void uploadFile(InputStream inputStream, String fileName) {
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName("my-bucket");
        request.setKey(fileName);
        request.setInputStream(inputStream);
        objectService.putObject(request);
    }
}
```

#### 2. AliyunBucketService - Bucket 管理

```java
@Service
public class BucketService {
    @Autowired
    private AliyunBucketService bucketService;
    
    public void createBucket(String bucketName) {
        CreateBucketRequest request = new CreateBucketRequest();
        request.setBucketName(bucketName);
        bucketService.createBucket(request);
    }
}
```

#### 3. AliyunPresignedUrlService - 预签名 URL

```java
@Service
public class FileService {
    @Autowired
    private AliyunPresignedUrlService presignedUrlService;
    
    public String generateDownloadUrl(String key, Duration expiration) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest();
        request.setBucketName("my-bucket");
        request.setKey(key);
        request.setExpiration(Date.from(Instant.now().plus(expiration)));
        return presignedUrlService.generatePresignedUrl(request).toString();
    }
}
```

**配置**：
```yaml
goya:
  oss:
    aliyun:
      enabled: true
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      endpoint: oss-cn-hangzhou.aliyuncs.com
      bucket-name: my-bucket
```

### component-oss-minio

**职责**：MinIO 实现

**配置**：
```yaml
goya:
  oss:
    minio:
      enabled: true
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: my-bucket
```

### component-oss-s3

**职责**：AWS S3 实现

**配置**：
```yaml
goya:
  oss:
    s3:
      enabled: true
      region: us-east-1
      access-key-id: your-access-key-id
      secret-access-key: your-secret-access-key
      bucket-name: my-bucket
```

---

## 消息总线模块

### component-kafka

**职责**：Kafka 消息总线实现

**核心类**：
```java
public class KafkaIntegrationBusBinder implements IntegrationBusBinder {
    @Override
    public void publish(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
    
    @Override
    public void subscribe(String topic, Consumer<Object> handler) {
        kafkaListenerContainerFactory.createContainer(topic, handler);
    }
}
```

**配置**：
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: goya-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

### component-rabbitmq

**职责**：RabbitMQ 消息总线实现

**核心类**：
```java
public class RabbitIntegrationBusBinder implements IntegrationBusBinder {
    @Override
    public void publish(String topic, Object message) {
        rabbitTemplate.convertAndSend(topic, message);
    }
    
    @Override
    public void subscribe(String topic, Consumer<Object> handler) {
        rabbitListenerContainerFactory.createContainer(topic, handler);
    }
}
```

**配置**：
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

---

## AI 模块

### ai-spring

**职责**：Spring AI 集成

**核心功能**：
- 多模型统一接口（OpenAI / Qwen / Local）
- ChatClient / EmbeddingClient
- Prompt 模板管理
- Function Calling

**使用示例**：
```java
@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String prompt) {
        return chatClient.call(prompt);
    }
}
```

**配置**：
```yaml
spring:
  ai:
    openai:
      api-key: your-api-key
      chat:
        options:
          model: gpt-4
```

### ai-model

**职责**：模型管理

**核心功能**：
- 模型配置管理
- 模型切换
- 模型监控

### ai-rag

**职责**：检索增强生成

**核心功能**：
- 文档向量化
- 向量存储
- 语义检索
- 答案生成

### ai-mcp

**职责**：Model Context Protocol 支持

**核心功能**：
- MCP 协议实现
- 上下文管理
- 工具调用

### ai-video

**职责**：视频 AI 处理

**核心功能**：
- 视频帧提取（FFmpeg）
- 图像识别（OpenCV）
- 视频分析
- 目标检测

---

## 下一步阅读

- [架构概览](./overview.md)
- [设计模式](./design-patterns.md)
- [开发指南](../guides/development.md)
- [快速开始](../guides/quick-start.md)
