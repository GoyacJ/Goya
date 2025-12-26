package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.metrics.DefaultCacheMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存服务实现
 *
 * <p>实现 {@link ICacheService} 接口，提供便捷的缓存操作方法。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>封装 Spring Cache API，提供类型安全的操作方法</li>
 *   <li>实现批量操作，提升性能</li>
 *   <li>实现高级功能（缓存预热、统计信息等）</li>
 * </ul>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>批量操作使用循环调用单个操作（未来可优化为真正的批量 API）</li>
 *   <li>缓存预热支持并发加载</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 15:05
 */
@Slf4j
public class DefaultCacheService implements ICacheService {

    /**
     * Spring Cache Manager
     */
    private final CacheManager cacheManager;

    /**
     * 监控指标（可选）
     */
    private final CacheMetrics metrics;

    /**
     * 构造函数
     *
     * @param cacheManager Spring Cache Manager（实际是 GoyaCacheManager）
     * @param metrics 监控指标（可选，如果为 null 则不记录统计信息）
     * @throws IllegalArgumentException 如果 cacheManager 为 null
     */
    public DefaultCacheService(CacheManager cacheManager, CacheMetrics metrics) {
        if (cacheManager == null) {
            throw new IllegalArgumentException("CacheManager cannot be null");
        }
        this.cacheManager = cacheManager;
        this.metrics = metrics;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V get(String cacheName, K key) {
        validateCacheNameAndKey(cacheName, key);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return null;
        }
        if (cache instanceof GoyaCache) {
            GoyaCache<K, V> goyaCache = (GoyaCache<K, V>) cache;
            return goyaCache.getTypedValue(key);
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (value == null) {
            return null;
        }
        try {
            return (V) value;
        } catch (ClassCastException e) {
            log.warn("Value type mismatch: cacheName={}, key={}, expected V, got {}",
                    cacheName, key, value.getClass().getName());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V get(String cacheName, K key, Callable<V> valueLoader) {
        validateCacheNameAndKey(cacheName, key);
        if (valueLoader == null) {
            throw new IllegalArgumentException("ValueLoader cannot be null");
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new CacheException("ValueLoader failed", e);
            }
        }
        try {
            return cache.get(key, valueLoader);
        } catch (Cache.ValueRetrievalException e) {
            throw new CacheException("Failed to get or load cache value", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> void put(String cacheName, K key, V value) {
        validateCacheNameAndKey(cacheName, key);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }
        if (cache instanceof GoyaCache) {
            GoyaCache<K, V> goyaCache = (GoyaCache<K, V>) cache;
            goyaCache.putTyped(key, value);
            return;
        }
        cache.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> void put(String cacheName, K key, V value, Duration ttl) {
        validateCacheNameAndKey(cacheName, key);
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }
        if (cache instanceof GoyaCache) {
            GoyaCache<K, V> goyaCache = (GoyaCache<K, V>) cache;
            goyaCache.putTyped(key, value, ttl);
            return;
        }
        // 对于非 GoyaCache 实现，降级到标准 Spring Cache API
        // Spring Cache 的 Cache 接口没有带 TTL 的 put 方法，所以只能使用默认 TTL
        log.warn("Cache implementation does not support custom TTL, using default TTL: cacheName={}", cacheName);
        cache.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> void evict(String cacheName, K key) {
        validateCacheNameAndKey(cacheName, key);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }
        if (cache instanceof GoyaCache) {
            GoyaCache<K, ?> goyaCache = (GoyaCache<K, ?>) cache;
            goyaCache.evictTyped(key);
            return;
        }
        cache.evict(key);
    }

    @Override
    public void clear(String cacheName) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }

        cache.clear();
    }

    @Override
    public <K> boolean exists(String cacheName, K key) {
        validateCacheNameAndKey(cacheName, key);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> batchGet(String cacheName, Set<K> keys) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return Collections.emptyMap();
        }

        // 如果缓存是 GoyaCache，使用批量 API 提升性能
        if (cache instanceof GoyaCache) {
            GoyaCache<K, V> goyaCache = (GoyaCache<K, V>) cache;
            return goyaCache.batchGetTyped(keys);
        }

        // 降级到循环调用单个操作（非 GoyaCache 实现）
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            if (key == null) {
                continue;
            }
            V value = get(cacheName, key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> void batchPut(String cacheName, Map<K, V> entries) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (entries == null) {
            throw new IllegalArgumentException("Entries cannot be null");
        }
        if (entries.isEmpty()) {
            return;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }

        // 如果缓存是 GoyaCache，使用批量 API 提升性能
        if (cache instanceof GoyaCache) {
            GoyaCache<K, V> goyaCache = (GoyaCache<K, V>) cache;
            // 使用缓存的默认 TTL（从配置获取）
            goyaCache.batchPutTyped(entries);
            return;
        }

        // 降级到循环调用单个操作（非 GoyaCache 实现）
        for (Map.Entry<K, V> entry : entries.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            put(cacheName, entry.getKey(), entry.getValue());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> void batchEvict(String cacheName, Set<K> keys) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.isEmpty()) {
            return;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }

        // 如果缓存是 GoyaCache，使用批量 API 提升性能
        if (cache instanceof GoyaCache) {
            GoyaCache<K, ?> goyaCache = (GoyaCache<K, ?>) cache;
            goyaCache.batchEvictTyped(keys);
            return;
        }

        // 降级到循环调用单个操作（非 GoyaCache 实现）
        for (K key : keys) {
            if (key == null) {
                continue;
            }
            evict(cacheName, key);
        }
    }

    @Override
    public <K, V> void warmUp(String cacheName, Function<K, V> loader, Set<K> keys) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.isEmpty()) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }
        // 并发预热
        List<CompletableFuture<Void>> futures = keys.stream()
                .filter(Objects::nonNull)
                .map(key -> CompletableFuture.runAsync(() -> {
                    try {
                        V value = loader.apply(key);
                        if (value != null) {
                            put(cacheName, key, value);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to warm up cache: cacheName={}, key={}", cacheName, key, e);
                    }
                }))
                .toList();
        // 等待所有预热任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (log.isDebugEnabled()) {
            log.debug("Cache warm up completed: cacheName={}, keys={}", cacheName, keys.size());
        }
    }

    @Override
    public CacheStatistics getStatistics(String cacheName) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }

        if (metrics == null) {
            // 监控未启用，返回空统计
            return new CacheStatistics(0, 0, 0, 0, 1.0);
        }

        DefaultCacheMetrics defaultMetrics = (DefaultCacheMetrics) metrics;
        long l1Hits = defaultMetrics.getL1Hits(cacheName);
        long l2Hits = defaultMetrics.getL2Hits(cacheName);
        long misses = defaultMetrics.getMisses(cacheName);
        long bloomFilterFalsePositives = defaultMetrics.getBloomFilterFalsePositives(cacheName);
        double refillSuccessRate = defaultMetrics.getRefillSuccessRate(cacheName);

        // 企业级指标
        double l1AvgLatencyMs = defaultMetrics.getL1AvgLatencyMs(cacheName);
        double l2AvgLatencyMs = defaultMetrics.getL2AvgLatencyMs(cacheName);
        double l1P99LatencyMs = defaultMetrics.getL1P99LatencyMs(cacheName);
        double l2P99LatencyMs = defaultMetrics.getL2P99LatencyMs(cacheName);
        long sourceLoadCount = defaultMetrics.getSourceLoadCount(cacheName);
        double sourceLoadAvgLatencyMs = defaultMetrics.getSourceLoadAvgLatencyMs(cacheName);

        return new CacheStatistics(l1Hits, l2Hits, misses, bloomFilterFalsePositives, refillSuccessRate,
                l1AvgLatencyMs, l2AvgLatencyMs, l1P99LatencyMs, l2P99LatencyMs,
                sourceLoadCount, sourceLoadAvgLatencyMs);
    }

    @Override
    public List<HotKey> getHotKeys(String cacheName, int topN) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (topN <= 0) {
            throw new IllegalArgumentException("TopN must be positive, got: " + topN);
        }

        if (metrics == null) {
            return Collections.emptyList();
        }

        DefaultCacheMetrics defaultMetrics = (DefaultCacheMetrics) metrics;
        List<DefaultCacheMetrics.HotKey> hotKeys = defaultMetrics.getHotKeys(cacheName, topN);

        // 转换为 ICacheService.HotKey
        return hotKeys.stream()
                .map(hk -> new HotKey(hk.getKey(), hk.getAccessCount()))
                .collect(Collectors.toList());
    }

    /**
     * 验证 cacheName 和 key
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @throws IllegalArgumentException 如果参数为 null
     */
    private void validateCacheNameAndKey(String cacheName, Object key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }
}

