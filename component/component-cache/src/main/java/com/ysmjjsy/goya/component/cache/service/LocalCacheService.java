package com.ysmjjsy.goya.component.cache.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>本地缓存服务（基于 Caffeine）</p>
 * <p>特点：</p>
 * <ul>
 *     <li>高性能，低延迟</li>
 *     <li>仅单机有效，不跨节点共享</li>
 *     <li>适合高频访问、不需要共享的数据</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>高频访问的数据</li>
 *     <li>不需要跨节点共享</li>
 *     <li>对一致性要求不高</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class HotDataService {
 *     private final LocalCacheService localCache;
 *
 *     public HotDataService(CacheServiceFactory factory) {
 *         // 高频访问数据，只用本地缓存
 *         this.localCache = factory.createLocal();
 *     }
 *
 *     public String getHotData(String key) {
 *         return localCache.get("hotData", key, this::loadHotData);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see AbstractCacheService
 * @see <a href="https://github.com/ben-manes/caffeine">Caffeine Github</a>
 */
@Slf4j
public class LocalCacheService extends AbstractCacheService {

    /**
     * 缓存容器映射
     * Key: cacheName, Value: Caffeine Cache 实例
     */
    private final Map<String, Cache<Object, Object>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 本地锁映射
     * Key: lockKey, Value: ReentrantLock 实例
     */
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    /**
     * 布隆过滤器映射（防止缓存穿透）
     * Key: cacheName, Value: BloomFilter 实例
     */
    private final Map<String, BloomFilter<byte[]>> bloomFilters = new ConcurrentHashMap<>();

    public LocalCacheService(CacheProperties cacheProperties) {
        super(cacheProperties);
    }

    /**
     * 获取或创建 Caffeine Cache 实例
     * <p>注意：L1 TTL 固定使用 {@link CacheProperties#caffeineTtl()}</p>
     *
     * @param cacheName 缓存名称
     * @return Caffeine Cache 实例
     */
    private Cache<Object, Object> getOrCreateCache(String cacheName) {
        return getOrCreateCache(cacheName, false);
    }

    /**
     * 获取或创建 Caffeine Cache 实例
     *
     * @param cacheName 缓存名称
     * @param eternal   是否永不过期
     * @return Caffeine Cache 实例
     */
    private Cache<Object, Object> getOrCreateCache(String cacheName, boolean eternal) {
        // 使用带前缀的 cacheName，永不过期的缓存使用单独的实例
        String prefixedName = buildCachePrefix(cacheName);
        String cacheKey = eternal ? prefixedName + ":eternal" : prefixedName;

        return cacheMap.computeIfAbsent(cacheKey, name -> {
            // 使用配置的 Caffeine 参数
            int maxSize = cacheProperties.caffeineMaxSize() != null
                    ? cacheProperties.caffeineMaxSize()
                    : 10000;

            Caffeine<Object, Object> builder = Caffeine.newBuilder()
                    .maximumSize(maxSize);

            // 永不过期的缓存不设置 expireAfterWrite
            if (!eternal) {
                // L1 固定使用 caffeineTtl
                Duration l1Ttl = cacheProperties.caffeineTtl();
                builder.expireAfterWrite(l1Ttl);
            }

            if (Boolean.TRUE.equals(cacheProperties.enableStats())) {
                builder.recordStats();
            }

            Cache<Object, Object> cache = builder.build();
            log.debug("[Goya] |- Cache |- Local cache [{}] created with eternal={}, maxSize [{}]",
                    name, eternal, maxSize);
            return cache;
        });
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            @SuppressWarnings("unchecked")
            V value = (V) cache.getIfPresent(key);
            return value;
        } catch (Exception e) {
            handleException("get", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            @SuppressWarnings("unchecked")
            V value = (V) cache.get(key, k -> {
                @SuppressWarnings("unchecked")
                K typedKey = (K) k;
                return mappingFunction.apply(typedKey);
            });
            return value;
        } catch (Exception e) {
            handleException("getOrLoad", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            Map<K, V> result = new HashMap<>();

            for (K key : keys) {
                @SuppressWarnings("unchecked")
                V value = (V) cache.getIfPresent(key);
                if (value != null) {
                    result.put(key, value);
                }
            }

            return result;
        } catch (Exception e) {
            handleException("getBatch", cacheName, e);
            return new HashMap<>();
        }
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatchOrLoad(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            Map<K, V> result = new HashMap<>();
            Set<K> missingKeys = keys.stream()
                    .filter(key -> !cache.asMap().containsKey(key))
                    .collect(Collectors.toSet());

            // 获取已存在的缓存值
            for (K key : keys) {
                @SuppressWarnings("unchecked")
                V value = (V) cache.getIfPresent(key);
                if (value != null) {
                    result.put(key, value);
                }
            }

            // 加载缺失的值
            if (!missingKeys.isEmpty()) {
                Map<? extends K, ? extends V> loadedValues = mappingFunction.apply(missingKeys);
                if (loadedValues != null) {
                    loadedValues.forEach((key, value) -> {
                        cache.put(key, value);
                        result.put(key, value);
                    });
                }
            }

            return result;
        } catch (Exception e) {
            handleException("getBatchOrLoad", cacheName, e);
            return new HashMap<>();
        }
    }

    @Override
    protected <K, V> void doPut(String cacheName, K key, V value, Duration duration) {
        try {
            // 判断是否永不过期
            boolean eternal = CacheProperties.isEternal(duration);
            
            // 永不过期的缓存使用单独的 Cache 实例
            Cache<Object, Object> cache = getOrCreateCache(cacheName, eternal);
            cache.put(key, value);

            // 自动添加到布隆过滤器
            addToBloomFilter(cacheName, key);

            log.trace("[Goya] |- Cache |- Local cache [{}] put key [{}], eternal={}", 
                    cacheName, key, eternal);
        } catch (Exception e) {
            handleException("put", cacheName, e);
        }
    }

    @Override
    public <K> Boolean doRemove(String cacheName, K key) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            boolean exists = cache.getIfPresent(key) != null;
            cache.invalidate(key);
            log.trace("[Goya] |- Cache |- Local cache [{}] remove key [{}]", cacheName, key);
            return exists;
        } catch (Exception e) {
            handleException("remove", cacheName, e);
            return false;
        }
    }

    @Override
    protected <K> void doRemoveBatch(String cacheName, Set<? extends K> keys) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            cache.invalidateAll(keys);
            log.trace("[Goya] |- Cache |- Local cache [{}] remove {} keys", cacheName, keys.size());
        } catch (Exception e) {
            handleException("removeBatch", cacheName, e);
        }
    }

    @Override
    protected <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName);
            @SuppressWarnings("unchecked")
            V value = (V) cache.get(key, k -> {
                @SuppressWarnings("unchecked")
                K typedKey = (K) k;
                return loader.apply(typedKey);
            });
            return value;
        } catch (Exception e) {
            handleException("computeIfAbsent", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        String lockKey = buildLockKey(cacheName, key);
        Lock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());

        try {
            // 本地锁不支持超时，这里只是记录日志
            log.warn("[Goya] |- Cache |- Local lock does not support timeout, lock key [{}], expire [{}]",
                    lockKey, expire);

            lock.lock();
            action.run();
            return true;
        } catch (Exception e) {
            log.error("[Goya] |- Cache |- Local lock and run failed, lock key [{}]", lockKey, e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        cacheMap.values().forEach(Cache::invalidateAll);
        log.info("[Goya] |- Cache |- All local caches cleared");
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        String prefixedName = buildCachePrefix(cacheName);
        Cache<Object, Object> cache = cacheMap.get(prefixedName);
        if (cache != null) {
            cache.invalidateAll();
            log.info("[Goya] |- Cache |- Local cache [{}] cleared", prefixedName);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 统计信息，如果缓存不存在或未启用统计则返回 null
     */
    public String getStats(String cacheName) {
        Cache<Object, Object> cache = cacheMap.get(cacheName);
        if (cache != null && Boolean.TRUE.equals(cacheProperties.enableStats())) {
            return cache.stats().toString();
        }
        return null;
    }

    // ==================== 布隆过滤器实现（防止缓存穿透）====================

    /**
     * 获取或创建布隆过滤器
     *
     * @param cacheName 缓存名称
     * @return BloomFilter 实例
     */
    private BloomFilter<byte[]> getOrCreateBloomFilter(String cacheName) {
        String prefixedName = buildCachePrefix(cacheName);
        return bloomFilters.computeIfAbsent(prefixedName, name -> {
            // 使用配置的预期插入数，如果没有配置则使用 caffeineMaxSize
            long expectedInsertions = cacheProperties.bloomFilterExpectedInsertions() != null
                    ? cacheProperties.bloomFilterExpectedInsertions()
                    : cacheProperties.caffeineMaxSize();
            
            // 使用配置的误判率，如果没有配置则使用默认值 0.01
            double fpp = cacheProperties.bloomFilterFpp() != null
                    ? cacheProperties.bloomFilterFpp()
                    : 0.01;

            BloomFilter<byte[]> filter = BloomFilter.create(
                    Funnels.byteArrayFunnel(),
                    expectedInsertions,
                    fpp
            );

            log.debug("[Goya] |- Cache |- Bloom filter created for cache [{}], expected {} insertions, fpp {}",
                    name, expectedInsertions, fpp);
            return filter;
        });
    }

    @Override
    public <K> boolean mightContain(String cacheName, K key) {
        String prefixedName = buildCachePrefix(cacheName);
        BloomFilter<byte[]> filter = bloomFilters.get(prefixedName);
        if (filter == null) {
            // 过滤器不存在，假设可能存在（保守策略）
            return true;
        }

        byte[] keyBytes = serializeKey(key);
        return filter.mightContain(keyBytes);
    }

    @Override
    public <K> void addToBloomFilter(String cacheName, K key) {
        try {
            BloomFilter<byte[]> filter = getOrCreateBloomFilter(cacheName);
            byte[] keyBytes = serializeKey(key);
            filter.put(keyBytes);
            log.trace("[Goya] |- Cache |- Key [{}] added to bloom filter for cache [{}]", key, cacheName);
        } catch (Exception e) {
            log.warn("[Goya] |- Cache |- Failed to add key to bloom filter: {}", e.getMessage());
        }
    }

    /**
     * 序列化 key（简单实现）
     *
     * @param key 缓存键
     * @return 序列化后的字节数组
     */
    private byte[] serializeKey(Object key) {
        return String.valueOf(key).getBytes(StandardCharsets.UTF_8);
    }
}

