# redis-boot-starter Redis 启动器

## 模块简介

`redis-boot-starter` 是 Goya 框架的 Redis 集成模块，基于 Redisson 提供分布式缓存和 Redis 特有功能。

## 核心功能

### 1. 分布式缓存
- 实现 `ICacheService` 接口
- 基于 `RMapCache` 提供带 TTL 的缓存
- 支持批量操作和懒加载
- 自动过期和分布式锁

### 2. Redis 特有功能

#### 原子计数器
- increment/decrement：原子递增/递减
- incrementBy/decrementBy：指定增量
- getCounter/setCounter：获取/设置计数值

#### 发布订阅
- publish：发布消息到主题
- subscribe：订阅主题
- unsubscribe：取消订阅

#### 分布式信号量
- acquire/release：获取/释放许可
- tryAcquire：尝试获取许可
- availablePermits：查询可用许可数

#### 分布式倒计时门闩
- countDown：倒计时减一
- await：等待倒计时到零
- trySetCount：设置倒计时数量

## 项目结构

```
redis-boot-starter/
├── configuration/
│   ├── RedisAutoConfiguration.java       # Redis 自动配置
│   └── properties/
│       └── RedisProperties.java          # Redis 配置属性
├── constants/
│   └── IRedisConstants.java              # Redis 常量
└── service/
    ├── IRedisService.java                # Redis 特有功能接口
    ├── RedisCacheService.java            # Redis 缓存实现
    └── RedisService.java                 # Redis 服务实现
```

## 使用方式

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.ysmjjsy.goya</groupId>
    <artifactId>redis-boot-starter</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
# Redisson 配置（使用 Redisson 默认配置或自定义）
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0

# Goya 缓存配置
platform:
  cache:
    type: redis             # 使用 Redis 缓存
    key-prefix: "goya:"     # 缓存键前缀
    default-ttl: 30m        # 默认过期时间
    redis:
      lock-timeout: 30s     # 锁超时时间
      lock-wait-time: 10s   # 锁等待时间
      watchdog-timeout: 30s # 看门狗超时时间
      enable-stats: false   # 是否启用统计
```

### 3. 使用缓存服务

```java
@Autowired
private ICacheService cacheService;

// 使用方式与 component-cache 中的示例相同
// 但底层使用 Redis 实现，支持分布式环境
```

### 4. 使用 Redis 特有功能

#### 原子计数器

```java
@Autowired
private IRedisService redisService;

// 递增计数器
Long value = redisService.increment("page:views");

// 递增指定值
Long value = redisService.incrementBy("page:views", 10);

// 获取计数值
Long value = redisService.getCounter("page:views");
```

#### 发布订阅

```java
// 发布消息
redisService.publish("notifications", "New message");

// 订阅主题
int listenerId = redisService.subscribe("notifications", message -> {
    System.out.println("Received: " + message);
});

// 取消订阅
redisService.unsubscribe("notifications", listenerId);
```

#### 分布式信号量

```java
// 设置许可数量
redisService.trySetPermits("resource:limit", 10);

// 获取许可
if (redisService.tryAcquire("resource:limit", Duration.ofSeconds(5))) {
    try {
        // 执行需要许可的操作
    } finally {
        redisService.release("resource:limit");
    }
}
```

#### 分布式倒计时门闩

```java
// 设置倒计时数量
redisService.trySetCount("task:complete", 3);

// 在各个任务完成时倒计时
redisService.countDown("task:complete");

// 等待所有任务完成
redisService.await("task:complete", Duration.ofMinutes(5));
```

## 设计说明

### 1. 基于 Redisson

- 使用 Redisson 作为 Redis 客户端
- 支持单机、集群、哨兵等多种部署模式
- 自动连接管理和故障转移

### 2. 分布式锁实现

- 使用 Redisson 的 `RLock` 实现
- 支持自动续约（看门狗机制）
- 支持超时和等待时间控制

### 3. 缓存实现

- 使用 `RMapCache` 实现缓存功能
- 支持 TTL 和 maxIdleTime
- cacheName 作为 Map 的 key，实现命名空间隔离

### 4. 条件化配置

- 当 Redisson 依赖存在且配置正确时自动注册
- 通过 `@ConditionalOnBean` 和 `@ConditionalOnProperty` 实现条件加载

## 性能优化

1. **连接池配置**：合理设置 Redisson 连接池大小
2. **批量操作**：尽量使用批量 API 减少网络往返
3. **过期时间**：合理设置 TTL，避免 Redis 内存溢出
4. **序列化**：Redisson 默认使用高效的序列化方式

## 注意事项

1. **Redisson 版本**：当前使用 Redisson 4.0.0
2. **Redis 版本**：建议使用 Redis 5.0+
3. **分布式锁**：确保网络稳定，避免锁失效
4. **过期时间**：合理设置锁和缓存的过期时间
5. **键命名**：使用有意义的键名，避免冲突

## 参考资料

- [Redisson 官方文档](https://github.com/redisson/redisson/wiki)
- [Redis 官方文档](https://redis.io/docs/)
- [Redisson Spring Boot Starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter)

