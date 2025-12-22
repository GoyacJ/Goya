package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;
import com.ysmjjsy.goya.component.cache.model.CacheValue;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

/**
 * <p>混合缓存服务（多级缓存：本地 + 远程）</p>
 * <p>特点：</p>
 * <ul>
 *     <li>L1 (LocalCacheService)：快速本地缓存</li>
 *     <li>L2 (RemoteCacheService)：共享远程缓存</li>
 *     <li>自动降级：L2 不可用时只使用 L1</li>
 *     <li>跨节点一致性：通过 Pub/Sub 同步</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>通用场景（默认推荐）</li>
 *     <li>兼顾性能和一致性</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     @Autowired
 *     private ICacheService cacheService;  // → HybridCacheService
 *
 *     public User getUser(Long id) {
 *         return cacheService.get("users", id, this::loadFromDb);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see AbstractCacheService
 * @see LocalCacheService
 * @see RemoteCacheService
 */
@Slf4j
public class HybridCacheService extends AbstractCacheService {

    private final LocalCacheService localCache;
    private final RemoteCacheService remoteCache;
    private final ICacheInvalidatePublisher publisher;
    private final String nodeId;
    private volatile boolean remoteCacheAvailable = true;

    /**
     * 构造混合缓存服务
     *
     * @param properties    缓存配置
     * @param remoteCache   远程缓存（可选，为 null 时只使用本地）
     * @param publisher     失效消息发布器（可选）
     */
    public HybridCacheService(
            CacheProperties properties,
            RemoteCacheService remoteCache,
            ICacheInvalidatePublisher publisher) {
        super(properties);
        this.localCache = new LocalCacheService(properties);
        this.remoteCache = remoteCache;
        this.publisher = publisher;
        this.nodeId = UUID.randomUUID().toString();

        logInitialization();
    }

    private void logInitialization() {
        if (hasRemoteCache()) {
            log.info("[Goya] |- Cache |- Hybrid cache initialized: Local (Caffeine) + Remote ({}), nodeId: {}",
                    remoteCache.getRemoteType(), nodeId);
        } else {
            log.warn("[Goya] |- Cache |- Hybrid cache degraded to Local only " +
                    "(no remote cache implementation found), nodeId: {}", nodeId);
        }
    }

    private boolean hasRemoteCache() {
        return remoteCache != null && remoteCacheAvailable && remoteCache.isAvailable();
    }

    /**
     * 获取节点 ID
     *
     * @return 节点 ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 获取本地缓存（供失效监听器使用）
     *
     * @return LocalCacheService 实例
     */
    public LocalCacheService getLocalCache() {
        return localCache;
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        // 1. 查询本地缓存（存储的是 CacheValue<V>）
        CacheValue<V> l1Value = localCache.get(cacheName, key);

        // 2. 查询远程缓存
        if (hasRemoteCache()) {
            try {
                CacheValue<V> l2Value = remoteCache.get(cacheName, key);

                // 3. 比较版本号，使用较新的值
                if (l2Value != null && l2Value.isNewerThan(l1Value)) {
                    // L2 版本更新，回填 L1
                    localCache.put(cacheName, key, l2Value, getDefaultTtl());
                    log.trace("[Goya] |- Cache |- L2 hit with newer version, cache: {}, key: {}, version: {}",
                            cacheName, key, l2Value.version());
                    return l2Value.value();
                }

                // L1 版本较新或相同，使用 L1
                if (l1Value != null) {
                    log.trace("[Goya] |- Cache |- L1 hit, cache: {}, key: {}, version: {}",
                            cacheName, key, l1Value.version());
                    return l1Value.value();
                }

                // 只有 L2 有值（L1 为 null）
                if (l2Value != null) {
                    // 回填 L1
                    localCache.put(cacheName, key, l2Value, getDefaultTtl());
                    log.trace("[Goya] |- Cache |- L2 hit, backfill L1, cache: {}, key: {}, version: {}",
                            cacheName, key, l2Value.version());
                    return l2Value.value();
                }
            } catch (Exception e) {
                markRemoteUnavailable("get", e);
                // L2 失败，降级使用 L1
                if (l1Value != null) {
                    log.debug("[Goya] |- Cache |- L2 failed, fallback to L1, cache: {}, key: {}",
                            cacheName, key);
                    return l1Value.value();
                }
            }
        } else {
            // 没有远程缓存，直接使用 L1
            if (l1Value != null) {
                log.trace("[Goya] |- Cache |- L1 hit (no L2), cache: {}, key: {}", cacheName, key);
                return l1Value.value();
            }
        }

        return null;
    }

    @Override
    protected <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        // 1. 先尝试从缓存获取（使用 doGet，已包含版本号逻辑）
        V value = doGet(cacheName, key);
        if (value != null) {
            return value;
        }

        // 2. 缓存未命中，加载数据
        value = mappingFunction.apply(key);
        if (value != null) {
            // 3. 写入缓存（使用 doPut，已包含版本号逻辑）
            doPut(cacheName, key, value, getDefaultTtl());
        }

        return value;
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();

        // 逐个处理（使用 doGet，包含版本号比较逻辑）
        for (K key : keys) {
            V value = doGet(cacheName, key);
            if (value != null) {
                result.put(key, value);
            }
        }

        return result;
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatchOrLoad(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        Map<K, V> result = new HashMap<>();
        Set<K> missKeys = new HashSet<>();

        // 1. 先尝试从缓存获取
        for (K key : keys) {
            V value = doGet(cacheName, key);
            if (value != null) {
                result.put(key, value);
            } else {
                missKeys.add(key);
            }
        }

        // 2. 加载缺失的值
        if (!missKeys.isEmpty()) {
            Map<? extends K, ? extends V> loadedValues = mappingFunction.apply(missKeys);
            if (loadedValues != null && !loadedValues.isEmpty()) {
                for (Map.Entry<? extends K, ? extends V> entry : loadedValues.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();
                    if (value != null) {
                        result.put(key, value);
                        // 写入缓存（使用 doPut，包含版本号逻辑）
                        doPut(cacheName, key, value, getDefaultTtl());
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected <K, V> void doPut(String cacheName, K key, V value, Duration duration) {
        // 包装为 CacheValue（带版本号）
        CacheValue<V> wrappedValue = CacheValue.of(value);

        // 1. 写入本地（存储 CacheValue）
        localCache.put(cacheName, key, wrappedValue, duration);
        log.trace("[Goya] |- Cache |- L1 put, cache: {}, key: {}, version: {}",
                cacheName, key, wrappedValue.version());

        // 2. 写入远程（存储 CacheValue）
        if (hasRemoteCache()) {
            try {
                remoteCache.put(cacheName, key, wrappedValue, duration);
                log.trace("[Goya] |- Cache |- L2 put, cache: {}, key: {}, version: {}",
                        cacheName, key, wrappedValue.version());
            } catch (Exception e) {
                markRemoteUnavailable("put", e);
                log.warn("[Goya] |- Cache |- L2 put failed, only L1 cached, cache: {}, key: {}, version: {}",
                        cacheName, key, wrappedValue.version());
            }
        }

        // 3. 发布失效消息（携带版本号）
        publishInvalidateMessage(
                CacheInvalidateMessage.ofKey(cacheName, key, nodeId, wrappedValue.version())
        );
    }

    @Override
    protected <K> Boolean doRemove(String cacheName, K key) {
        Boolean result;

        // 1. 删除 L1
        result = localCache.remove(cacheName, key);

        // 2. 删除 L2
        if (hasRemoteCache()) {
            try {
                Boolean l2Result = remoteCache.remove(cacheName, key);
                result = result || l2Result;
            } catch (Exception e) {
                markRemoteUnavailable("remove", e);
            }
        }

        // 3. 发布失效消息（删除操作不需要版本号）
        publishInvalidateMessage(
                CacheInvalidateMessage.ofKey(cacheName, key, nodeId)
        );

        return result;
    }

    @Override
    protected <K> void doRemoveBatch(String cacheName, Set<? extends K> keys) {
        // 1. 删除 L1
        localCache.remove(cacheName, keys);

        // 2. 删除 L2
        if (hasRemoteCache()) {
            try {
                remoteCache.remove(cacheName, keys);
            } catch (Exception e) {
                markRemoteUnavailable("removeBatch", e);
            }
        }

        // 3. 发布失效消息（批量）（删除操作不需要版本号）
        for (K key : keys) {
            publishInvalidateMessage(
                    CacheInvalidateMessage.ofKey(cacheName, key, nodeId)
            );
        }
    }

    @Override
    protected <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        // 1. 先查询 L1
        V value = localCache.get(cacheName, key);
        if (value != null) {
            return value;
        }

        // 2. 查询 L2
        if (hasRemoteCache()) {
            try {
                value = remoteCache.get(cacheName, key);
                if (value != null) {
                    // 回填 L1
                    localCache.put(cacheName, key, value, getDefaultTtl());
                    return value;
                }
            } catch (Exception e) {
                markRemoteUnavailable("computeIfAbsent", e);
            }
        }

        // 3. 加载数据
        value = loader.apply(key);
        if (value != null) {
            // 写入 L1
            localCache.put(cacheName, key, value, getDefaultTtl());

            // 写入 L2
            if (hasRemoteCache()) {
                try {
                    remoteCache.put(cacheName, key, value, getDefaultTtl());
                } catch (Exception e) {
                    markRemoteUnavailable("computeIfAbsent-put", e);
                }
            }
        }

        return value;
    }

    @Override
    protected <K> void doTryLock(String cacheName, K key, Duration expire) {
        // 优先使用 L2 的分布式锁
        if (hasRemoteCache()) {
            try {
                remoteCache.tryLock(cacheName, key, expire);
                return;
            } catch (Exception e) {
                markRemoteUnavailable("tryLock", e);
            }
        }

        // 降级到 L1 的本地锁
        localCache.tryLock(cacheName, key, expire);
    }

    @Override
    protected <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        // 优先使用 L2 的分布式锁
        if (hasRemoteCache()) {
            try {
                return remoteCache.lockAndRun(cacheName, key, expire, action);
            } catch (Exception e) {
                markRemoteUnavailable("lockAndRun", e);
            }
        }

        // 降级到 L1 的本地锁
        return localCache.lockAndRun(cacheName, key, expire, action);
    }

    @Override
    public <K> boolean mightContain(String cacheName, K key) {
        // 优先查询 L2 布隆过滤器（分布式，更准确）
        if (hasRemoteCache()) {
            try {
                return remoteCache.mightContain(cacheName, key);
            } catch (Exception e) {
                log.warn("[Goya] |- Cache |- L2 bloom filter check failed, fallback to L1", e);
            }
        }

        // 降级到 L1 本地布隆过滤器
        return localCache.mightContain(cacheName, key);
    }

    @Override
    public <K> void addToBloomFilter(String cacheName, K key) {
        // 同时添加到 L1 和 L2 布隆过滤器
        localCache.addToBloomFilter(cacheName, key);

        if (hasRemoteCache()) {
            try {
                remoteCache.addToBloomFilter(cacheName, key);
            } catch (Exception e) {
                log.warn("[Goya] |- Cache |- Failed to add to L2 bloom filter", e);
            }
        }
    }

    /**
     * 发布失效消息（不抛出异常）
     */
    /**
     * 发布失效消息
     */
    private void publishInvalidateMessage(CacheInvalidateMessage message) {
        if (publisher != null) {
            try {
                publisher.publish(message);
            } catch (Exception e) {
                log.warn("[Goya] |- Cache |- Failed to publish invalidate message", e);
            }
        }
    }

    /**
     * 标记远程缓存不可用
     */
    private void markRemoteUnavailable(String operation, Exception e) {
        remoteCacheAvailable = false;
        log.warn("[Goya] |- Cache |- Remote cache unavailable during [{}], degraded to local only: {}",
                operation, e.getMessage());
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        // 清空 L1
        localCache.clear(cacheName);

        // 清空 L2
        if (hasRemoteCache()) {
            try {
                remoteCache.clearAll();
            } catch (Exception e) {
                markRemoteUnavailable("clear", e);
            }
        }

        log.info("[Goya] |- Cache |- Cache [{}] cleared", cacheName);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nodeId", nodeId);
        stats.put("remoteCacheType", hasRemoteCache() ? remoteCache.getRemoteType() : "none");
        stats.put("remoteCacheAvailable", remoteCacheAvailable);
        return stats;
    }
}

