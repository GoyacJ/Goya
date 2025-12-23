package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.model.CacheNullValue;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>缓存服务抽象基类</p>
 * <p>提供缓存操作的通用模板方法，子类只需实现具体的缓存操作逻辑</p>
 *
 * @author goya
 * @see ICacheService
 * @since 2025/12/21 23:35
 */
@Slf4j
public abstract class AbstractCacheService implements ICacheService {

    protected final CacheProperties cacheProperties;

    protected AbstractCacheService(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * 构建完整的缓存前缀
     * 格式: {cachePrefix}{cacheName}
     *
     * @param cacheName 缓存名称
     * @return 完整的缓存键
     */
    protected String buildCachePrefix(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            throw new CacheException("Cache name cannot be blank");
        }

        String prefix = cacheProperties.cachePrefix();

        return ICacheConstants.CACHE_PREFIX + prefix + cacheName + ICacheConstants.CACHE_SEPARATOR;
    }

    /**
     * 构建分布式锁的完整键
     * 格式: cache:{keyPrefix}lock:{cacheName}:{key}
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 完整的锁键
     */
    protected String buildLockKey(String cacheName, Object key) {
        if (key == null) {
            throw new CacheException("Cache key cannot be null");
        }

        String prefix = cacheProperties.cachePrefix();
        return ICacheConstants.CACHE_PREFIX + prefix + "lock"
                + ICacheConstants.CACHE_SEPARATOR + cacheName
                + ICacheConstants.CACHE_SEPARATOR + key;
    }

    /**
     * 获取默认过期时间
     *
     * @return 默认过期时间
     */
    protected Duration getDefaultTtl() {
        return cacheProperties.defaultTtl();
    }

    /**
     * <p>获取随机化的 TTL（防止缓存雪崩）</p>
     * <p>在基础 TTL 上增加 ±10% 的随机抖动，避免大量 key 同时过期</p>
     * <p>使用场景：批量数据预热、定时任务刷新缓存</p>
     * 
     * <p>示例：</p>
     * <ul>
     *     <li>输入 TTL: 60秒</li>
     *     <li>输出 TTL: 54-66秒之间的随机值</li>
     * </ul>
     *
     * @param baseTtl 基础 TTL
     * @return 随机化后的 TTL（最小 1 秒）
     */
    protected Duration getRandomizedTtl(Duration baseTtl) {
        long baseMillis = baseTtl.toMillis();

        // 计算抖动范围：±10%
        long jitter = ThreadLocalRandom.current().nextLong(
                -baseMillis / 10,      // -10%
                baseMillis / 10 + 1    // +10%
        );

        long finalMillis = baseMillis + jitter;
        // 确保最小 1 秒
        return Duration.ofMillis(Math.max(1000, finalMillis));
    }

    /**
     * 验证缓存名称和键
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    protected void validateCacheNameAndKey(String cacheName, Object key) {
        if (StringUtils.isBlank(cacheName)) {
            throw new CacheException("Cache name cannot be blank");
        }
        if (key == null) {
            throw new CacheException("Cache key cannot be null");
        }
    }

    /**
     * 验证缓存名称和键集合
     *
     * @param cacheName 缓存名称
     * @param keys      缓存键集合
     */
    protected void validateCacheNameAndKeys(String cacheName, Set<?> keys) {
        if (StringUtils.isBlank(cacheName)) {
            throw new CacheException("Cache name cannot be blank");
        }
        if (keys == null || keys.isEmpty()) {
            throw new CacheException("Cache keys cannot be null or empty");
        }
    }

    /**
     * 处理缓存操作异常
     *
     * @param operation 操作名称
     * @param cacheName 缓存名称
     * @param e         异常
     */
    protected void handleException(String operation, String cacheName, Exception e) {
        String message = String.format("Cache operation [%s] failed for cache [%s]", operation, cacheName);
        log.error(message, e);
        throw new CacheException(message, e);
    }

    @Override
    public <K, V> V get(String cacheName, @NotBlank K key) {
        validateCacheNameAndKey(cacheName, key);
        V value = doGet(cacheName, key);
        // 检测并过滤空值哨兵
        return CacheNullValue.isNullValue(value) ? null : value;
    }

    @Override
    public <K, V> V get(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        validateCacheNameAndKey(cacheName, key);
        if (mappingFunction == null) {
            throw new CacheException("Mapping function cannot be null");
        }
        V value = doGetOrLoad(cacheName, key, mappingFunction);
        // 检测并过滤空值哨兵
        return CacheNullValue.isNullValue(value) ? null : value;
    }

    @Override
    public <K, V> Map<K, @NonNull V> get(String cacheName, Set<? extends K> keys) {
        validateCacheNameAndKeys(cacheName, keys);
        Map<K, V> result = doGetBatch(cacheName, keys);
        // 过滤空值哨兵
        result.entrySet().removeIf(entry -> CacheNullValue.isNullValue(entry.getValue()));
        @SuppressWarnings("unchecked")
        Map<K, @NonNull V> nonNullResult = (Map<K, @NonNull V>) result;
        return nonNullResult;
    }

    @Override
    public <K, V> Map<K, @NonNull V> get(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        validateCacheNameAndKeys(cacheName, keys);
        if (mappingFunction == null) {
            throw new CacheException("Mapping function cannot be null");
        }
        Map<K, V> result = doGetBatchOrLoad(cacheName, keys, mappingFunction);
        // 过滤空值哨兵
        result.entrySet().removeIf(entry -> CacheNullValue.isNullValue(entry.getValue()));
        @SuppressWarnings("unchecked")
        Map<K, @NonNull V> nonNullResult = (Map<K, @NonNull V>) result;
        return nonNullResult;
    }

    @Override
    public <K, V> void put(String cacheName, @NotBlank K key, V value) {
        validateCacheNameAndKey(cacheName, key);
        // 使用随机 TTL 防止缓存雪崩
        Duration randomizedTtl = getRandomizedTtl(getDefaultTtl());
        doPut(cacheName, key, value, randomizedTtl);
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value, Duration duration) {
        validateCacheNameAndKey(cacheName, key);
        if (duration == null || duration.isNegative()) {
            throw new CacheException("Duration must be positive");
        }
        doPut(cacheName, key, value, duration);
    }

    @Override
    public <K> Boolean remove(String cacheName, @NotBlank K key) {
        validateCacheNameAndKey(cacheName, key);
        return doRemove(cacheName, key);
    }

    @Override
    public <K> void remove(String cacheName, @NotBlank Set<? extends K> keys) {
        validateCacheNameAndKeys(cacheName, keys);
        doRemoveBatch(cacheName, keys);
    }

    @Override
    public <K, V> V computeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        validateCacheNameAndKey(cacheName, key);
        if (loader == null) {
            throw new CacheException("Loader function cannot be null");
        }
        return doComputeIfAbsent(cacheName, key, loader);
    }

    @Override
    public <K> boolean lockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        validateCacheNameAndKey(cacheName, key);
        if (expire == null || expire.isNegative()) {
            throw new CacheException("Expire duration must be positive");
        }
        if (action == null) {
            throw new CacheException("Action cannot be null");
        }
        return doLockAndRun(cacheName, key, expire, action);
    }

    @Override
    public <K, V> V getWithLock(String cacheName, K key, Duration lockTimeout, Function<K, V> loader) {
        validateCacheNameAndKey(cacheName, key);
        if (lockTimeout == null || lockTimeout.isNegative()) {
            throw new CacheException("Lock timeout must be positive");
        }
        if (loader == null) {
            throw new CacheException("Loader function cannot be null");
        }

        // 1. 先尝试从缓存获取（快速路径）
        V value = get(cacheName, key);
        if (value != null) {
            log.trace("[Goya] |- Cache |- getWithLock cache hit for key [{}] in cache [{}]", key, cacheName);
            return value;
        }

        // 2. 布隆过滤器检查（防止穿透）
        try {
            if (!mightContain(cacheName, key)) {
                log.debug("[Goya] |- Cache |- Key [{}] not in bloom filter for cache [{}], skip loading",
                        key, cacheName);
                return null;
            }
        } catch (UnsupportedOperationException e) {
            // 子类未实现布隆过滤器，跳过检查
            log.trace("[Goya] |- Cache |- Bloom filter not supported, skip check");
        }

        // 3. 使用分布式锁加载数据（防止击穿）
        AtomicReference<V> result = new AtomicReference<>();
        boolean success = lockAndRun(cacheName, key, lockTimeout, () -> {
            // 双重检查（DCL）- 可能其他线程已经加载
            V doubleCheck = get(cacheName, key);
            if (doubleCheck != null) {
                result.set(doubleCheck);
                log.trace("[Goya] |- Cache |- DCL cache hit for key [{}] in cache [{}]", key, cacheName);
                return;
            }

            // 加载数据
            log.debug("[Goya] |- Cache |- Loading data for key [{}] in cache [{}]", key, cacheName);
            V loaded = loader.apply(key);
            if (loaded != null) {
                // 缓存加载的数据（使用随机 TTL 防止雪崩）
                Duration ttl = getRandomizedTtl(getDefaultTtl());
                put(cacheName, key, loaded, ttl);
                result.set(loaded);
                log.debug("[Goya] |- Cache |- Data loaded and cached for key [{}]", key);
            } else {
                // 空值缓存哨兵，防止穿透（短 TTL，1分钟）
                Duration shortTtl = Duration.ofMinutes(1);
                @SuppressWarnings("unchecked")
                V sentinel = (V) CacheNullValue.INSTANCE;
                put(cacheName, key, sentinel, shortTtl);
                log.debug("[Goya] |- Cache |- Null sentinel cached for key [{}] with short TTL [{}]",
                        key, shortTtl);
            }
        });

        if (!success) {
            log.warn("[Goya] |- Cache |- Failed to acquire lock for key [{}] in cache [{}]", key, cacheName);
        }

        return result.get();
    }

    /* ---------- 抽象方法，由子类实现 ---------- */

    /**
     * 获取缓存值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存值
     */
    protected abstract <K, V> V doGet(String cacheName, K key);

    /**
     * 获取缓存值，如果不存在则加载
     *
     * @param cacheName       缓存名称
     * @param key             缓存键
     * @param mappingFunction 值加载函数
     * @param <K>             键类型
     * @param <V>             值类型
     * @return 缓存值
     */
    protected abstract <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * 批量获取缓存值
     *
     * @param cacheName 缓存名称
     * @param keys      缓存键集合
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存值映射
     */
    protected abstract <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys);

    /**
     * 批量获取缓存值，如果不存在则加载
     *
     * @param cacheName       缓存名称
     * @param keys            缓存键集合
     * @param mappingFunction 批量加载函数
     * @param <K>             键类型
     * @param <V>             值类型
     * @return 缓存值映射
     */
    protected abstract <K, V> Map<K, @NonNull V> doGetBatchOrLoad(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction);

    /**
     * 设置缓存值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     * @param duration  过期时间
     * @param <K>       键类型
     * @param <V>       值类型
     */
    protected abstract <K, V> void doPut(String cacheName, K key, V value, Duration duration);

    /**
     * 删除缓存值
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return 是否删除成功
     */
    protected abstract <K> Boolean doRemove(String cacheName, K key);

    /**
     * 批量删除缓存值
     *
     * @param cacheName 缓存名称
     * @param keys      缓存键集合
     * @param <K>       键类型
     */
    protected abstract <K> void doRemoveBatch(String cacheName, Set<? extends K> keys);

    /**
     * 如果不存在则计算并缓存
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param loader    值加载函数
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存值
     */
    protected abstract <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader);

    /**
     * 尝试获取锁
     *
     * @param cacheName 缓存名称
     * @param key       锁键
     * @param expire    过期时间
     * @param <K>       键类型
     */
    protected abstract <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action);

    // ==================== 缓存预热与刷新（ICacheWarmup 实现）====================

    @Override
    public <K, V> void warmup(String cacheName, Map<K, V> data) {
        if (data == null || data.isEmpty()) {
            log.warn("[Goya] |- Cache |- Warmup data is empty for cache [{}]", cacheName);
            return;
        }

        log.info("[Goya] |- Cache |- Starting warmup for cache [{}] with {} entries",
                cacheName, data.size());

        long startTime = System.currentTimeMillis();
        Duration ttl = getRandomizedTtl(getDefaultTtl());

        // 批量写入（使用随机 TTL 防止雪崩）
        for (Map.Entry<K, V> entry : data.entrySet()) {
            put(cacheName, entry.getKey(), entry.getValue(), ttl);
            // 添加到布隆过滤器（如果实现了）
            try {
                addToBloomFilter(cacheName, entry.getKey());
            } catch (UnsupportedOperationException e) {
                // 子类未实现布隆过滤器，忽略
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[Goya] |- Cache |- Warmup completed for cache [{}], {} entries in {}ms",
                cacheName, data.size(), elapsed);
    }

    @Override
    public <K, V> void refreshAsync(String cacheName, K key, Function<K, V> loader) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("[Goya] |- Cache |- Starting async refresh for key [{}] in cache [{}]",
                        key, cacheName);

                V value = loader.apply(key);
                if (value != null) {
                    put(cacheName, key, value);
                    log.debug("[Goya] |- Cache |- Async refresh completed for key [{}]", key);
                } else {
                    log.debug("[Goya] |- Cache |- Async refresh returned null for key [{}], skip caching", key);
                }
            } catch (Exception e) {
                log.error("[Goya] |- Cache |- Async refresh failed for key [{}] in cache [{}]: {}",
                        key, cacheName, e.getMessage(), e);
            }
        });
    }

    @Override
    public <K, V> void scheduleRefresh(String cacheName, Duration interval, Supplier<Map<K, V>> loader) {
        log.warn("[Goya] |- Cache |- scheduleRefresh not implemented yet for cache [{}]", cacheName);
        // TODO: 实现定时刷新功能，需要使用 ScheduledExecutorService
        // 当前计划中不包含此功能的完整实现
    }
}

