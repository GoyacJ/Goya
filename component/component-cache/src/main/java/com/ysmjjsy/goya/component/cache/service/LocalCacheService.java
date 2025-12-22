package com.ysmjjsy.goya.component.cache.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

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

    public LocalCacheService(CacheProperties cacheProperties) {
        super(cacheProperties);
    }

    /**
     * 获取或创建 Caffeine Cache 实例
     *
     * @param cacheName 缓存名称
     * @param duration  过期时间
     * @return Caffeine Cache 实例
     */
    private Cache<Object, Object> getOrCreateCache(String cacheName, Duration duration) {
        // 使用带前缀的 cacheName
        String prefixedName = buildCachePrefix(cacheName);
        return cacheMap.computeIfAbsent(prefixedName, name -> {
            // 使用配置的 Caffeine 参数
            Integer maxSize = cacheProperties.caffeineMaxSize() != null
                    ? cacheProperties.caffeineMaxSize()
                    : 10000;

            Caffeine<Object, Object> builder = Caffeine.newBuilder()
                    .expireAfterWrite(duration)
                    .maximumSize(maxSize);

            if (Boolean.TRUE.equals(cacheProperties.enableStats())) {
                builder.recordStats();
            }

            Cache<Object, Object> cache = builder.build();
            log.debug("[Goya] |- Cache |- Local cache [{}] created with ttl [{}], maxSize [{}]",
                    name, duration, maxSize);
            return cache;
        });
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
                        @SuppressWarnings("unchecked")
                        V typedValue = (V) value;
                        result.put(key, typedValue);
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
            Cache<Object, Object> cache = getOrCreateCache(cacheName, duration);
            cache.put(key, value);
            log.trace("[Goya] |- Cache |- Local cache [{}] put key [{}]", cacheName, key);
        } catch (Exception e) {
            handleException("put", cacheName, e);
        }
    }

    @Override
    public <K> Boolean doRemove(String cacheName, K key) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
            cache.invalidateAll(keys);
            log.trace("[Goya] |- Cache |- Local cache [{}] remove {} keys", cacheName, keys.size());
        } catch (Exception e) {
            handleException("removeBatch", cacheName, e);
        }
    }

    @Override
    protected <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        try {
            Cache<Object, Object> cache = getOrCreateCache(cacheName, getDefaultTtl());
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
    protected <K> void doTryLock(String cacheName, K key, Duration expire) {
        String lockKey = buildLockKey(cacheName, key);
        Lock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());

        // 本地锁不支持超时，这里只是记录日志
        log.warn("[Goya] |- Cache |- Local lock does not support timeout, lock key [{}], expire [{}]",
                lockKey, expire);

        lock.lock();
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
}

