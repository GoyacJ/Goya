# redis-boot-starter 模块

## 简介

`redis-boot-starter` 是 Goya 项目的 Redis 集成模块，提供 `IL2Cache` 接口的 Redis 实现，基于 Redisson 实现分布式缓存和 Redis 特有功能。

## 架构设计

本模块实现了 `component-cache` 定义的 `IL2Cache` 接口，作为混合缓存架构的 L2 层：

```
┌─────────────────────────────────────────────────────────────────┐
│                    component-cache (抽象层)                      │
│  ┌───────────────┐     ┌──────────────────────────────────────┐ │
│  │ ICacheService │ ←── │ HybridCacheService (默认 bean)      │ │
│  └───────────────┘     │ ├── L1: LocalCacheService (固定)    │ │
│         ↑              │ └── L2: RemoteCacheService (可选)    │ │
│  ┌───────────────┐     └──────────────────────────────────────┘ │
│  │   IL2Cache    │                                              │
│  └───────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
           ↑ 实现
┌─────────────────────────────────────────────────────────────────┐
│                    redis-boot-starter                            │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ RedisCacheService implements IL2Cache                       ││
│  │ ├── getCacheType() → "redis"                               ││
│  │ ├── isAvailable() → 检查 Redis 连接                         ││
│  │ └── 分布式缓存操作 + 分布式锁                                ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ RedisCacheInvalidatePublisher / Listener                    ││
│  │ └── 基于 Redis Pub/Sub 的缓存失效通知                        ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## 核心特性

1. **IL2Cache 实现**
   - 基于 Redisson RMapCache 实现
   - 支持 TTL 和过期策略
   - JSON 序列化（复用项目 JsonMapper）

2. **自动装配**
   - 存在 RedissonClient 时自动注册为 IL2Cache
   - CacheAutoConfiguration 检测到后自动启用混合缓存
   - 无需手动配置缓存类型

3. **Redis 特有功能**
   - 原子计数器（RAtomicLong）
   - 发布订阅（RTopic）
   - 分布式锁（RLock）
   - 分布式信号量（RSemaphore）
   - 分布式倒计时门闩（RCountDownLatch）

## 配置示例

### 基本配置

```yaml
# Spring Redisson 配置
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 0

# Goya 缓存配置
platform:
  cache:
    key-prefix: "goya:"
    default-ttl: PT30M
    enable-stats: true
    
    # 多级缓存配置（默认启用）
    multi-level:
      enabled: true  # 默认 true，有 L2 时自动启用多级模式
      ttl-sync-strategy: UNIFIED
      invalidate-topic: "cache:invalidate"
      
      l1:
        max-size: 10000
        ttl: PT5M  # INDEPENDENT 模式生效
    
    # Redis 特有配置
    redis:
      lock-timeout: PT30S
      lock-wait-time: PT10S
```

### 强制 L1 Only（禁用 Redis L2）

```yaml
platform:
  cache:
    multi-level:
      enabled: false  # 即使有 Redis 也只使用 L1
```

## 装配流程

```
1. 应用启动，Redisson 自动配置 RedissonClient
2. RedisAutoConfiguration 检测到 RedissonClient
3. 注册 RedisCacheService 为 IL2Cache bean
4. MultiLevelCacheAutoConfiguration 检测到 IL2Cache
5. 注册 MultiLevelCacheService (L1 + L2)
6. 应用使用多级缓存
```

## 使用方式

### 1. 使用缓存服务（自动注入多级缓存）

```java
@Autowired
private ICacheService cacheService;

// 缓存操作（自动 L1+L2 协作）
cacheService.put("userCache", "userId:1", user, Duration.ofMinutes(30));
User user = cacheService.get("userCache", "userId:1");
cacheService.remove("userCache", "userId:1");

// 获取或加载
User result = cacheService.get("userCache", "userId:1", key -> {
    return userRepository.findById(key);
});
```

### 2. 使用 Redis 特有功能

```java
@Autowired
private IRedisService redisService;

// 原子计数器
Long count = redisService.increment("visitCount");
redisService.setCounter("visitCount", 100);

// 发布订阅
redisService.publish("notification", message);
redisService.subscribe("notification", String.class, (channel, msg) -> {
    System.out.println("Received: " + msg);
});

// 分布式信号量
redisService.trySetPermits("resourceLock", 10);
if (redisService.tryAcquire("resourceLock")) {
    try {
        // 访问受限资源
    } finally {
        redisService.release("resourceLock");
    }
}
```

### 3. 分布式锁

```java
// 使用锁并执行操作（L2 可用时为分布式锁）
boolean success = cacheService.lockAndRun(
    "lockCache",
    "orderId:123",
    Duration.ofSeconds(30),
    () -> {
        processOrder("123");
    }
);
```

## 序列化机制

### 默认序列化

Redisson 使用项目配置的 `JsonMapper` 进行序列化：

- **编解码器**: `TypedJsonJacksonCodec`
- **序列化格式**: JSON
- **复用配置**: 时间格式、枚举处理、命名策略等

### 自定义序列化

如需自定义，可以覆盖 Redisson 的 Codec 配置：

```java
@Bean
public Codec customCodec() {
    return new YourCustomCodec();
}
```

## 多级缓存架构

```
应用节点1               应用节点2               应用节点N
   |                     |                     |
 L1(Caffeine)         L1(Caffeine)         L1(Caffeine)
   |                     |                     |
   +---------------------+---------------------+
                         |
                    L2(Redis)
                         |
                  Pub/Sub Channel
                (失效消息广播)
```

### 工作流程

1. **写入操作**: 
   - 写入 L1 + L2
   - 发布失效消息到 Pub/Sub
   - 其他节点收到消息后失效本地 L1

2. **读取操作**:
   - 先查 L1，命中则返回
   - L1 未命中查 L2
   - L2 命中则回填 L1

3. **降级流程**:
   - L2 不可用时自动降级为 L1 Only
   - 日志记录降级事件
   - L2 恢复后自动恢复多级模式

## 高可用特性

1. **自动降级**: L2 不可用时自动降级为 L1 Only
2. **容错机制**: Pub/Sub 失败不阻塞主流程
3. **连接池**: Redisson 自动管理连接池
4. **健康检查**: `isAvailable()` 方法检查 Redis 连接状态

## 监控与统计

```java
@Autowired
private MultiLevelCacheService multiLevelCache;

// 获取统计信息
Map<String, Object> stats = multiLevelCache.getStats();
System.out.println("NodeId: " + stats.get("nodeId"));
System.out.println("L2 Type: " + stats.get("l2CacheType"));  // "redis"
System.out.println("L2 Available: " + stats.get("l2Available"));
```

## 依赖

```xml
<dependency>
    <groupId>com.ysmjjsy.goya</groupId>
    <artifactId>redis-boot-starter</artifactId>
</dependency>

<!-- component-cache 会自动传递依赖 -->
<!-- Redisson 已内置，无需额外引入 -->
```

## 注意事项

1. **序列化对象**: 确保缓存的对象可以被 Jackson 序列化
2. **TTL 设置**: 推荐使用 UNIFIED 模式，或确保 L1 TTL ≤ L2 TTL
3. **Key 设计**: 使用有意义的 key 前缀，避免冲突
4. **内存管理**: L1 设置合理的 maxSize，避免 OOM
5. **网络延迟**: 注意 Redis 网络延迟对性能的影响

## 常见问题

### Q: 如何只使用本地缓存（禁用 Redis）？

不添加 `redis-boot-starter` 依赖，或：

```yaml
platform:
  cache:
    multi-level:
      enabled: false
```

### Q: 缓存失效消息延迟怎么办？

多级缓存使用最终一致性模型，存在短暂的不一致窗口。建议：
- 使用 UNIFIED TTL 策略
- 设置较短的 L1 TTL

### Q: 如何查看调试日志？

启用 TRACE 日志级别：

```yaml
logging:
  level:
    com.ysmjjsy.goya.starter.redis: TRACE
    com.ysmjjsy.goya.component.cache: TRACE
```

### Q: 如何扩展其他 L2 实现？

实现 `IL2Cache` 接口即可，参考 `RedisCacheService` 实现。
