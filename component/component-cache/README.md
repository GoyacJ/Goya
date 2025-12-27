# component-cache 模块

## 简介

`component-cache` 是 Goya 项目的企业级多级缓存框架，提供类型安全、高性能的缓存解决方案。基于 Spring Cache SPI，支持本地缓存（L1）和远程缓存（L2）的多级架构，并提供丰富的企业级特性。

## 核心特性

### 1. 类型安全
- `GoyaCache<K, V>` 支持泛型，提供编译期类型检查
- `ICacheService` 使用泛型方法，支持类型推断
- 序列化时保存类型信息，反序列化时自动恢复类型

### 2. 多级缓存架构
- **L1（本地缓存）**：基于 Caffeine，提供极低延迟的进程内缓存
- **L2（远程缓存）**：基于 Redisson，提供跨节点的分布式缓存
- **灵活配置**：支持 L1_ONLY、L2_ONLY、L1_L2 三种模式

### 3. 统一序列化
- 基于 Jackson 3 的 `JsonMapper` 统一序列化策略
- 序列化格式包含类型信息：`{"@type":"类型全限定名","data":实际数据}`
- Key 和 Value 使用统一的序列化机制，确保配置一致性

### 4. 企业级特性
- **布隆过滤器**：缓存穿透保护，快速过滤不存在的 key
- **降级策略**：L2 失败时的智能降级处理
- **缓存回填**：L2 命中后异步回填 L1，提升后续访问性能（带并发控制）
- **跨节点同步**：基于事件机制的缓存失效通知（支持幂等性）
- **监控指标**：L1/L2 命中率、延迟统计、热Key检测、回源监控等企业级指标
- **批量操作优化**：利用 L1/L2 批量 API，减少网络往返次数
- **序列化版本控制**：支持版本号机制，为未来版本兼容性做准备

## 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                    应用层（Spring Cache / ICacheService）          │
├─────────────────────────────────────────────────────────────────┤
│  @Cacheable / @CacheEvict 等注解                                  │
│  ICacheService (类型安全的泛型方法)                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    GoyaCacheManager (CacheManager)                │
│  - 管理所有 GoyaCache 实例                                        │
│  - 实现 Spring Cache SPI                                         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    GoyaCache<K, V> (Cache)                       │
│  - 多级缓存编排（L1 + L2）                                       │
│  - 布隆过滤器集成                                                │
│  - 缓存回填管理                                                   │
│  - 事件发布                                                      │
└─────────────────────────────────────────────────────────────────┘
         ↓                                    ↓
┌─────────────────────┐          ┌─────────────────────┐
│  LocalCache (L1)    │          │  RemoteCache (L2)   │
│  - Caffeine         │          │  - Redisson         │
│  - NoOpLocalCache   │          │  - NoOpRemoteCache  │
└─────────────────────┘          └─────────────────────┘
```

## 核心组件

### GoyaCache<K, V>
多级缓存编排实现，支持泛型类型安全。

**特性**：
- 实现 Spring Cache SPI，完全兼容 `@Cacheable`、`@CacheEvict` 等注解
- 支持类型安全方法：`getTyped()`、`putTyped()`、`evictTyped()`、`getTypedValue()`
- 支持批量操作：`batchGetTyped()`、`batchPutTyped()`、`batchEvictTyped()`（利用 L1/L2 批量 API）
- 自动编排 L1 和 L2 的访问逻辑
- 集成布隆过滤器进行缓存穿透保护
- 支持异步回填 L1（带并发控制，防止重复回填）
- 写入顺序优化：优先写入 L2，确保数据一致性

### ICacheService
高级缓存服务接口，提供类型安全的泛型方法。

**特性**：
- 所有方法使用泛型方法（`<K, V>`），支持类型推断
- 支持批量操作：`batchGet()`、`batchPut()`、`batchEvict()`（已优化，利用 L1/L2 批量 API）
- 支持缓存预热：`warmUp()`
- 支持统计信息：`getStatistics()`（包含企业级指标）
- 支持热Key检测：`getHotKeys()`（Top N 热Key查询）
- 支持自定义 TTL：`put(cacheName, key, value, ttl)`

### CacheFactory
统一的缓存工厂，负责创建 `GoyaCache` 实例。

**特性**：
- 根据 `CacheLevel` 自动创建对应的 L1/L2 实例
- 支持便捷方法：`createL1Only()`、`createL2Only()`、`createL1L2()`
- 支持动态配置：`createCache(cacheName, configurator)`

### CacheSpecification
单个缓存的完整配置规范。

**配置项**：
- `keyPrefix`：缓存键前缀
- `cacheLevel`：缓存级别（L1_ONLY / L2_ONLY / L1_L2）
- `ttl`：L2 缓存过期时间
- `localMaxSize`：L1 缓存最大容量
- `localTtlStrategy`：L1 TTL 计算策略
- `enableBloomFilter`：是否启用布隆过滤器
- `fallbackStrategy`：降级策略类型
- `consistencyLevel`：一致性等级（STRONG / EVENTUAL）

### 一致性等级说明

缓存写入时的一致性保证语义：

- **STRONG（强一致性）**：
  - 要求 L2 写入成功 + L1 写入成功，确保当前节点的 L1 和 L2 缓存一致
  - L2 写入失败时，不写入 L1，抛出异常
  - L1 写入失败时，回滚 L2（如果可能），抛出异常
  - **注意**：此等级仅保证当前节点的 L1 和 L2 缓存一致，不保证跨节点立即一致。跨节点一致性通过事件机制异步保证
  - 适用于对数据一致性要求极高的场景，如金融交易、库存扣减等

- **EVENTUAL（最终一致性，默认）**：
  - 要求 L2 写入成功，L1 和跨节点同步异步执行
  - L2 写入成功即可返回，不等待 L1 和跨节点同步
  - L2 写入失败时，根据降级策略处理（可能降级到 L1）
  - L1 写入失败时，记录日志但不影响主流程
  - 适用于大多数业务场景，如用户信息、配置数据等

## 配置示例

### 基础配置

```yaml
platform:
  cache:
    # 全局缓存键前缀
    key-prefix: "goya:cache:"
    
    # 默认配置
    default-config:
      ttl: PT1H                    # L2 缓存过期时间（1小时）
      allow-null-values: true      # 是否允许缓存 null 值
      local-max-size: 10000        # L1 缓存最大容量
      cache-level: L1_L2           # 缓存级别：L1_ONLY / L2_ONLY / L1_L2
      enable-bloom-filter: false   # 是否启用布隆过滤器
      fallback-strategy: degrade-to-l1  # 降级策略
    
    # 特定缓存的配置（覆盖默认配置）
    caches:
      userCache:
        ttl: PT30M
        local-max-size: 5000
        cache-level: L1_L2
        enable-bloom-filter: true
        bloom-filter-expected-insertions: 1000000
        bloom-filter-false-positive-rate: 0.03
      
      configCache:
        ttl: PT24H
        cache-level: L2_ONLY        # 仅使用远程缓存
      
      hotDataCache:
        ttl: PT5M
        cache-level: L1_ONLY        # 仅使用本地缓存
```

### 缓存级别说明

- **L1_ONLY**：仅使用本地缓存（Caffeine），适用于单机场景或对一致性要求不高的场景
- **L2_ONLY**：仅使用远程缓存（Redis），适用于分布式场景但不需要本地缓存加速
- **L1_L2**：使用多级缓存（默认），提供最佳性能和一致性平衡

## 使用示例

### 1. 使用 Spring Cache 注解（推荐）

```java
@Service
public class UserService {
    
    @Cacheable(value = "userCache", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id);
    }
    
    @CacheEvict(value = "userCache", key = "#user.id")
    public void updateUser(User user) {
        userRepository.save(user);
    }
    
    @Cacheable(value = "userCache", key = "#id")
    public User getUserWithLoader(Long id) {
        // 如果缓存未命中，会执行此方法并缓存结果
        return userRepository.findById(id);
    }
}
```

### 2. 使用 ICacheService（类型安全）

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final ICacheService cacheService;
    
    public User getUser(Long id) {
        // 类型安全：自动推断返回类型为 User
        return cacheService.get("userCache", id);
    }
    
    public User getUserWithLoader(Long id) {
        // 带加载器的获取方法
        return cacheService.get("userCache", id, () -> {
            return userRepository.findById(id);
        });
    }
    
    public void updateUser(User user) {
        userRepository.save(user);
        // 失效缓存
        cacheService.evict("userCache", user.getId());
    }
    
    public void saveUser(User user, Duration ttl) {
        // 使用自定义 TTL
        cacheService.put("userCache", user.getId(), user, ttl);
    }
    
    public Map<Long, User> batchGetUsers(Set<Long> ids) {
        // 批量获取（已优化，利用 L1/L2 批量 API，减少网络往返）
        return cacheService.batchGet("userCache", ids);
    }
    
    public void batchSaveUsers(Map<Long, User> users) {
        // 批量写入（已优化，利用 L1/L2 批量 API）
        cacheService.batchPut("userCache", users);
    }
    
    public void batchDeleteUsers(Set<Long> ids) {
        // 批量失效（已优化）
        cacheService.batchEvict("userCache", ids);
    }
    
    public void warmUpCache(Set<Long> ids) {
        // 缓存预热
        cacheService.warmUp("userCache", this::loadUser, ids);
    }
    
    private User loadUser(Long id) {
        return userRepository.findById(id);
    }
}
```

### 3. 使用 GoyaCache 类型安全方法

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final CacheManager cacheManager;
    
    public User getUser(Long id) {
        // 获取 GoyaCache 实例（类型擦除，但可以使用类型安全方法）
        Cache cache = cacheManager.getCache("userCache");
        if (cache instanceof GoyaCache) {
            @SuppressWarnings("unchecked")
            GoyaCache<Long, User> goyaCache = (GoyaCache<Long, User>) cache;
            // 使用类型安全方法
            return goyaCache.getTypedValue(id);
        }
        // 降级到标准 Spring Cache API
        Cache.ValueWrapper wrapper = cache.get(id);
        return wrapper != null ? (User) wrapper.get() : null;
    }
}
```

### 4. 使用 CacheFactory 创建特定缓存

```java
@Service
@RequiredArgsConstructor
public class SpecialCacheService {
    
    private final CacheFactory cacheFactory;
    private final CacheSpecificationResolver resolver;
    
    public void useL1OnlyCache() {
        // 创建仅本地缓存
        String cacheName = "hotData";
        CacheSpecification spec = resolver.resolve(cacheName);
        GoyaCache<String, String> cache = cacheFactory.createL1Only(cacheName, spec);
        
        cache.putTyped("key1", "value1");
        String value = cache.getTypedValue("key1");
    }
    
    public void useCustomConfig() {
        // 使用配置构建器动态配置
        GoyaCache<String, Object> cache = cacheFactory.createCache("customCache", builder -> {
            builder.ttl(Duration.ofMinutes(30));
            builder.localMaxSize(5000);
            builder.cacheLevel(CacheLevel.L1_L2);
            builder.enableBloomFilter(true);
        });
    }
}
```

## 多级缓存访问流程

### get() 操作流程

```
1. 布隆过滤器检查（如果启用）
   ├─ 判断"不存在" → 记录过滤指标，继续查询 L2（避免误判）
   └─ 判断"可能存在" → 继续查询

2. 查询 L1（本地缓存）
   ├─ 命中 → 返回结果，记录 L1 命中
   └─ 未命中 → 继续查询 L2

3. 查询 L2（远程缓存）
   ├─ 命中 → 异步回填 L1，返回结果，记录 L2 命中
   └─ 未命中 → 返回 null，记录未命中

4. 如果 L2 查询失败
   └─ 根据降级策略处理（降级到 L1 / 快速失败 / 忽略）
```

### put() 操作流程

```
1. Null 值处理
   └─ 如果允许 null 值，包装为 NullValueWrapper

2. 写入 L2（同步，优先写入，确保持久化）
   ├─ 成功 → 继续
   └─ 失败 → 根据降级策略处理，继续写入 L1（保证当前节点可用性）

3. 写入 L1（同步，保证当前节点一致性）
   └─ 失败 → 记录错误日志，不影响 L2

4. 更新布隆过滤器（异步，失败不影响主流程）
```

**写入顺序优化**：
- 优先写入 L2（远程缓存），确保数据持久化和跨节点一致性
- 如果 L2 写入失败，根据降级策略处理，但仍尝试写入 L1 保证当前节点可用性
- 这种顺序避免了 L1 写入成功但 L2 失败导致的数据不一致问题

### evict() 操作流程

```
1. 失效 L1（同步）
2. 失效 L2（同步）
3. 发布失效事件（其他节点会收到通知）
```

## 序列化机制

### 序列化格式

```json
{
  "@type": "com.example.User",
  "@version": "1",
  "data": {
    "id": 1,
    "name": "张三",
    "email": "zhangsan@example.com"
  }
}
```

### 类型信息保存

- `@type`：值类型的全限定名（必需）
- `@version`：版本号（可选，默认 "1"，用于未来版本兼容性控制）
- `data`：实际数据（必需）
- `@nullValueWrapper`：NullValueWrapper 标记（特殊值）

### 版本兼容性

- 序列化时自动添加 `@version` 字段（默认版本 "1"）
- 反序列化时读取版本号，为未来版本转换做准备
- 向后兼容：旧数据没有版本号时使用默认版本

### 序列化策略

- **Key 序列化**：使用 `CacheKeySerializer`（基于 `JsonUtils.toJson()`）
- **Value 序列化**：使用 `TypedJsonMapperCodec`（基于 `JsonMapper`）
- **统一配置**：Key 和 Value 使用相同的 `JsonMapper`，确保时间格式、枚举处理等配置一致

## 高级特性

### 布隆过滤器

用于缓存穿透保护，快速过滤不存在的 key。

```yaml
platform:
  cache:
    caches:
      userCache:
        enable-bloom-filter: true
        bloom-filter-expected-insertions: 1000000  # 预期插入量
        bloom-filter-false-positive-rate: 0.03     # 误判率
```

**工作原理**：
1. 写入时：异步更新布隆过滤器
2. 查询时：先检查布隆过滤器，如果判断"不存在"则快速返回（但仍查询 L2 避免误判）

### 降级策略

当 L2 缓存操作失败时的处理策略。

**策略类型**：
- `degrade-to-l1`：降级到 L1，从本地缓存获取
- `fast-fail`：快速失败，直接返回 null
- `ignore`：忽略错误，继续执行

### 缓存回填

L2 命中后，异步将数据回填到 L1，提升后续访问性能。

**特性**：
- 异步执行，不阻塞主流程
- 并发控制：使用 `computeIfAbsent` 确保同一 key 只回填一次
- 双重检查：回填前检查 L1 是否已有值，避免不必要的回填
- 失败不影响主流程（L2 已命中，数据可用）
- 自动记录回填成功/失败指标

### 跨节点同步

基于 Spring 事件机制的缓存失效通知，支持幂等性处理。

**流程**：
1. 节点 A 失效缓存 → 发布 `CacheEvictionEvent`
2. `RedisCacheEvictionSubscriber` 接收事件 → 生成消息 ID（UUID）→ 转发到 Redis Pub/Sub
3. 其他节点订阅 Redis 消息 → 检查消息 ID（幂等性）→ 失效本地 L1 缓存

**幂等性保证**：
- 每个失效消息携带唯一的消息 ID（UUID）
- 节点维护已处理消息 ID 的集合（TTL 5 秒）
- 重复消息自动过滤，防止重复处理
- 定期清理过期的消息 ID，防止内存泄漏

## 监控指标

### 获取统计信息

```java
@Service
@RequiredArgsConstructor
public class CacheMonitorService {
    
    private final ICacheService cacheService;
    
    public void printStatistics(String cacheName) {
        ICacheService.CacheStatistics stats = cacheService.getStatistics(cacheName);
        
        // 基础指标
        System.out.println("L1 命中次数: " + stats.getL1Hits());
        System.out.println("L2 命中次数: " + stats.getL2Hits());
        System.out.println("未命中次数: " + stats.getMisses());
        System.out.println("命中率: " + stats.getHitRate());
        System.out.println("布隆过滤器误判次数: " + stats.getBloomFilterFalsePositives());
        System.out.println("回填成功率: " + stats.getRefillSuccessRate());
        
        // 企业级指标
        System.out.println("L1 平均延迟: " + stats.getL1AvgLatencyMs() + " ms");
        System.out.println("L2 平均延迟: " + stats.getL2AvgLatencyMs() + " ms");
        System.out.println("L1 P99 延迟: " + stats.getL1P99LatencyMs() + " ms");
        System.out.println("L2 P99 延迟: " + stats.getL2P99LatencyMs() + " ms");
        System.out.println("回源次数: " + stats.getSourceLoadCount());
        System.out.println("回源平均延迟: " + stats.getSourceLoadAvgLatencyMs() + " ms");
    }
    
    public void analyzeHotKeys(String cacheName) {
        // 获取 Top 10 热Key
        List<ICacheService.HotKey> hotKeys = cacheService.getHotKeys(cacheName, 10);
        
        System.out.println("Top 10 热Key:");
        for (ICacheService.HotKey hotKey : hotKeys) {
            System.out.println("  Key: " + hotKey.getKey() + 
                             ", 访问次数: " + hotKey.getAccessCount());
        }
    }
}
```

### 统计指标说明

#### 基础指标
- **L1 Hits**：L1 缓存命中次数
- **L2 Hits**：L2 缓存命中次数
- **Misses**：缓存未命中次数
- **Hit Rate**：命中率 = (L1 Hits + L2 Hits) / (L1 Hits + L2 Hits + Misses)
- **Bloom Filter False Positives**：布隆过滤器误判次数
- **Refill Success Rate**：L1 回填成功率

#### 企业级指标
- **L1 Avg Latency**：L1 平均查询延迟（毫秒），用于性能分析
- **L2 Avg Latency**：L2 平均查询延迟（毫秒），用于网络性能分析
- **L1 P99 Latency**：L1 P99 延迟（毫秒），用于识别慢查询
- **L2 P99 Latency**：L2 P99 延迟（毫秒），用于识别网络瓶颈
- **Source Load Count**：回源次数（缓存未命中后从数据源加载的次数）
- **Source Load Avg Latency**：回源平均延迟（毫秒），用于监控数据源性能

#### 热Key检测
- **HotKey**：访问频率最高的 Key 列表
- 采样机制：默认采样率 10%，控制内存开销
- 自动清理：超过最大跟踪数量时，自动清理低频 Key
- 用途：容量规划、热Key识别、性能优化

## 自动装配逻辑

### 场景 1：有 redis-boot-starter 依赖

```
1. RedisAutoConfiguration 注册：
   - CacheKeySerializer Bean
   - TypedJsonMapperCodec Bean
   - RemoteCacheFactory Bean（使用 Redisson + TypedJsonMapperCodec）
   - RedisCacheEvictionSubscriber Bean

2. CacheAutoConfiguration 注册：
   - CacheSpecificationResolver Bean
   - BloomFilterManager Bean
   - CacheRefillManager Bean
   - CacheEventPublisher Bean
   - LocalCacheFactory Bean（Caffeine）
   - CacheFactory Bean
   - GoyaCacheManager Bean（作为 CacheManager）
   - ICacheService Bean（DefaultCacheService）

3. 应用使用：
   - @Cacheable 等注解 → GoyaCacheManager → GoyaCache<K, V>
   - ICacheService → DefaultCacheService → GoyaCacheManager → GoyaCache<K, V>
```

### 场景 2：无 redis-boot-starter 依赖

```
1. CacheAutoConfiguration 注册：
   - 所有核心 Bean（同上）
   - 但没有 RemoteCacheFactory Bean

2. GoyaCache 创建时：
   - 检测到没有 RemoteCacheFactory → 使用 NoOpRemoteCache
   - 实际效果：L1_ONLY 模式
```

## 扩展指南

### SPI 扩展点

`component-cache` 提供了以下 SPI 扩展点，允许开发者自定义实现：

1. **LocalCacheFactory**：本地缓存工厂接口（位于 `GoyaCacheManager` 内部）
2. **RemoteCacheFactory**：远程缓存工厂接口（位于 `GoyaCacheManager` 内部）
3. **FallbackStrategyFactory**：降级策略工厂接口（位于 `GoyaCacheManager` 内部）
4. **CacheKeySerializer**：缓存键序列化器接口
5. **CacheMetrics**：监控指标接口

### 实现新的 RemoteCache

1. 实现 `RemoteCache` 接口
2. 在 starter 中注册 `RemoteCacheFactory` Bean（实现 `GoyaCacheManager.RemoteCacheFactory` 接口）
3. 自动被 `CacheFactory` 使用

示例：

```java
@AutoConfiguration
public class CustomCacheAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(name = "remoteCacheFactory")
    public GoyaCacheManager.RemoteCacheFactory remoteCacheFactory(
            CustomCacheClient client) {
        return (cacheName, spec) -> {
            return new CustomRemoteCache(cacheName, client, spec);
        };
    }
}
```

**注意**：工厂接口定义在 `GoyaCacheManager` 内部，这是设计选择，用于减少包暴露。这些接口是 SPI 扩展点，开发者应该通过实现这些接口来扩展功能。

### 自定义序列化器

```java
@Component
public class CustomCacheKeySerializer implements CacheKeySerializer {
    
    @Override
    public byte[] serialize(Object key) {
        // 自定义序列化逻辑
        return JsonUtils.toJson(key).getBytes(StandardCharsets.UTF_8);
    }
}
```

## 性能优化建议

1. **合理设置 L1 容量**：根据应用内存大小调整 `local-max-size`
2. **合理设置 TTL**：根据数据更新频率调整 `ttl`
3. **使用批量操作**：减少网络往返，使用 `batchGet()`、`batchPut()`、`batchEvict()`（已优化，利用 L1/L2 批量 API）
4. **启用布隆过滤器**：对于缓存穿透风险高的场景，启用布隆过滤器
5. **选择合适的缓存级别**：
   - 高频访问、单机场景 → `L1_ONLY`
   - 强一致性、分布式场景 → `L2_ONLY`
   - 通用场景 → `L1_L2`（默认）
6. **监控热Key**：定期查询热Key列表，识别访问热点，优化缓存策略
7. **关注延迟指标**：通过 P99 延迟识别性能瓶颈，优化慢查询
8. **监控回源频率**：高回源频率可能表示缓存命中率低，需要调整缓存策略

## 注意事项

1. **类型擦除**：Java 泛型类型擦除，运行时无法获取 `K` 和 `V` 的实际类型，类型信息通过序列化层保存
2. **Caffeine TTL 限制**：Caffeine 本地缓存使用全局 TTL 策略，不支持每个 key 独立的 TTL（传入的 ttl 参数会被忽略）
3. **序列化兼容性**：
   - 新格式包含 `@version` 字段，为未来版本兼容性做准备
   - 旧数据没有版本号时自动使用默认版本（向后兼容）
4. **跨节点同步**：需要有 `RedisCacheEvictionSubscriber` 实现（redis-boot-starter 提供）
5. **热Key检测**：
   - 使用采样机制（默认 10%），控制内存开销
   - 最大跟踪 Key 数量为 10000，超过后自动清理低频 Key
   - 采样可能导致部分低频 Key 未被记录
6. **延迟统计**：
   - 使用滑动窗口（1000 个样本）计算平均延迟和 P99 延迟
   - 批量操作的延迟为平均延迟（总延迟 / Key 数量）

## 日志说明

```
[Goya] |- component [cache] CacheAutoConfiguration auto configure.
[Goya] |- component [cache] CacheAutoConfiguration |- bean [goyaCacheManager] register.
[Goya] |- starter [redis] RedisAutoConfiguration auto configure.
Created GoyaCache: name=userCache, level=L1_L2, ttl=PT1H, localMaxSize=10000, bloomFilterEnabled=true
```

## 技术栈

- **Spring Boot 4.0.1**：基础框架
- **Spring Cache**：缓存抽象
- **Caffeine**：L1 本地缓存实现
- **Redisson 4.0.0**：L2 远程缓存实现
- **Jackson 3**：统一序列化（JsonMapper）
- **Netty ByteBuf**：Redisson Codec 底层实现

## 参考文档

- [Spring Cache 文档](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Caffeine 文档](https://github.com/ben-manes/caffeine)
- [Redisson 文档](https://github.com/redisson/redisson)
