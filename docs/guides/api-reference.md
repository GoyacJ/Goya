# API 参考 | API Reference

本文档提供 Goya 框架核心 API 的详细参考。

## 目录

- [响应封装](#响应封装)
- [异常处理](#异常处理)
- [缓存 API](#缓存-api)
- [Redis 增强 API](#redis-增强-api)
- [安全 API](#安全-api)
- [OSS API](#oss-api)
- [消息总线 API](#消息总线-api)

---

## 响应封装

### ApiRes<T>

统一 API 响应体，使用 Java Record 类型实现。

**类定义**：
```java
public record ApiRes<T>(
    boolean success,                    // 是否成功
    String code,                        // 稳定错误码/成功码（成功时固定 OK）
    String message,                     // 对外安全文案
    String traceId,                     // 链路追踪标识
    LocalDateTime timestamp,            // 响应时间（UTC）
    T data,                             // 成功数据
    String path,                        // 请求地址
    Map<String, Object> meta,          // 扩展信息（分页/统计等）
    List<ApiFieldError> fieldErrors    // 字段级错误
) implements IResponse
```

**静态方法**：

#### ok()
```java
public static ApiRes<Void> ok()
```
返回成功响应（无数据）。

**示例**：
```java
return ApiRes.ok();
```

#### ok(T data)
```java
public static <T> ApiRes<T> ok(T data)
```
返回成功响应（带数据）。

**示例**：
```java
User user = userService.getById(1L);
return ApiRes.ok(user);
```

#### ok(T data, String message)
```java
public static <T> ApiRes<T> ok(T data, String message)
```
返回成功响应（带数据和消息）。

**示例**：
```java
return ApiRes.ok(user, "查询成功");
```

#### okPage(List<T> data, PageMeta pageMeta)
```java
public static <T> ApiRes<List<T>> okPage(List<T> data, PageMeta pageMeta)
```
返回分页成功响应。

**示例**：
```java
PageVO<User> page = userService.page(query);
return ApiRes.okPage(page.getRecords(), page.getPageMeta());
```

#### fail(ErrorCode errorCode)
```java
public static ApiRes<Void> fail(ErrorCode errorCode)
```
返回失败响应。

**示例**：
```java
return ApiRes.fail(CommonErrorCode.INVALID_PARAM);
```

#### fail(ErrorCode errorCode, String message)
```java
public static ApiRes<Void> fail(ErrorCode errorCode, String message)
```
返回失败响应（带消息）。

**示例**：
```java
return ApiRes.fail(CommonErrorCode.INVALID_PARAM, "参数不合法");
```

#### okBuilder()
```java
public static OkBuilder okBuilder()
```
返回成功响应构建器。

**示例**：
```java
return ApiRes.okBuilder()
    .message("查询成功")
    .meta("total", 100)
    .data(list);
```

#### failBuilder(ErrorCode errorCode)
```java
public static FailBuilder failBuilder(ErrorCode errorCode)
```
返回失败响应构建器。

**示例**：
```java
return ApiRes.failBuilder(CommonErrorCode.INVALID_PARAM)
    .message("参数不合法")
    .fieldError("name", "不能为空")
    .build();
```

---

## 异常处理

### BaseException

业务异常基类。

**类定义**：
```java
public class BaseException extends RuntimeException {
    private String code;      // 错误码
    private String message;   // 错误消息
}
```

**子类**：

- `BizException`：业务异常
- `AuthException`：认证异常
- `ValidationException`：验证异常
- `InfraException`：基础设施异常
- `RemoteException`：远程调用异常

**使用示例**：
```java
if (user == null) {
    throw new BizException("0404", "用户不存在");
}
```

### Exceptions

异常工具类，提供快速创建异常的方法。

**方法**：

#### bizException(String message)
```java
public static BizException bizException(String message)
```
创建业务异常。

**示例**：
```java
throw Exceptions.bizException("用户不存在");
```

#### authException(String message)
```java
public static AuthException authException(String message)
```
创建认证异常。

**示例**：
```java
throw Exceptions.authException("未授权");
```

---

## 缓存 API

### CacheService

缓存服务接口。

**方法**：

#### get(String cacheName, K key, Class<V> type)
```java
<K, V> V get(String cacheName, K key, Class<V> type)
```
获取缓存值。

**参数**：
- `cacheName`：缓存名（区分不同缓存区域）
- `key`：缓存键
- `type`：值类型

**返回**：缓存值，如果不存在返回 `null`

**示例**：
```java
User user = cacheService.get("user", 1L, User.class);
```

#### put(String cacheName, K key, V value)
```java
<K, V> void put(String cacheName, K key, V value)
```
设置缓存值（使用默认 TTL）。

**参数**：
- `cacheName`：缓存名
- `key`：缓存键
- `value`：缓存值

**示例**：
```java
cacheService.put("user", 1L, user);
```

#### put(String cacheName, K key, V value, Duration ttl)
```java
<K, V> void put(String cacheName, K key, V value, Duration ttl)
```
设置缓存值（指定 TTL）。

**参数**：
- `cacheName`：缓存名
- `key`：缓存键
- `value`：缓存值
- `ttl`：过期时间（为空或非正数表示使用默认 TTL）

**示例**：
```java
cacheService.put("user", 1L, user, Duration.ofMinutes(10));
```

#### delete(String cacheName, K key)
```java
<K> boolean delete(String cacheName, K key)
```
删除缓存项。

**参数**：
- `cacheName`：缓存名
- `key`：缓存键

**返回**：是否实际删除（命中才为 true）

**示例**：
```java
boolean deleted = cacheService.delete("user", 1L);
```

#### clear(String cacheName)
```java
void clear(String cacheName)
```
清空某个缓存区域。

**参数**：
- `cacheName`：缓存名

**示例**：
```java
cacheService.clear("user");
```

#### getOrLoad(String cacheName, K key, Supplier<V> loader)
```java
<K, V> V getOrLoad(String cacheName, K key, Supplier<V> loader)
```
获取或加载（缓存未命中则调用 loader 计算并写入缓存）。

**参数**：
- `cacheName`：缓存名
- `key`：缓存键
- `loader`：加载器

**返回**：缓存值或加载的值

**示例**：
```java
User user = cacheService.getOrLoad("user", 1L, () -> {
    return userRepository.findById(1L).orElse(null);
});
```

#### getAll(String cacheName, Collection<K> keys)
```java
<K, V> Map<K, V> getAll(String cacheName, Collection<K> keys)
```
批量获取。

**参数**：
- `cacheName`：缓存名
- `keys`：键集合

**返回**：map：key -> value（仅包含命中的项）

**示例**：
```java
List<Long> ids = Arrays.asList(1L, 2L, 3L);
Map<Long, User> users = cacheService.getAll("user", ids);
```

#### exists(String cacheName, K key)
```java
<K> boolean exists(String cacheName, K key)
```
是否存在 key。

**参数**：
- `cacheName`：缓存名
- `key`：缓存键

**返回**：是否存在

**示例**：
```java
boolean exists = cacheService.exists("user", 1L);
```

---

## Redis 增强 API

### RedisLockService

分布式锁服务。

**方法**：

#### withLock(String lockName, Duration lease, Callable<T> task)
```java
<T> T withLock(String lockName, Duration lease, Callable<T> task)
```
执行互斥逻辑（阻塞获取锁）。

**参数**：
- `lockName`：锁名
- `lease`：锁持有时间（到期自动释放）
- `task`：要执行的任务（Callable）

**返回**：任务执行结果

**示例**：
```java
Order order = lockService.withLock("order:create:1", Duration.ofSeconds(30), () -> {
    // 业务逻辑
    return orderRepository.save(newOrder);
});
```

#### tryWithLock(String lockName, Duration wait, Duration lease, Callable<T> task)
```java
<T> T tryWithLock(String lockName, Duration wait, Duration lease, Callable<T> task)
```
尝试执行互斥逻辑（带等待时间）。

**参数**：
- `lockName`：锁名
- `wait`：获取锁最大等待时间
- `lease`：锁持有时间（到期自动释放）
- `task`：要执行的任务（Callable）

**返回**：获取锁成功才执行并返回结果，否则返回 `null`

**示例**：
```java
Order order = lockService.tryWithLock(
    "order:create:1", 
    Duration.ofSeconds(10), 
    Duration.ofSeconds(30), 
    () -> {
        return orderRepository.save(newOrder);
    }
);

if (order == null) {
    throw new BizException("获取锁失败，请稍后重试");
}
```

### RedisBloomFilterService

布隆过滤器服务。

**方法**：

#### add(String name, Object value)
```java
void add(String name, Object value)
```
添加元素到布隆过滤器。

**参数**：
- `name`：过滤器名称
- `value`：要添加的值

**示例**：
```java
bloomFilterService.add("user:exists", userId);
```

#### contains(String name, Object value)
```java
boolean contains(String name, Object value)
```
检查元素是否可能存在。

**参数**：
- `name`：过滤器名称
- `value`：要检查的值

**返回**：`true` 表示可能存在，`false` 表示一定不存在

**示例**：
```java
boolean exists = bloomFilterService.contains("user:exists", userId);
```

### RedisDelayedQueueService

延迟队列服务。

**方法**：

#### enqueue(String queue, Object message, Duration delay)
```java
void enqueue(String queue, Object message, Duration delay)
```
延迟投递消息。

**参数**：
- `queue`：队列名
- `message`：消息对象
- `delay`：延迟时长

**示例**：
```java
delayedQueueService.enqueue("order:timeout", order, Duration.ofMinutes(30));
```

#### poll(String queue)
```java
Optional<Object> poll(String queue)
```
非阻塞拉取一条消息。

**参数**：
- `queue`：队列名

**返回**：消息（可能为空）

**示例**：
```java
Optional<Order> order = delayedQueueService.poll("order:timeout");
order.ifPresent(this::handleOrderTimeout);
```

#### take(String queue, Duration timeout)
```java
Optional<Object> take(String queue, Duration timeout)
```
阻塞等待一条消息（带超时）。

**参数**：
- `queue`：队列名
- `timeout`：超时时间

**返回**：消息（可能为空）

**示例**：
```java
Optional<Order> order = delayedQueueService.take("order:timeout", Duration.ofSeconds(10));
```

### RedisRateLimiterService

限流器服务。

**方法**：

#### tryAcquire(String key, int permits, Duration window)
```java
boolean tryAcquire(String key, int permits, Duration window)
```
尝试获取许可（令牌桶算法）。

**参数**：
- `key`：限流键
- `permits`：需要的许可数
- `window`：时间窗口

**返回**：`true` 表示获取成功，`false` 表示限流

**示例**：
```java
boolean allowed = rateLimiterService.tryAcquire("api:limit:user:1", 1, Duration.ofSeconds(1));
if (!allowed) {
    throw new BizException("0403", "请求过于频繁");
}
```

### RedisTopicService

Topic 消息服务。

**方法**：

#### publish(String topic, Object message)
```java
void publish(String topic, Object message)
```
发布消息。

**参数**：
- `topic`：主题
- `message`：消息对象

**示例**：
```java
topicService.publish("notification:user:1", notification);
```

#### subscribe(String pattern, BiConsumer<String, Object> handler)
```java
void subscribe(String pattern, BiConsumer<String, Object> handler)
```
订阅消息。

**参数**：
- `pattern`：主题模式（支持通配符）
- `handler`：消息处理器

**示例**：
```java
topicService.subscribe("notification:*", (channel, message) -> {
    log.info("收到通知: {}", message);
});
```

### RedisAtomicService

原子操作服务。

**方法**：

#### incrementAndGet(String name)
```java
long incrementAndGet(String name)
```
自增并返回新值。

**参数**：
- `name`：计数器名

**返回**：新值

**示例**：
```java
long count = atomicService.incrementAndGet("user:visit:1");
```

#### addAndGet(String name, long delta)
```java
long addAndGet(String name, long delta)
```
增加 delta 并返回新值。

**参数**：
- `name`：计数器名
- `delta`：增量

**返回**：新值

**示例**：
```java
long count = atomicService.addAndGet("user:visit:1", 5);
```

#### compareAndSet(String name, long expect, long update)
```java
boolean compareAndSet(String name, long expect, long update)
```
CAS 更新。

**参数**：
- `name`：计数器名
- `expect`：期望值
- `update`：更新值

**返回**：是否成功

**示例**：
```java
boolean success = atomicService.compareAndSet("user:visit:1", 10, 20);
```

---

## 安全 API

### SecurityUser

安全用户模型。

**Builder 模式**：
```java
SecurityUser user = SecurityUser.builder()
    .username("admin")
    .password("{bcrypt}$2a$10...")
    .authorities(authorities)
    .attribute("tenantId", "tenant-001")
    .attribute("userId", 1L)
    .build();
```

**方法**：

#### getUserId()
```java
String getUserId()
```
获取用户 ID。

#### getTenantId()
```java
String getTenantId()
```
获取租户 ID。

#### getAttribute(String key)
```java
Object getAttribute(String key)
```
获取属性值。

### IUserService

用户服务 SPI。

**方法**：

#### loadUserByUsername(String username)
```java
SecurityUser loadUserByUsername(String username)
```
根据用户名加载用户。

#### loadUserByMobile(String mobile)
```java
SecurityUser loadUserByMobile(String mobile)
```
根据手机号加载用户。

---

## OSS API

### IOssService

对象存储服务接口。

**方法**：

#### upload(InputStream inputStream, String fileName)
```java
String upload(InputStream inputStream, String fileName)
```
上传文件。

**参数**：
- `inputStream`：文件输入流
- `fileName`：文件名

**返回**：文件 Key

**示例**：
```java
String key = ossService.upload(fileInputStream, "avatar.jpg");
```

#### download(String key)
```java
InputStream download(String key)
```
下载文件。

**参数**：
- `key`：文件 Key

**返回**：文件输入流

**示例**：
```java
InputStream stream = ossService.download("avatar.jpg");
```

#### delete(String key)
```java
void delete(String key)
```
删除文件。

**参数**：
- `key`：文件 Key

**示例**：
```java
ossService.delete("avatar.jpg");
```

#### getUrl(String key, Duration expiration)
```java
String getUrl(String key, Duration expiration)
```
获取预签名 URL。

**参数**：
- `key`：文件 Key
- `expiration`：过期时间

**返回**：预签名 URL

**示例**：
```java
String url = ossService.getUrl("avatar.jpg", Duration.ofHours(1));
```

---

## 消息总线 API

### IMessageBus

消息总线接口。

**方法**：

#### publish(String topic, Object message)
```java
void publish(String topic, Object message)
```
发布消息。

**参数**：
- `topic`：主题
- `message`：消息对象

**示例**：
```java
messageBus.publish("user:created", user);
```

#### subscribe(String topic, Consumer<Object> handler)
```java
void subscribe(String topic, Consumer<Object> handler)
```
订阅消息。

**参数**：
- `topic`：主题
- `handler`：消息处理器

**示例**：
```java
messageBus.subscribe("user:created", (message) -> {
    log.info("用户创建事件: {}", message);
});
```

---

## 下一步阅读

- [模块详细文档](../architecture/modules-detailed.md)
- [开发指南](./development.md)
- [快速开始](./quick-start.md)
