package com.ysmjjsy.goya.starter.redis.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.service.AbstractCacheService;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>基于 Redis (Redisson) 的分布式缓存服务实现</p>
 * <p>适用于分布式环境，支持缓存共享和分布式锁</p>
 * <p>特点：</p>
 * <ul>
 *     <li>分布式缓存，多实例间共享缓存数据</li>
 *     <li>支持 TTL 和 maxIdleTime</li>
 *     <li>支持分布式锁</li>
 *     <li>高可用性和持久化支持</li>
 * </ul>
 *
 * @author goya
 * @see AbstractCacheService
 * @see RedissonClient
 * @see <a href="https://github.com/redisson/redisson">Redisson Github</a>
 * @since 2025/12/22 00:10
 */
@Slf4j
public class RedisCacheService extends AbstractCacheService {

    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    public RedisCacheService(CacheProperties cacheProperties,
                                RedisProperties redisProperties,
                                RedissonClient redissonClient) {
        super(cacheProperties);
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
    }

    /**
     * 获取 RMapCache 实例
     * 使用 cacheName 作为 Redis Map 的 key
     *
     * @param cacheName 缓存名称
     * @param <K>       键类型
     * @param <V>       值类型
     * @return RMapCache 实例
     */
    private <K, V> RMapCache<K, V> getMapCache(String cacheName) {
        return redissonClient.getMapCache(cacheName);
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁键
     * @return RLock 实例
     */
    private RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            V value = mapCache.get(key);
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] get key [{}]", cacheName, key);
            return value;
        } catch (Exception e) {
            handleException("get", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            V value = mapCache.get(key);

            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    Duration ttl = getDefaultTtl();
                    mapCache.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
                    log.trace("[Goya] |- starter [redis] |- redis cache [{}] load and put key [{}]",
                            cacheName, key);
                }
            }

            return value;
        } catch (Exception e) {
            handleException("getOrLoad", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            Map<K, V> result = new HashMap<>();

            for (K key : keys) {
                V value = mapCache.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }

            log.trace("[Goya] |- starter [redis] |- redis cache [{}] get batch keys [{}]",
                    cacheName, keys.size());
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
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            Map<K, V> result = new HashMap<>();
            Set<K> missingKeys = keys.stream()
                    .filter(key -> !mapCache.containsKey(key))
                    .collect(Collectors.toSet());

            // 获取已存在的缓存值
            for (K key : keys) {
                V value = mapCache.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }

            // 加载缺失的值
            if (!missingKeys.isEmpty()) {
                Map<? extends K, ? extends V> loadedValues = mappingFunction.apply(missingKeys);
                if (loadedValues != null && !loadedValues.isEmpty()) {
                    Duration ttl = getDefaultTtl();
                    long ttlMillis = ttl.toMillis();

                    loadedValues.forEach((key, value) -> {
                        if (value != null) {
                            mapCache.put(key, value, ttlMillis, TimeUnit.MILLISECONDS);
                            @SuppressWarnings("unchecked")
                            V typedValue = (V) value;
                            result.put(key, typedValue);
                        }
                    });

                    log.trace("[Goya] |- starter [redis] |- redis cache [{}] load and put batch keys [{}]",
                            cacheName, loadedValues.size());
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
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            long ttlMillis = duration.toMillis();
            mapCache.put(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] put key [{}] with ttl [{}]",
                    cacheName, key, duration);
        } catch (Exception e) {
            handleException("put", cacheName, e);
        }
    }

    @Override
    protected <K> Boolean doRemove(String cacheName, K key) {
        try {
            RMapCache<K, Object> mapCache = getMapCache(cacheName);
            Object removed = mapCache.remove(key);
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] remove key [{}]", cacheName, key);
            return removed != null;
        } catch (Exception e) {
            handleException("remove", cacheName, e);
            return false;
        }
    }

    @Override
    protected <K> void doRemoveBatch(String cacheName, Set<? extends K> keys) {
        try {
            RMapCache<K, Object> mapCache = getMapCache(cacheName);
            for (K key : keys) {
                mapCache.remove(key);
            }
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] remove batch keys [{}]",
                    cacheName, keys.size());
        } catch (Exception e) {
            handleException("removeBatch", cacheName, e);
        }
    }

    @Override
    protected <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            V value = mapCache.get(key);

            if (value == null) {
                value = loader.apply(key);
                if (value != null) {
                    Duration ttl = getDefaultTtl();
                    mapCache.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
                    log.trace("[Goya] |- starter [redis] |- redis cache [{}] compute and put key [{}]",
                            cacheName, key);
                }
            }

            return value;
        } catch (Exception e) {
            handleException("computeIfAbsent", cacheName, e);
            return null;
        }
    }

    @Override
    protected <K> void doTryLock(String cacheName, K key, Duration expire) {
        String lockKey = buildCacheKey(cacheName, key);
        RLock lock = getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(0, expire.toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                log.trace("[Goya] |- starter [redis] |- redis lock acquired [{}] with expire [{}]",
                        lockKey, expire);
            } else {
                log.warn("[Goya] |- starter [redis] |- redis lock failed to acquire [{}]", lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- redis lock interrupted [{}]", lockKey, e);
        }
    }

    @Override
    protected <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        String lockKey = buildCacheKey(cacheName, key);
        RLock lock = getLock(lockKey);

        Duration waitTime = redisProperties.lockWaitTime() != null
                ? redisProperties.lockWaitTime()
                : Duration.ofSeconds(10);

        try {
            boolean acquired = lock.tryLock(waitTime.toMillis(), expire.toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                try {
                    action.run();
                    log.trace("[Goya] |- starter [redis] |- redis lock and run success [{}]", lockKey);
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.trace("[Goya] |- starter [redis] |- redis lock released [{}]", lockKey);
                    }
                }
            } else {
                log.warn("[Goya] |- starter [redis] |- redis lock failed to acquire [{}] within wait time [{}]",
                        lockKey, waitTime);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- redis lock interrupted [{}]", lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- redis lock and run failed [{}]", lockKey, e);
            return false;
        }
    }

    /**
     * 清空指定缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        try {
            RMapCache<Object, Object> mapCache = getMapCache(cacheName);
            mapCache.clear();
            log.info("[Goya] |- starter [redis] |- redis cache [{}] cleared", cacheName);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- redis cache [{}] clear failed", cacheName, e);
        }
    }

    /**
     * 获取缓存大小
     *
     * @param cacheName 缓存名称
     * @return 缓存条目数量
     */
    public int size(String cacheName) {
        try {
            RMapCache<Object, Object> mapCache = getMapCache(cacheName);
            return mapCache.size();
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- redis cache [{}] size query failed", cacheName, e);
            return 0;
        }
    }
}

