# 设计模式 | Design Patterns

Goya 框架在设计中广泛应用了经典设计模式，本文档详细介绍各种模式的应用场景和实现方式。

## 目录

- [创建型模式](#创建型模式)
  - [Builder 模式](#builder-模式)
  - [Factory 模式](#factory-模式)
- [结构型模式](#结构型模式)
  - [Adapter 模式](#adapter-模式)
  - [Decorator 模式](#decorator-模式)
- [行为型模式](#行为型模式)
  - [Strategy 模式](#strategy-模式)
  - [Chain of Responsibility 模式](#chain-of-responsibility-模式)
  - [Template Method 模式](#template-method-模式)
  - [Observer 模式](#observer-模式)

---

## 创建型模式

### Builder 模式

**应用场景**：构建复杂对象，特别是有多个可选参数的对象。

#### 1. SecurityUser（安全用户模型）

```java
/**
 * 安全用户模型
 * 使用 Builder 模式构建，避免构造函数参数过多
 */
public class SecurityUser {
    
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Map<String, Object> attributes;
    
    private SecurityUser(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.authorities = builder.authorities;
        this.accountNonExpired = builder.accountNonExpired;
        this.accountNonLocked = builder.accountNonLocked;
        this.credentialsNonExpired = builder.credentialsNonExpired;
        this.enabled = builder.enabled;
        this.attributes = builder.attributes;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String username;
        private String password;
        private Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private boolean enabled = true;
        private Map<String, Object> attributes = new HashMap<>();
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }
        
        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public SecurityUser build() {
            Assert.notNull(username, "username cannot be null");
            return new SecurityUser(this);
        }
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

**优点**：
- 链式调用，代码清晰
- 可选参数灵活配置
- 不可变对象，线程安全
- 参数校验集中在 `build()` 方法

#### 2. ApiRes（统一响应）

```java
/**
 * 统一 API 响应体（Record 类型）
 */
public record ApiRes<T>(
    boolean success,
    String code,
    String message,
    String traceId,
    LocalDateTime timestamp,
    T data,
    String path,
    Map<String, Object> meta,
    List<ApiFieldError> fieldErrors
) implements IResponse {
    
    public static <T> ApiRes<T> ok() {
        return ok(null, null, null);
    }
    
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
    
    public static final class OkBuilder {
        private String message;
        private String traceId;
        private final Map<String, Object> meta = new LinkedHashMap<>();
        
        public OkBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public OkBuilder meta(String key, Object value) {
            this.meta.put(key, value);
            return this;
        }
        
        public <T> ApiRes<T> data(T data) {
            return new ApiRes<>(
                true,
                CommonErrorCode.OK.code(),
                message,
                traceId,
                LocalDateTime.now(),
                data,
                getCurrentRequestPath(),
                meta,
                List.of()
            );
        }
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
    
    @PostMapping("/users")
    public ApiRes<User> createUser(@RequestBody UserDTO dto) {
        User user = userService.create(dto);
        return ApiRes.okBuilder()
            .message("创建成功")
            .data(user);
    }
}
```

---

### Factory 模式

**应用场景**：根据不同条件创建不同类型的对象。

#### 1. CaptchaRendererFactory（验证码渲染器工厂）

```java
/**
 * 验证码渲染器工厂
 * 根据验证码类型创建对应的渲染器
 */
@Component
public class CaptchaRendererFactory {
    
    private final Map<CaptchaTypeEnum, ICaptchaRenderer> renderers;
    
    @Autowired
    public CaptchaRendererFactory(List<ICaptchaRenderer> rendererList) {
        this.renderers = rendererList.stream()
            .collect(Collectors.toMap(
                ICaptchaRenderer::getType,
                Function.identity()
            ));
    }
    
    /**
     * 获取验证码渲染器
     *
     * @param type 验证码类型
     * @return 渲染器
     */
    public ICaptchaRenderer getRenderer(CaptchaTypeEnum type) {
        ICaptchaRenderer renderer = renderers.get(type);
        if (renderer == null) {
            throw new CaptchaException("不支持的验证码类型: " + type);
        }
        return renderer;
    }
}
```

**使用示例**：

```java
@Service
public class CaptchaService {
    
    @Autowired
    private CaptchaRendererFactory captchaRendererFactory;
    
    public CaptchaVO generate(CaptchaTypeEnum type) {
        ICaptchaRenderer renderer = captchaRendererFactory.getRenderer(type);
        return renderer.render();
    }
}
```

#### 2. MultiClusterRemoteCacheFactory（多集群缓存工厂）

```java
/**
 * 多集群 Redis 缓存工厂
 */
@Component
public class MultiClusterRemoteCacheFactory {
    
    private final Map<String, RedissonClient> clients = new ConcurrentHashMap<>();
    
    /**
     * 创建或获取 Redis 客户端
     *
     * @param clusterName 集群名称
     * @param config Redis 配置
     * @return Redisson 客户端
     */
    public RedissonClient getOrCreateClient(String clusterName, Config config) {
        return clients.computeIfAbsent(clusterName, key -> {
            log.info("[Goya] |- Redis cluster [{}] client created", clusterName);
            return Redisson.create(config);
        });
    }
}
```

---

## 结构型模式

### Adapter 模式

**应用场景**：将一个接口转换为客户端期望的另一个接口。

#### 1. OAuth2 认证适配器

```java
/**
 * 将 HTTP 请求适配为 Spring Security 的 Authentication
 */
public class LoginAuthenticationConverter implements AuthenticationConverter {
    
    @Override
    public Authentication convert(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        }
        
        return null;
    }
}
```

#### 2. JWT 认证适配器

```java
/**
 * 将 JWT Token 适配为 Spring Security 的 Authentication
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String username = jwt.getSubject();
        return new JwtAuthenticationToken(jwt, authorities, username);
    }
    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
}
```

---

### Decorator 模式

**应用场景**：动态地给对象添加额外的职责。

#### 1. 缓存装饰器

```java
/**
 * 为缓存添加统计功能
 */
public class StatisticsCacheService implements CacheService {
    
    private final CacheService delegate;
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    
    public StatisticsCacheService(CacheService delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public <K, V> V get(String cacheName, K key, Class<V> type) {
        V value = delegate.get(cacheName, key, type);
        if (value != null) {
            hitCount.incrementAndGet();
        } else {
            missCount.incrementAndGet();
        }
        return value;
    }
    
    @Override
    public <K, V> void put(String cacheName, K key, V value) {
        delegate.put(cacheName, key, value);
    }
    
    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0 : (double) hitCount.get() / total;
    }
}
```

---

## 行为型模式

### Strategy 模式

**应用场景**：定义一系列算法，把它们封装起来，并且使它们可以互相替换。

#### 1. 策略接口和实现

```java
/**
 * 策略接口
 */
public interface IStrategy {
    /**
     * 获取策略标识
     */
    String getStrategyId();
}

/**
 * 支付策略
 */
public interface PaymentStrategy extends IStrategy {
    void pay(Order order);
}

@Component("alipayStrategy")
public class AlipayStrategy implements PaymentStrategy {
    
    @Override
    public String getStrategyId() {
        return "alipay";
    }
    
    @Override
    public void pay(Order order) {
        log.info("使用支付宝支付订单: {}", order.getId());
        // 支付宝支付逻辑
    }
}

@Component("wechatPayStrategy")
public class WechatPayStrategy implements PaymentStrategy {
    
    @Override
    public String getStrategyId() {
        return "wechat";
    }
    
    @Override
    public void pay(Order order) {
        log.info("使用微信支付订单: {}", order.getId());
        // 微信支付逻辑
    }
}
```

#### 2. 策略选择器

```java
/**
 * 策略选择器
 * 根据策略 ID 动态选择策略
 */
public class StrategyChoose {
    
    private static final Map<Class<?>, Map<String, Object>> STRATEGY_MAP = new ConcurrentHashMap<>();
    
    /**
     * 注册策略
     */
    public static <T extends IStrategy> void register(T strategy) {
        Class<?> strategyInterface = findStrategyInterface(strategy.getClass());
        STRATEGY_MAP.computeIfAbsent(strategyInterface, k -> new ConcurrentHashMap<>())
            .put(strategy.getStrategyId(), strategy);
    }
    
    /**
     * 选择策略
     */
    @SuppressWarnings("unchecked")
    public static <T extends IStrategy> T choose(String strategyId, Class<T> strategyInterface) {
        Map<String, Object> strategies = STRATEGY_MAP.get(strategyInterface);
        if (strategies == null) {
            throw new IllegalArgumentException("未找到策略接口: " + strategyInterface.getName());
        }
        
        T strategy = (T) strategies.get(strategyId);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到策略: " + strategyId);
        }
        
        return strategy;
    }
}
```

**使用示例**：

```java
@Service
public class OrderService {
    
    public void processPayment(Order order, String paymentMethod) {
        PaymentStrategy strategy = StrategyChoose.choose(paymentMethod, PaymentStrategy.class);
        strategy.pay(order);
    }
}
```

---

### Chain of Responsibility 模式

**应用场景**：避免请求发送者与接收者耦合，让多个对象都有可能接收请求。

#### 1. 责任链接口

```java
/**
 * 责任链处理器
 */
public interface IChainHandler<T, R> {
    
    /**
     * 获取处理器标识
     */
    String getHandlerId();
    
    /**
     * 获取处理器顺序
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 处理请求
     */
    R handle(T context, ChainContext chainContext);
}
```

#### 2. 责任链上下文

```java
/**
 * 责任链上下文
 */
public class ChainContext {
    
    private final List<IChainHandler<?, ?>> handlers;
    private int currentIndex = 0;
    
    public ChainContext(List<IChainHandler<?, ?>> handlers) {
        this.handlers = handlers.stream()
            .sorted(Comparator.comparingInt(IChainHandler::getOrder))
            .collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    public <T, R> R next(T context) {
        if (currentIndex >= handlers.size()) {
            return null;
        }
        
        IChainHandler<T, R> handler = (IChainHandler<T, R>) handlers.get(currentIndex++);
        return handler.handle(context, this);
    }
}
```

#### 3. 具体处理器

```java
/**
 * 登录前置验证处理器
 */
@Component
public class LoginPreValidationHandler implements IChainHandler<LoginRequest, Boolean> {
    
    @Override
    public String getHandlerId() {
        return "loginPreValidation";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Boolean handle(LoginRequest request, ChainContext chainContext) {
        // 验证用户名和密码不为空
        if (StringUtils.isEmpty(request.getUsername()) || 
            StringUtils.isEmpty(request.getPassword())) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        
        // 继续执行下一个处理器
        return chainContext.next(request);
    }
}

/**
 * 验证码校验处理器
 */
@Component
public class CaptchaValidationHandler implements IChainHandler<LoginRequest, Boolean> {
    
    @Autowired
    private CaptchaService captchaService;
    
    @Override
    public String getHandlerId() {
        return "captchaValidation";
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
    
    @Override
    public Boolean handle(LoginRequest request, ChainContext chainContext) {
        // 验证验证码
        if (!captchaService.verify(request.getCaptchaKey(), request.getCaptchaCode())) {
            throw new CaptchaException("验证码错误");
        }
        
        return chainContext.next(request);
    }
}

/**
 * 用户认证处理器
 */
@Component
public class UserAuthenticationHandler implements IChainHandler<LoginRequest, Boolean> {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Override
    public String getHandlerId() {
        return "userAuthentication";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
    
    @Override
    public Boolean handle(LoginRequest request, ChainContext chainContext) {
        UsernamePasswordAuthenticationToken token = 
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return true;
    }
}
```

**使用示例**：

```java
@Service
public class LoginService {
    
    @Autowired
    private List<IChainHandler<LoginRequest, Boolean>> handlers;
    
    public boolean login(LoginRequest request) {
        ChainContext context = new ChainContext(handlers);
        return context.next(request);
    }
}
```

---

### Template Method 模式

**应用场景**：定义算法骨架，让子类实现某些步骤。

#### 1. 抽象社交登录管理器

```java
/**
 * 社交登录抽象模板
 */
public abstract class AbstractSocialManager implements SocialManager {
    
    /**
     * 社交登录模板方法
     */
    @Override
    public final SocialUser login(SocialTypeEnum type, String code) {
        // 1. 验证参数
        validateParams(type, code);
        
        // 2. 获取第三方用户信息（由子类实现）
        ThirdPrincipal principal = fetchThirdPartyUser(type, code);
        
        // 3. 转换为系统用户
        SocialUser socialUser = convertToSocialUser(principal);
        
        // 4. 缓存用户信息
        cacheUserInfo(socialUser);
        
        // 5. 记录登录日志
        logLoginEvent(socialUser);
        
        return socialUser;
    }
    
    /**
     * 验证参数
     */
    protected void validateParams(SocialTypeEnum type, String code) {
        Assert.notNull(type, "社交平台类型不能为空");
        Assert.hasText(code, "授权码不能为空");
    }
    
    /**
     * 获取第三方用户信息（由子类实现）
     */
    protected abstract ThirdPrincipal fetchThirdPartyUser(SocialTypeEnum type, String code);
    
    /**
     * 转换为系统用户
     */
    protected SocialUser convertToSocialUser(ThirdPrincipal principal) {
        SocialUser socialUser = new SocialUser();
        socialUser.setOpenId(principal.getOpenId());
        socialUser.setNickname(principal.getNickname());
        socialUser.setAvatar(principal.getAvatar());
        socialUser.setGender(principal.getGender());
        return socialUser;
    }
    
    /**
     * 缓存用户信息
     */
    protected void cacheUserInfo(SocialUser socialUser) {
        // 缓存逻辑
    }
    
    /**
     * 记录登录日志（钩子方法，子类可选实现）
     */
    protected void logLoginEvent(SocialUser socialUser) {
        log.info("用户社交登录: {}", socialUser.getOpenId());
    }
}
```

#### 2. 具体实现

```java
/**
 * 默认社交登录管理器
 */
@Service
public class DefaultSocialManager extends AbstractSocialManager {
    
    @Autowired
    private ThirdPartService thirdPartService;
    
    @Override
    protected ThirdPrincipal fetchThirdPartyUser(SocialTypeEnum type, String code) {
        switch (type) {
            case WECHAT_MINI:
                return thirdPartService.getWechatMiniUser(code);
            case WECHAT_MP:
                return thirdPartService.getWechatMpUser(code);
            default:
                throw new SocialException("不支持的社交平台: " + type);
        }
    }
}
```

---

### Observer 模式

**应用场景**：定义对象间一对多的依赖关系，当一个对象状态改变时，所有依赖它的对象都得到通知。

#### 1. 缓存失效通知

```java
/**
 * 缓存失效订阅者
 */
@Component
public class RedisCacheEvictionSubscriber {
    
    @Autowired
    private CacheService localCache;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic("cache:eviction");
        topic.addListener(String.class, (channel, key) -> {
            log.debug("收到缓存失效通知: {}", key);
            // 解析 cacheName 和 key
            String[] parts = key.split(":", 2);
            if (parts.length == 2) {
                localCache.delete(parts[0], parts[1]);
            }
        });
    }
}

/**
 * 缓存更新发布者
 */
@Service
public class CacheService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    public void evictCache(String key) {
        // 发布缓存失效事件
        RTopic topic = redissonClient.getTopic("cache:eviction");
        topic.publish(key);
    }
}
```

---

## 最佳实践

### 1. 选择合适的模式

- **Builder**：复杂对象构建，特别是有多个可选参数
- **Factory**：根据条件创建不同类型的对象
- **Strategy**：算法族可以互换
- **Chain of Responsibility**：请求需要多个处理器顺序处理
- **Template Method**：算法骨架固定，部分步骤可变

### 2. 避免过度设计

- 不要为了用模式而用模式
- 只在真正需要时才引入模式
- 保持代码简洁和可读性

### 3. 与 Spring 结合

- 利用 Spring 的依赖注入管理对象
- 使用 Spring 的生命周期回调初始化
- 结合 Spring Boot 自动配置

### 4. 文档和注释

- 在类注释中说明使用的设计模式
- 关键方法添加详细注释
- 提供使用示例

---

## 下一步阅读

- [架构概览](./overview.md)
- [模块详解](./modules.md)
- [开发指南](../guides/development.md)
