# component-cache 缓存组件

## 模块简介

`component-cache` 是 Goya 框架的缓存抽象层模块，提供统一的缓存服务接口，支持多种缓存实现（Caffeine 本地缓存、Redis 分布式缓存）。

## 核心功能

### 1. 缓存操作
- **基本操作**：get、put、remove
- **批量操作**：批量获取、批量删除
- **懒加载**：支持 computeIfAbsent 和 getOrLoad
- **过期控制**：支持设置 TTL

### 2. 分布式锁
- **tryLock**：尝试获取锁
- **lockAndRun**：获取锁并执行操作

### 3. 多种实现
- **CaffeineCacheService**：基于 Caffeine 的本地缓存实现
- **RedisCacheService**：基于 Redisson 的分布式缓存实现（在 redis-boot-starter 中）

## 项目结构

```
component-cache/
├── configuration/
│   ├── CacheAutoConfiguration.java       # 缓存自动配置
│   └── properties/
│       └── CacheProperties.java          # 缓存配置属性
├── constants/
│   └── ICacheConstants.java              # 缓存常量
├── enums/
│   └── CacheTypeEnum.java                # 缓存类型枚举
├── exception/
│   └── CacheException.java               # 缓存异常
└── service/
    ├── ICacheService.java                # 缓存服务接口
    ├── AbstractCacheService.java         # 抽象基类
    └── CaffeineCacheService.java         # Caffeine 实现
```

## 使用方式

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.ysmjjsy.goya</groupId>
    <artifactId>component-cache</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
platform:
  cache:
    type: caffeine          # 缓存类型: caffeine / redis
    key-prefix: "goya:"     # 缓存键前缀
    default-ttl: 30m        # 默认过期时间
    enable-stats: false     # 是否启用统计
```

### 3. 代码示例

```java
@Autowired
private ICacheService cacheService;

// 基本操作
cacheService.put("user", "user:1", userObject);
User user = cacheService.get("user", "user:1");

// 带过期时间
cacheService.put("session", "session:123", session, Duration.ofMinutes(30));

// 懒加载
User user = cacheService.get("user", "user:1", key -> loadUserFromDb(key));

// 批量操作
Set<String> keys = Set.of("user:1", "user:2", "user:3");
Map<String, User> users = cacheService.get("user", keys);

// 分布式锁
cacheService.lockAndRun("lock", "resource:1", Duration.ofSeconds(30), () -> {
    // 执行需要锁保护的操作
});
```

## 设计说明

### 1. 抽象层设计

- **ICacheService**：定义缓存服务接口
- **AbstractCacheService**：提供通用逻辑和模板方法
- **具体实现类**：实现特定缓存技术的操作

### 2. 配置优先级

1. 当配置 `platform.cache.type=redis` 且存在 Redisson 依赖时，使用 Redis 缓存
2. 当配置 `platform.cache.type=caffeine` 或未配置时，使用 Caffeine 本地缓存
3. 通过条件注解实现自动切换

### 3. 扩展性

如需添加新的缓存实现：

1. 继承 `AbstractCacheService`
2. 实现所有抽象方法
3. 在自动配置类中注册 Bean

## 注意事项

1. **本地锁限制**：CaffeineCacheService 的锁功能仅在单个 JVM 内有效，不支持分布式锁
2. **Redis 依赖**：使用 Redis 缓存需要引入 `redis-boot-starter`
3. **键命名**：建议使用有意义的 cacheName 和 key，避免键冲突
4. **过期时间**：合理设置 TTL，避免缓存占用过多内存

## 参考资料

- [Caffeine Github](https://github.com/ben-manes/caffeine)
- [Spring Cache](https://docs.spring.io/spring-framework/reference/integration/cache.html)

