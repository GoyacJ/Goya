package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;
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
        // 1. 查询本地缓存
        V value = localCache.get(cacheName, key);
        if (value != null) {
            log.trace("[Goya] |- Cache |- L1 hit, cache: {}, key: {}", cacheName, key);
            return value;
        }

        // 2. 本地未命中，查询远程缓存
        if (hasRemoteCache()) {
            try {
                value = remoteCache.get(cacheName, key);
                if (value != null) {
                    // 3. 回填本地缓存
                    localCache.put(cacheName, key, value, getDefaultTtl());
                    log.trace("[Goya] |- Cache |- L2 hit, cache: {}, key: {}", cacheName, key);
                    return value;
                }
            } catch (Exception e) {
                markRemoteUnavailable("get", e);
            }
        }

        return null;
    }

    @Override
    protected <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
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
                markRemoteUnavailable("getOrLoad", e);
            }
        }

        // 3. 加载数据
        value = mappingFunction.apply(key);
        if (value != null) {
            // 4. 写入 L1 和 L2
            localCache.put(cacheName, key, value, getDefaultTtl());

            if (hasRemoteCache()) {
                try {
                    remoteCache.put(cacheName, key, value, getDefaultTtl());
                } catch (Exception e) {
                    markRemoteUnavailable("getOrLoad-put", e);
                }
            }
        }

        return value;
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        Set<K> l1MissKeys = new HashSet<>(keys);

        // 1. 先从 L1 获取
        Map<K, V> l1Result = localCache.get(cacheName, keys);
        result.putAll(l1Result);
        l1MissKeys.removeAll(l1Result.keySet());

        // 2. L1 未命中的从 L2 获取
        if (hasRemoteCache() && !l1MissKeys.isEmpty()) {
            try {
                Map<K, V> l2Result = remoteCache.get(cacheName, l1MissKeys);
                result.putAll(l2Result);

                // 回填 L1
                if (!l2Result.isEmpty()) {
                    for (Map.Entry<K, V> entry : l2Result.entrySet()) {
                        localCache.put(cacheName, entry.getKey(), entry.getValue(), getDefaultTtl());
                    }
                }
            } catch (Exception e) {
                markRemoteUnavailable("getBatch", e);
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
        Set<K> l1MissKeys = new HashSet<>(keys);

        // 1. 先从 L1 获取
        Map<K, V> l1Result = localCache.get(cacheName, keys);
        result.putAll(l1Result);
        l1MissKeys.removeAll(l1Result.keySet());

        Set<K> l2MissKeys = new HashSet<>(l1MissKeys);

        // 2. L1 未命中的从 L2 获取
        if (hasRemoteCache() && !l1MissKeys.isEmpty()) {
            try {
                Map<K, V> l2Result = remoteCache.get(cacheName, l1MissKeys);
                result.putAll(l2Result);
                l2MissKeys.removeAll(l2Result.keySet());

                // 回填 L1
                if (!l2Result.isEmpty()) {
                    for (Map.Entry<K, V> entry : l2Result.entrySet()) {
                        localCache.put(cacheName, entry.getKey(), entry.getValue(), getDefaultTtl());
                    }
                }
            } catch (Exception e) {
                markRemoteUnavailable("getBatchOrLoad", e);
            }
        }

        // 3. 加载缺失的值
        if (!l2MissKeys.isEmpty()) {
            Map<? extends K, ? extends V> loadedValues = mappingFunction.apply(l2MissKeys);
            if (loadedValues != null && !loadedValues.isEmpty()) {
                for (Map.Entry<? extends K, ? extends V> entry : loadedValues.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();
                    if (value != null) {
                        result.put(key, value);

                        // 写入 L1
                        localCache.put(cacheName, key, value, getDefaultTtl());

                        // 写入 L2
                        if (hasRemoteCache()) {
                            try {
                                remoteCache.put(cacheName, key, value, getDefaultTtl());
                            } catch (Exception e) {
                                markRemoteUnavailable("getBatchOrLoad-put", e);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected <K, V> void doPut(String cacheName, K key, V value, Duration duration) {
        // 1. 写入本地
        localCache.put(cacheName, key, value, duration);

        // 2. 写入远程
        if (hasRemoteCache()) {
            try {
                remoteCache.put(cacheName, key, value, duration);
            } catch (Exception e) {
                markRemoteUnavailable("put", e);
            }
        }

        // 3. 发布失效消息
        publishInvalidateMessage(cacheName, key);
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

        // 3. 发布失效消息
        publishInvalidateMessage(cacheName, key);

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

        // 3. 发布失效消息（批量）
        for (K key : keys) {
            publishInvalidateMessage(cacheName, key);
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

    /**
     * 发布失效消息（不抛出异常）
     */
    private void publishInvalidateMessage(String cacheName, Object key) {
        if (publisher != null) {
            try {
                publisher.publish(CacheInvalidateMessage.ofKey(cacheName, key, nodeId));
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

