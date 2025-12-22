# component-cache 模块

## 简介

`component-cache` 是 Goya 项目的缓存抽象层，提供统一的缓存接口和默认的混合缓存实现（Hybrid Cache）。

## 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                      component-cache (抽象层)                    │
├─────────────────────────────────────────────────────────────────┤
│  ICacheService (统一缓存接口)                                     │
│      ↑                                                          │
│  IL2Cache (L2 分布式缓存接口)                                     │
│      ↑                                                          │
│  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────┐  │
│  │LocalCacheService │  │RemoteCacheService│  │HybridCache   │  │
│  │  (本地缓存)       │  │  (远程缓存包装器)  │  │Service       │  │
│  │  - Caffeine      │  │  - IL2Cache      │  │  (默认实现)    │  │
│  └──────────────────┘  └───────────────────┘  │  - L1 + L2   │  │
│                                               └──────────────┘  │
│  ┌──────────────────┐                                           │
│  │CacheService      │                                           │
│  │Factory           │ (工厂类，创建各种缓存实例)                  │
│  └──────────────────┘                                           │
└─────────────────────────────────────────────────────────────────┘
                                    ↑
                                    │ 实现 IL2Cache
┌─────────────────────────────────────────────────────────────────┐
│  redis-boot-starter                                              │
│  ├── RedisCacheService (实现 IL2Cache)                          │
│  ├── RedisCacheInvalidatePublisher                               │
│  └── RedisCacheInvalidateListener                                │
└─────────────────────────────────────────────────────────────────┘
```

## 核心特性

1. **默认混合缓存架构**
   - L1 (LocalCacheService): 固定启用，高性能本地缓存 (Caffeine)
   - L2 (RemoteCacheService): 通过接口支持多种分布式实现
   - 自动检测：有 L2 实现时自动启用混合缓存，无则降级为 L1 Only

2. **灵活的使用方式**
   - **默认注入**：`ICacheService` → `HybridCacheService`（推荐）
   - **工厂创建**：通过 `CacheServiceFactory` 创建特定缓存实例
     - `createLocal()`: 仅本地缓存（高频访问场景）
     - `createRemote()`: 仅远程缓存（强一致性场景）
     - `createHybrid()`: 混合缓存（默认）

3. **清晰的命名**
   - `LocalCacheService`: 本地缓存服务
   - `RemoteCacheService`: 远程缓存服务
   - `HybridCacheService`: 混合缓存服务（多级）

4. **高可用设计**
   - L2 不可用时自动降级为 L1 Only
   - Pub/Sub 失败不阻塞主流程
   - 支持缓存统计和监控

## 配置示例

### 基础配置

```yaml
platform:
  cache:
    key-prefix: "goya:"
    default-ttl: PT30M
    enable-stats: true
    
    # 本地缓存（Caffeine）配置
    caffeine-max-size: 10000
    caffeine-ttl: PT5M  # 可选，不设置则使用 default-ttl
    
    # 缓存失效消息主题
    invalidate-topic: "cache:invalidate"
```

## 使用示例

### 1. 默认使用（推荐）

大部分场景使用默认的混合缓存：

```java
@Service
public class UserService {
    
    @Autowired
    private ICacheService cacheService;  // → HybridCacheService
    
    public User getUser(Long id) {
        return cacheService.get("users", id, this::loadFromDb);
    }
    
    public void updateUser(User user) {
        userRepository.save(user);
        // 自动失效本地和远程缓存，并通知其他节点
        cacheService.remove("users", user.getId());
    }
}
```

### 2. 特殊场景：仅本地缓存

高频访问的数据，不需要跨节点共享：

```java
@Service
public class HotDataService {
    
    private final LocalCacheService localCache;
    
    public HotDataService(CacheServiceFactory factory) {
        // 创建本地缓存实例
        this.localCache = factory.createLocal();
    }
    
    public String getHotData(String key) {
        return localCache.get("hotData", key, this::loadHotData);
    }
}
```

### 3. 特殊场景：仅远程缓存

多实例共享配置，需要强一致性：

```java
@Service
public class SharedConfigService {
    
    private final RemoteCacheService remoteCache;
    
    public SharedConfigService(CacheServiceFactory factory) {
        // 创建远程缓存实例
        this.remoteCache = factory.createRemote();
    }
    
    public Config getConfig(String key) {
        return remoteCache.get("sharedConfig", key, this::loadConfig);
    }
}
```

## 装配逻辑

### 场景 1: 有 redis-boot-starter 依赖

```
1. RedisAutoConfiguration 注册 RedisCacheService 为 IL2Cache
2. CacheAutoConfiguration 检测到 IL2Cache bean
3. 注册 HybridCacheService (L1 + L2)
4. 应用使用混合缓存（自动降级、跨节点同步）
```

### 场景 2: 无任何 L2 starter 依赖

```
1. 没有 IL2Cache bean
2. CacheAutoConfiguration 注册 HybridCacheService (L1 Only)
3. 应用使用本地缓存（无跨节点同步）
```

## 扩展指南

### 实现新的 L2 缓存

1. 实现 `IL2Cache` 接口
2. 在 starter 中注册为 Spring Bean
3. 自动被 `CacheAutoConfiguration` 检测并使用

示例：

```java
@Component
public class MongodbCacheService extends AbstractCacheService implements IL2Cache {
    
    @Override
    public String getCacheType() {
        return "mongodb";
    }
    
    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        // MongoDB 实现
    }
    
    // 实现其他方法...
}
```

## 日志说明

```
[Goya] |- Cache |- Hybrid cache initialized: Local (Caffeine) + Remote (redis), nodeId: xxx
```

- 表示混合缓存已启动
- `Local (Caffeine)`: L1 本地缓存
- `Remote (redis)`: L2 远程缓存类型
- `nodeId`: 节点 ID，用于跨节点消息过滤

```
[Goya] |- Cache |- Hybrid cache degraded to Local only (no remote cache implementation found)
```

- 表示没有找到 L2 实现，降级为 L1 Only

## 性能优化建议

1. **合理设置 L1 容量**：根据应用内存大小调整 `caffeine-max-size`
2. **合理设置 TTL**：根据数据更新频率调整 `caffeine-ttl`
3. **使用批量操作**：减少网络往返
4. **特定场景使用特定缓存**：
   - 高频访问 → `LocalCacheService`
   - 强一致性 → `RemoteCacheService`
   - 通用场景 → `HybridCacheService`（默认）

## 监控指标

```java
@Autowired
private HybridCacheService cacheService;

// 获取缓存统计
Map<String, Object> stats = cacheService.getStats();
// stats 包含：nodeId, remoteCacheType, remoteCacheAvailable
```

## 注意事项

1. **Bean 唯一性**：`ICacheService` 只有一个 bean（`HybridCacheService`）
2. **工厂创建的实例**：不是 Spring bean，不会被自动注入到其他组件
3. **L2 可选性**：没有 L2 实现时自动降级，不会抛出异常
4. **跨节点同步**：需要有 `ICacheInvalidatePublisher` 实现（如 Redis Pub/Sub）
