package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.metrics.DefaultCacheMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
    public <T> T get(String cacheName, Object key, Class<T> type) {
        validateCacheNameAndKey(cacheName, key);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            return null;
        }

        Object value = wrapper.get();
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        return null;
    }

    @Override
    public <T> T get(String cacheName, Object key, Callable<T> valueLoader) {
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
    public void put(String cacheName, Object key, Object value) {
        validateCacheNameAndKey(cacheName, key);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
            return;
        }

        cache.put(key, value);
    }

    @Override
    public void evict(String cacheName, Object key) {
        validateCacheNameAndKey(cacheName, key);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache not found: cacheName={}", cacheName);
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
    public boolean exists(String cacheName, Object key) {
        validateCacheNameAndKey(cacheName, key);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null;
    }

    @Override
    public <T> Map<Object, T> batchGet(String cacheName, Set<Object> keys, Class<T> type) {
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

        // 批量获取（当前使用循环，未来可优化为真正的批量 API）
        Map<Object, T> result = new HashMap<>();
        for (Object key : keys) {
            if (key == null) {
                continue;
            }
            T value = get(cacheName, key, type);
            if (value != null) {
                result.put(key, value);
            }
        }

        return result;
    }

    @Override
    public void batchPut(String cacheName, Map<Object, Object> entries) {
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

        // 批量写入（当前使用循环，未来可优化为真正的批量 API）
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            put(cacheName, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void batchEvict(String cacheName, Set<Object> keys) {
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

        // 批量失效（当前使用循环，未来可优化为真正的批量 API）
        for (Object key : keys) {
            if (key == null) {
                continue;
            }
            evict(cacheName, key);
        }
    }

    @Override
    public void warmUp(String cacheName, Function<Object, Object> loader, Set<Object> keys) {
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
                        Object value = loader.apply(key);
                        if (value != null) {
                            cache.put(key, value);
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

        return new CacheStatistics(l1Hits, l2Hits, misses, bloomFilterFalsePositives, refillSuccessRate);
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

