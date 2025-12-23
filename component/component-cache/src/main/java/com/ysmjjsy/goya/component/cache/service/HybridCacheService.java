package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;
import com.ysmjjsy.goya.component.cache.model.CacheValue;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
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

    /**
     * -- GETTER --
     *  获取本地缓存（供失效监听器使用）
     *
     */
    @Getter
    private final LocalCacheService localCache;
    private final RemoteCacheService remoteCache;
    private final ICacheInvalidatePublisher publisher;
    /**
     * -- GETTER --
     *  获取节点 ID
     *
     */
    @Getter
    private final String nodeId;
    private volatile boolean remoteCacheAvailable = true;
    private volatile long lastRemoteFailureAt = 0L;
    private static final Duration RETRY_AFTER = Duration.ofSeconds(30);

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
        if (remoteCache == null) {
            return false;
        }

        // 如果已标记为不可用，检查是否到了重试时间
        if (!remoteCacheAvailable) {
            long now = System.currentTimeMillis();
            long retryAfterMillis = RETRY_AFTER.toMillis();
            if (now - lastRemoteFailureAt >= retryAfterMillis) {
                // 尝试恢复
                log.info("[Goya] |- Cache |- Attempting to recover remote cache after {} seconds",
                        RETRY_AFTER.getSeconds());
                remoteCacheAvailable = true;
                lastRemoteFailureAt = 0L;
            } else {
                return false;
            }
        }

        return remoteCache.isAvailable();
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        // 1. 查询本地缓存
        final CacheValue<V> l1Value = localCache.get(cacheName, key);

        // 2. 如果没有远程缓存，直接使用 L1
        if (!hasRemoteCache()) {
            if (l1Value != null) {
                log.trace("[Goya] |- Cache |- L1 hit (no L2), cache: {}, key: {}", cacheName, key);
                return l1Value.value();
            }
            return null;
        }

        // 3. 处理远程缓存
        try {
            final CacheValue<V> l2Value = remoteCache.get(cacheName, key);

            // L2 版本更新，回填 L1 并返回
            if (l2Value != null && l2Value.isNewerThan(l1Value)) {
                localCache.put(cacheName, key, l2Value, cacheProperties.caffeineTtl());
                log.trace("[Goya] |- Cache |- L2 hit with newer version, cache: {}, key: {}, version: {}",
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
            return null; // L2 失败且 L1 为空
        }

        // 4. L2 不存在或 L1 版本较新，使用 L1
        if (l1Value != null) {
            log.trace("[Goya] |- Cache |- L1 hit, cache: {}, key: {}, version: {}",
                    cacheName, key, l1Value.version());
            return l1Value.value();
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

    /**
     * 选择较新的 CacheValue（用于版本号比较）
     *
     * @param l1Value L1 缓存值
     * @param l2Value L2 缓存值
     * @param <V>     值类型
     * @return 较新的缓存值，如果都为 null 则返回 null
     */
    private <V> CacheValue<V> selectNewer(CacheValue<V> l1Value, CacheValue<V> l2Value) {
        if (l1Value == null && l2Value == null) {
            return null;
        }
        if (l1Value == null) {
            return l2Value;
        }
        if (l2Value == null) {
            return l1Value;
        }
        return l2Value.isNewerThan(l1Value) ? l2Value : l1Value;
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }

        Map<K, V> result = new HashMap<>();

        // 1. 批量查询 L1（直接调用 doGetBatch，返回 Map<K, CacheValue<V>>）
        Map<K, CacheValue<V>> l1Results = localCache.doGetBatch(cacheName, keys);

        // 2. 批量查询 L2（如果有）
        Map<K, CacheValue<V>> l2Results = safeGetRemoteBatch(cacheName, keys);

        // 3. 合并结果（版本号比较）并批量回填 L1
        Map<K, CacheValue<V>> toBackfill = mergeSelectedAndCollectBackfill(keys, l1Results, l2Results, result);

        // 4. 批量回填 L1（L2 版本更新的项）
        backfillLocalCache(cacheName, toBackfill);

        return result;
    }

    private <K, V> Map<K, CacheValue<V>> safeGetRemoteBatch(String cacheName, Set<? extends K> keys) {
        if (!hasRemoteCache()) {
            return Collections.emptyMap();
        }

        try {
            return remoteCache.doGetBatch(cacheName, keys);
        } catch (Exception e) {
            markRemoteUnavailable("getBatch", e);
            return Collections.emptyMap();
        }
    }

    private <K, V> Map<K, CacheValue<V>> mergeSelectedAndCollectBackfill(
            Set<? extends K> keys,
            Map<K, CacheValue<V>> l1Results,
            Map<K, CacheValue<V>> l2Results,
            Map<K, V> result) {
        Map<K, CacheValue<V>> toBackfill = new HashMap<>();
        for (K key : keys) {
            CacheValue<V> l1Value = MapUtils.isNotEmpty(l1Results) ? l1Results.get(key) : null;
            CacheValue<V> l2Value = MapUtils.isNotEmpty(l2Results) ? l2Results.get(key) : null;

            CacheValue<V> selected = selectNewer(l1Value, l2Value);
            if (selected == null) {
                continue;
            }

            result.put(key, selected.value());

            // 如果 L2 版本更新，需要回填 L1
            if (l2Value != null && l2Value.isNewerThan(l1Value)) {
                toBackfill.put(key, l2Value);
            }
        }
        return toBackfill;
    }

    private <K, V> void backfillLocalCache(String cacheName, Map<K, CacheValue<V>> toBackfill) {
        if (toBackfill.isEmpty()) {
            return;
        }

        for (Map.Entry<K, CacheValue<V>> entry : toBackfill.entrySet()) {
            localCache.put(cacheName, entry.getKey(), entry.getValue(), cacheProperties.caffeineTtl());
        }
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatchOrLoad(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }

        // 1. 批量查询缓存（使用批量 API）
        Map<K, V> cachedResults = doGetBatch(cacheName, keys);
        Map<K, V> result = new HashMap<>(cachedResults);

        // 找出未命中的 key
        Set<K> missKeys = findMissKeys(keys, cachedResults);

        // 2. 加载缺失的值
        if (missKeys.isEmpty()) {
            return result;
        }

        Map<? extends K, ? extends V> loadedValues = mappingFunction.apply(missKeys);
        if (loadedValues == null || loadedValues.isEmpty()) {
            return result;
        }

        // 批量写入缓存（使用 doPut，包含版本号逻辑）
        mergeLoadedValues(cacheName, loadedValues, result);

        return result;
    }

    private <K> Set<K> findMissKeys(Set<? extends K> keys, Map<K, ?> cachedResults) {
        Set<K> missKeys = new HashSet<>();
        for (K key : keys) {
            if (!cachedResults.containsKey(key)) {
                missKeys.add(key);
            }
        }
        return missKeys;
    }

    private <K, V> void mergeLoadedValues(String cacheName, Map<? extends K, ? extends V> loadedValues, Map<K, V> result) {
        for (Map.Entry<? extends K, ? extends V> entry : loadedValues.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (value != null) {
                result.put(key, value);
                doPut(cacheName, key, value, getDefaultTtl());
            }
        }
    }

    @Override
    protected <K, V> void doPut(String cacheName, K key, V value, Duration duration) {
        // 包装为 CacheValue（带版本号）
        CacheValue<V> wrappedValue = CacheValue.of(value);

        // 判断是否永不过期
        boolean eternal = CacheProperties.isEternal(duration);

        // 1. 写入本地（存储 CacheValue）
        // 永不过期的缓存也使用 ETERNAL，否则使用 caffeineTtl
        Duration l1Ttl = eternal ? CacheProperties.ETERNAL : cacheProperties.caffeineTtl();
        localCache.put(cacheName, key, wrappedValue, l1Ttl);
        log.trace("[Goya] |- Cache |- L1 put, cache: {}, key: {}, version: {} (ttl: {}, eternal: {})",
                cacheName, key, wrappedValue.version(), l1Ttl, eternal);

        // 2. 写入远程（存储 CacheValue）
        if (hasRemoteCache()) {
            try {
                remoteCache.put(cacheName, key, wrappedValue, duration);
                log.trace("[Goya] |- Cache |- L2 put, cache: {}, key: {}, version: {} (eternal: {})",
                        cacheName, key, wrappedValue.version(), eternal);
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
        // 1. 先查询 L1（返回 CacheValue<V>）
        CacheValue<V> l1Value = localCache.get(cacheName, key);
        if (l1Value != null) {
            return l1Value.value();
        }

        // 2. 查询 L2（返回 CacheValue<V>）
        if (hasRemoteCache()) {
            try {
                CacheValue<V> l2Value = remoteCache.get(cacheName, key);
                if (l2Value != null) {
                    // 回填 L1（使用 CacheValue，固定使用 caffeineTtl）
                    localCache.put(cacheName, key, l2Value, cacheProperties.caffeineTtl());
                    return l2Value.value();
                }
            } catch (Exception e) {
                markRemoteUnavailable("computeIfAbsent", e);
            }
        }

        // 3. 加载数据
        V value = loader.apply(key);
        if (value != null) {
            // 使用 doPut 写入缓存（自动包装为 CacheValue）
            doPut(cacheName, key, value, getDefaultTtl());
        }

        return value;
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
     * 标记远程缓存不可用（并记录时间用于恢复探测）
     */
    private void markRemoteUnavailable(String operation, Exception e) {
        remoteCacheAvailable = false;
        lastRemoteFailureAt = System.currentTimeMillis();
        log.warn("[Goya] |- Cache |- Remote cache unavailable during [{}], degraded to local only. " +
                "Will retry after {} seconds. Error: {}",
                operation, RETRY_AFTER.getSeconds(), e.getMessage());
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

