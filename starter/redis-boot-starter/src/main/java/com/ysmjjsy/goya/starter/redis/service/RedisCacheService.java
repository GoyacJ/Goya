package com.ysmjjsy.goya.starter.redis.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.service.IL2Cache;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>基于 Redis (Redisson) 的 L2 分布式缓存服务实现</p>
 * <p>作为多级缓存架构中的 L2 层，实现 IL2Cache SPI</p>
 * <p>特点：</p>
 * <ul>
 *     <li>分布式缓存，多实例间共享缓存数据</li>
 *     <li>支持 TTL 和 maxIdleTime</li>
 *     <li>支持分布式锁</li>
 *     <li>高可用性和持久化支持</li>
 * </ul>
 *
 * @author goya
 * @see IL2Cache
 * @see RedissonClient
 * @see <a href="https://github.com/redisson/redisson">Redisson Github</a>
 * @since 2025/12/22
 */
@Slf4j
public class RedisCacheService implements IL2Cache {

    private static final String CACHE_TYPE = "redis";

    private final CacheProperties cacheProperties;
    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    public RedisCacheService(CacheProperties cacheProperties,
                             RedisProperties redisProperties,
                             RedissonClient redissonClient) {
        this.cacheProperties = cacheProperties;
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
        log.info("[Goya] |- starter [redis] |- RedisCacheService initialized as IL2Cache SPI");
    }

    @Override
    public String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 检查 Redis 连接是否可用
            return redissonClient != null && !redissonClient.isShutdown();
        } catch (Exception e) {
            log.warn("[Goya] |- starter [redis] |- Redis availability check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void clearAll() {
        try {
            // 注意：此操作会清空所有缓存，使用需谨慎
            log.warn("[Goya] |- starter [redis] |- clearAll is not recommended in production");
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- clearAll failed", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建缓存前缀
     * <p>格式: {cachePrefix}{cacheName}:</p>
     * <p>例如: cache:goya:users:</p>
     *
     * @param cacheName 缓存名称
     * @return 完整缓存前缀
     */
    private String buildCachePrefix(String cacheName) {
        return cacheProperties.buildCachePrefix(cacheName);
    }

    /**
     * 构建锁键
     * <p>格式: {cachePrefix}lock:{cacheName}:{key}</p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 锁键
     */
    private String buildLockKey(String cacheName, Object key) {
        return cacheProperties.buildLockKey(cacheName, key);
    }

    /**
     * 获取默认 TTL
     */
    private Duration getDefaultTtl() {
        return cacheProperties.defaultTtl() != null
                ? cacheProperties.defaultTtl()
                : Duration.ofHours(1);
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
        String prefixedName = buildCachePrefix(cacheName);
        return redissonClient.getMapCache(prefixedName);
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

    /**
     * 处理异常
     */
    private void handleException(String operation, String cacheName, Exception e) {
        log.error("[Goya] |- starter [redis] |- redis cache [{}] {} failed", cacheName, operation, e);
    }

    // ==================== IL2Cache 接口实现 ====================

    @Override
    public <K, V> V get(String cacheName, K key) {
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
    public <K, V> V get(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
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
    public <K, V> Map<K, @NonNull V> get(String cacheName, Set<? extends K> keys) {
        if (keys.isEmpty()) {
            return Map.of();
        }

        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);

            // 类型转换：Set<? extends K> -> Set<K>（用于 Redisson API）
            @SuppressWarnings("unchecked")
            Set<K> keysSet = new HashSet<>(keys);

            // 使用 Redisson 的批量操作（底层使用 MGET，一次网络调用）
            Map<K, V> allValues = mapCache.getAll(keysSet);

            // 过滤 null 值
            Map<K, V> result = allValues.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.trace("[Goya] |- starter [redis] |- redis batch get {} keys from cache [{}], found {}",
                    keys.size(), cacheName, result.size());

            return result;
        } catch (Exception e) {
            handleException("getBatch", cacheName, e);
            return new HashMap<>();
        }
    }

    @Override
    public <K, V> Map<K, @NonNull V> get(
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
    public <K, V> void put(String cacheName, K key, V value, Duration duration) {
        try {
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            long ttlMillis = duration.toMillis();
            mapCache.put(key, value, ttlMillis, TimeUnit.MILLISECONDS);

            // 自动添加到布隆过滤器
            addToBloomFilter(cacheName, key);

            log.trace("[Goya] |- starter [redis] |- redis cache [{}] put key [{}] with ttl [{}]",
                    cacheName, key, duration);
        } catch (Exception e) {
            handleException("put", cacheName, e);
        }
    }

    @Override
    public <K> Boolean remove(String cacheName, K key) {
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
    public <K> void remove(String cacheName, Set<? extends K> keys) {
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
    public <K, V> V computeIfAbsent(String cacheName, K key, Function<K, V> loader) {
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
    public <K> boolean lockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        String lockKey = buildLockKey(cacheName, key);
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
            String prefixedName = buildCachePrefix(cacheName);
            RMapCache<Object, Object> mapCache = redissonClient.getMapCache(prefixedName);
            mapCache.clear();
            log.info("[Goya] |- starter [redis] |- redis cache [{}] cleared", prefixedName);
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

    /**
     * 获取 RedissonClient 实例（用于高级操作）
     *
     * @return RedissonClient 实例
     */
    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    // ==================== 批量操作优化（使用 Pipeline）====================

    /**
     * <p>批量写入（使用 Pipeline 优化）</p>
     * <p>将多个写入操作打包为一次网络调用，显著提升批量写入性能</p>
     *
     * @param cacheName 缓存名称
     * @param entries   要写入的键值对
     * @param duration  过期时间
     * @param <K>       键类型
     * @param <V>       值类型
     */
    public <K, V> void putBatch(String cacheName, Map<K, V> entries, Duration duration) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        try {
            // 创建批处理（Pipeline）
            RBatch batch = redissonClient.createBatch();
            String prefixedName = buildCachePrefix(cacheName);
            RMapCacheAsync<K, V> asyncMapCache = batch.getMapCache(prefixedName);

            long ttlMillis = duration.toMillis();

            // 批量写入（所有操作打包为一次网络调用）
            for (Map.Entry<K, V> entry : entries.entrySet()) {
                asyncMapCache.putAsync(entry.getKey(), entry.getValue(), ttlMillis, TimeUnit.MILLISECONDS);
            }

            // 执行批处理（一次性发送所有命令）
            batch.execute();

            log.trace("[Goya] |- starter [redis] |- redis batch put {} entries to cache [{}]",
                    entries.size(), cacheName);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- redis cache [{}] batch put failed", cacheName, e);
        }
    }

    // ==================== 布隆过滤器实现（防止缓存穿透）====================

    @Override
    public <K> boolean mightContain(String cacheName, K key) {
        try {
            String filterName = buildCachePrefix(cacheName) + "bloom";
            RBloomFilter<Object> filter = redissonClient.getBloomFilter(filterName);

            if (!filter.isExists()) {
                // 过滤器不存在，假设可能存在（保守策略）
                return true;
            }

            return filter.contains(key);
        } catch (Exception e) {
            log.warn("[Goya] |- starter [redis] |- Bloom filter check failed for key [{}]: {}",
                    key, e.getMessage());
            // 出错时假设可能存在（保守策略，避免误杀）
            return true;
        }
    }

    @Override
    public <K> void addToBloomFilter(String cacheName, K key) {
        try {
            String filterName = buildCachePrefix(cacheName) + "bloom";
            RBloomFilter<Object> filter = redissonClient.getBloomFilter(filterName);

            // 初始化过滤器（如果未初始化）
            if (!filter.isExists()) {
                // 预期 10 万元素
                long expectedInsertions = 100000L;
                // 1% 误判率
                double fpp = 0.01;
                filter.tryInit(expectedInsertions, fpp);
                log.debug("[Goya] |- starter [redis] |- Bloom filter initialized for cache [{}], " +
                                "expected {} insertions, fpp {}",
                        cacheName, expectedInsertions, fpp);
            }

            filter.add(key);
            log.trace("[Goya] |- starter [redis] |- Key [{}] added to bloom filter for cache [{}]",
                    key, cacheName);
        } catch (Exception e) {
            log.warn("[Goya] |- starter [redis] |- Failed to add key [{}] to bloom filter: {}",
                    key, e.getMessage());
        }
    }
}
