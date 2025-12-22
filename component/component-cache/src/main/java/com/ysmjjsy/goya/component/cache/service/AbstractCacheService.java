package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
        return doGet(cacheName, key);
    }

    @Override
    public <K, V> V get(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        validateCacheNameAndKey(cacheName, key);
        if (mappingFunction == null) {
            throw new CacheException("Mapping function cannot be null");
        }
        return doGetOrLoad(cacheName, key, mappingFunction);
    }

    @Override
    public <K, V> Map<K, @NonNull V> get(String cacheName, Set<? extends K> keys) {
        validateCacheNameAndKeys(cacheName, keys);
        return doGetBatch(cacheName, keys);
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
        return doGetBatchOrLoad(cacheName, keys, mappingFunction);
    }

    @Override
    public <K, V> void put(String cacheName, @NotBlank K key, V value) {
        validateCacheNameAndKey(cacheName, key);
        doPut(cacheName, key, value, getDefaultTtl());
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
    public <K> void tryLock(String cacheName, K key, Duration expire) {
        validateCacheNameAndKey(cacheName, key);
        if (expire == null || expire.isNegative()) {
            throw new CacheException("Expire duration must be positive");
        }
        doTryLock(cacheName, key, expire);
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
    protected abstract <K> void doTryLock(String cacheName, K key, Duration expire);

    /**
     * 获取锁并执行操作
     *
     * @param cacheName 缓存名称
     * @param key       锁键
     * @param expire    过期时间
     * @param action    执行的操作
     * @param <K>       键类型
     * @return 是否执行成功
     */
    protected abstract <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action);
}

