package com.ysmjjsy.goya.starter.redis.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.service.IL2Cache;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import lombok.Getter;
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
    /**
     * -- GETTER --
     *  获取 RedissonClient 实例（用于高级操作）
     *
     * @return RedissonClient 实例
     */
    @Getter
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
     * <p>获取 TTL（支持优先级逻辑）</p>
     * <p>优先级逻辑：</p>
     * <ol>
     *     <li>如果配置中有该 cacheName 的配置 → 使用配置的 defaultTtl</li>
     *     <li>如果配置中没有 → 使用调用方法传入的 duration</li>
     *     <li>如果 duration 为空 → 使用默认配置的 defaultTtl</li>
     * </ol>
     *
     * @param cacheName 缓存名称
     * @param duration  调用方法传入的过期时间（可选）
     * @return TTL 值
     */
    private Duration getTtl(String cacheName, Duration duration) {
        // 获取配置（优先查找特定缓存配置，否则使用默认配置）
        CacheProperties.CacheConfig cacheConfig = cacheProperties.getCacheConfig(cacheName);
        boolean hasSpecificConfig = cacheConfig != null;
        
        if (!hasSpecificConfig) {
            // 配置中没有该 cacheName，使用默认配置
            cacheConfig = cacheProperties.getCacheConfigByDefault(cacheName);
        }
        
        if (hasSpecificConfig) {
            // 优先级1：配置中有该 cacheName → 使用配置的 defaultTtl
            return cacheConfig.defaultTtl();
        } else {
            // 配置中没有该 cacheName
            if (duration != null) {
                // 优先级2：使用调用方法传入的 duration
                return duration;
            } else {
                // 优先级3：duration 为空 → 使用默认配置的 defaultTtl
                return cacheConfig.defaultTtl();
            }
        }
    }

    /**
     * 获取默认 TTL（兼容旧代码）
     * 
     * @deprecated 使用 {@link #getTtl(String, Duration)} 替代
     */
    @Deprecated
    private Duration getDefaultTtl() {
        CacheProperties.CacheConfig defaultConfig = cacheProperties.defaultConfig();
        return defaultConfig != null && defaultConfig.defaultTtl() != null
                ? defaultConfig.defaultTtl()
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
     * 获取 RMap 实例（用于永不过期的缓存）
     * 使用 cacheName 作为 Redis Map 的 key
     *
     * @param cacheName 缓存名称
     * @param <K>       键类型
     * @param <V>       值类型
     * @return RMap 实例
     */
    private <K, V> RMap<K, V> getMap(String cacheName) {
        String prefixedName = buildCachePrefix(cacheName);
        return redissonClient.getMap(prefixedName);
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
     * 统一异常处理策略：记录日志并抛出异常
     *
     * @param operation 操作名称
     * @param cacheName 缓存名称
     * @param e         异常
     * @throws CacheException 缓存异常
     */
    private void handleException(String operation, String cacheName, Exception e) {
        String message = String.format("Redis cache operation [%s] failed for cache [%s]", operation, cacheName);
        log.error("[Goya] |- starter [redis] |- {}", message, e);
        throw new CacheException(message, e);
    }

    // ==================== IL2Cache 接口实现 ====================

    @Override
    public <K, V> V get(String cacheName, K key) {
        try {
            // 缓存穿透保护检查
            CacheProperties.CacheConfig config = cacheProperties.getCacheConfigByDefault(cacheName);
            if (Boolean.TRUE.equals(config.penetrationProtect())) {
                if (!mightContain(cacheName, key)) {
                    log.trace("[Goya] |- starter [redis] |- Key [{}] not in bloom filter for cache [{}], skip query",
                            key, cacheName);
                    return null;
                }
            }

            // 先尝试从 RMapCache 获取（有过期时间的缓存）
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            V value = mapCache.get(key);
            
            // 如果 RMapCache 中没有，尝试从 RMap 获取（永不过期的缓存）
            if (value == null) {
                RMap<K, V> map = getMap(cacheName);
                value = map.get(key);
            }
            
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] get key [{}]", cacheName, key);
            return value;
        } catch (Exception e) {
            handleException("get", cacheName, e);
            return null; // 不会执行到这里，因为 handleException 会抛出异常
        }
    }

    @Override
    public <K, V> V get(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        try {
            CacheProperties.CacheConfig config = cacheProperties.getCacheConfigByDefault(cacheName);
            
            // 缓存穿透保护检查
            if (Boolean.TRUE.equals(config.penetrationProtect())) {
                if (!mightContain(cacheName, key)) {
                    log.trace("[Goya] |- starter [redis] |- Key [{}] not in bloom filter for cache [{}], skip query",
                            key, cacheName);
                    return null;
                }
            }

            // 先尝试从 RMapCache 获取（有过期时间的缓存）
            RMapCache<K, V> mapCache = getMapCache(cacheName);
            V value = mapCache.get(key);

            // 如果 RMapCache 中没有，尝试从 RMap 获取（永不过期的缓存）
            if (value == null) {
                RMap<K, V> map = getMap(cacheName);
                value = map.get(key);
            }

            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    // 使用优先级逻辑获取 TTL（传入 null，会使用配置的 defaultTtl）
                    Duration ttl = getTtl(cacheName, null);
                    mapCache.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
                    log.trace("[Goya] |- starter [redis] |- redis cache [{}] load and put key [{}]",
                            cacheName, key);
                } else {
                    // 如果加载的值为 null，且配置不允许 null 值，使用哨兵值
                    if (!Boolean.TRUE.equals(config.allowNullValues())) {
                        Duration ttl = config.penetrationProtectTimeout() != null
                                ? config.penetrationProtectTimeout()
                                : Duration.ofMinutes(1);
                        @SuppressWarnings("unchecked")
                        V sentinel = (V) com.ysmjjsy.goya.component.cache.model.CacheNullValue.INSTANCE;
                        mapCache.put(key, sentinel, ttl.toMillis(), TimeUnit.MILLISECONDS);
                        log.trace("[Goya] |- starter [redis] |- redis cache [{}] put null sentinel for key [{}]",
                                cacheName, key);
                    }
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
                    // 使用优先级逻辑获取 TTL（传入 null，会使用配置的 defaultTtl）
                    Duration ttl = getTtl(cacheName, null);
                    long ttlMillis = ttl.toMillis();

                    loadedValues.forEach((key, value) -> {
                        if (value != null) {
                            mapCache.put(key, value, ttlMillis, TimeUnit.MILLISECONDS);
                            result.put(key, value);
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
            // 获取实际的 TTL（支持优先级逻辑）
            Duration actualTtl = getTtl(cacheName, duration);
            
            // 判断是否永不过期
            boolean eternal = com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties.isEternal(actualTtl);

            if (eternal) {
                // 永不过期：使用 RMap（不设置 TTL）
                RMap<K, V> map = getMap(cacheName);
                map.put(key, value);
                log.trace("[Goya] |- starter [redis] |- redis cache [{}] put eternal key [{}]", 
                        cacheName, key);
            } else {
                // 有过期时间：使用 RMapCache（设置 TTL）
                RMapCache<K, V> mapCache = getMapCache(cacheName);
                long ttlMillis = actualTtl.toMillis();
                mapCache.put(key, value, ttlMillis, TimeUnit.MILLISECONDS);
                log.trace("[Goya] |- starter [redis] |- redis cache [{}] put key [{}] with ttl [{}]",
                        cacheName, key, actualTtl);
            }

            // 自动添加到布隆过滤器
            addToBloomFilter(cacheName, key);
        } catch (Exception e) {
            handleException("put", cacheName, e);
        }
    }

    @Override
    public <K> Boolean remove(String cacheName, K key) {
        try {
            boolean removed = false;
            
            // 尝试从 RMapCache 删除（有过期时间的缓存）
            RMapCache<K, Object> mapCache = getMapCache(cacheName);
            Object removedFromCache = mapCache.remove(key);
            if (removedFromCache != null) {
                removed = true;
            }
            
            // 尝试从 RMap 删除（永不过期的缓存）
            RMap<K, Object> map = getMap(cacheName);
            Object removedFromMap = map.remove(key);
            if (removedFromMap != null) {
                removed = true;
            }
            
            log.trace("[Goya] |- starter [redis] |- redis cache [{}] remove key [{}]", cacheName, key);
            return removed;
        } catch (Exception e) {
            handleException("remove", cacheName, e);
            return false; // 不会执行到这里，因为 handleException 会抛出异常
        }
    }

    @Override
    public <K> void remove(String cacheName, Set<? extends K> keys) {
        try {
            RMapCache<K, Object> mapCache = getMapCache(cacheName);
            RMap<K, Object> map = getMap(cacheName);
            
            // 批量删除：同时从 RMapCache 和 RMap 删除
            for (K key : keys) {
                mapCache.remove(key);
                map.remove(key);
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
                    // 使用优先级逻辑获取 TTL（传入 null，会使用配置的 defaultTtl）
                    Duration ttl = getTtl(cacheName, null);
                    mapCache.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
                    log.trace("[Goya] |- starter [redis] |- redis cache [{}] compute and put key [{}]",
                            cacheName, key);
                }
            }

            return value;
        } catch (Exception e) {
            handleException("computeIfAbsent", cacheName, e);
            return null; // 不会执行到这里，因为 handleException 会抛出异常
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
                // 使用配置的预期插入数，如果没有配置则使用默认值 100000
                long expectedInsertions = cacheProperties.defaultConfig().bloomFilterExpectedInsertions() != null
                        ? cacheProperties.defaultConfig().bloomFilterExpectedInsertions()
                        : 100000L;
                
                // 使用配置的误判率，如果没有配置则使用默认值 0.01
                double fpp = cacheProperties.defaultConfig().bloomFilterFpp() != null
                        ? cacheProperties.defaultConfig().bloomFilterFpp()
                        : 0.01;
                
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
