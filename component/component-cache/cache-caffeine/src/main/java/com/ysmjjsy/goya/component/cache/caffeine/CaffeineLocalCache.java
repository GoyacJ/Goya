package com.ysmjjsy.goya.component.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.multilevel.core.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.support.SimpleValueWrapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存实现
 *
 * <p>使用 Caffeine 实现 {@link LocalCache} 接口，提供高性能的本地缓存能力。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>使用 Caffeine 作为底层缓存实现</li>
 *   <li>支持 TTL 配置的写入操作</li>
 *   <li>支持批量操作以提升性能</li>
 *   <li>处理 NullValueWrapper 的存储和读取</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>实现 {@link LocalCache} 接口，由 {@link GoyaCache} 使用</li>
 *   <li>返回的 ValueWrapper 完全兼容 Spring Cache SPI</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li><b>初始化：</b>
 *     <ol>
 *       <li>使用 Caffeine.newBuilder() 创建 Cache</li>
 *       <li>配置 maximumSize（从 CacheSpecification 读取）</li>
 *       <li>配置 expireAfterWrite（使用默认 TTL，实际写入时使用 put(key, value, ttl)）</li>
 *     </ol>
 *   </li>
 *   <li><b>put(key, value, ttl)：</b>
 *     <ol>
 *       <li>使用 Caffeine 的 expireAfterWrite 策略（但 Caffeine 不支持每个 key 独立的 TTL）</li>
 *       <li>因此使用统一的 TTL（从配置读取），或使用 Caffeine 的 expireVariably 策略</li>
 *       <li>写入缓存</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>Caffeine Cache 是线程安全的，支持高并发读写</li>
 *   <li>所有操作都是同步的</li>
 * </ul>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>如果 key 为 null，抛出 {@link IllegalArgumentException}</li>
 *   <li>如果 TTL 无效，抛出 {@link IllegalArgumentException}</li>
 *   <li>如果缓存操作失败，抛出 {@link RuntimeException}</li>
 * </ul>
 *
 * <p><b>注意：</b>
 * <ul>
 *   <li>Caffeine 的 expireAfterWrite 是全局策略，不支持每个 key 独立的 TTL</li>
 *   <li>当前实现使用全局 TTL（从配置读取），所有 key 使用相同的过期时间</li>
 *   <li>put(key, value, ttl) 方法中的 ttl 参数会被忽略，实际使用配置中的 localTtl</li>
 *   <li>如果需要每个 key 独立的 TTL，需要使用 Caffeine 3.x 的 expireVariably 策略（未来版本支持）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:52
 */
@Slf4j
public class CaffeineLocalCache implements LocalCache {

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * Caffeine Cache 实例
     */
    private final Cache<Object, Object> cache;

    /**
     * 默认 TTL（用于 put(key, value) 方法）
     */
    private final Duration defaultTtl;

    /**
     * 构造函数
     *
     * @param name 缓存名称
     * @param spec 缓存配置规范
     * @throws IllegalArgumentException 如果参数无效
     */
    public CaffeineLocalCache(String name, CacheSpecification spec) {
        if (name == null || spec == null) {
            throw new IllegalArgumentException("Name and spec cannot be null");
        }
        this.name = name;
        this.defaultTtl = spec.getLocalTtl();

        // 构建 Caffeine Cache
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(spec.getLocalMaxSize())
                .expireAfterWrite(defaultTtl.toMillis(), TimeUnit.MILLISECONDS);

        // 如果支持可变过期时间，使用 expireVariably
        // 注意：Caffeine 3.x 支持 expireVariably，但需要额外的配置
        // 当前实现使用全局 expireAfterWrite，简化实现

        this.cache = builder.build();

        log.info("Created CaffeineLocalCache: name={}, maxSize={}, defaultTtl={}",
                name, spec.getLocalMaxSize(), defaultTtl);
    }

    @Override
    @NullMarked
    public String getName() {
        return name;
    }

    @Override
    @NullMarked
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object value = cache.getIfPresent(key);
        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
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
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        try {
            @SuppressWarnings("unchecked")
            T value = (T) cache.get(key, k -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new CacheException("Value loader failed", e);
                }
            });
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        put(key, value, defaultTtl);
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        // Caffeine 的 expireAfterWrite 是全局策略，不支持每个 key 独立的 TTL
        // 当前实现使用全局 TTL（从配置读取），忽略传入的 ttl 参数
        // 如果需要支持每个 key 独立的 TTL，需要使用 Caffeine 3.x 的 expireVariably 策略
        // 但为了简化实现，当前使用全局 TTL
        //
        // 注意：传入的 ttl 参数会被忽略，实际使用构造函数中配置的 defaultTtl
        // 这是当前实现的已知限制，所有 key 使用相同的过期时间

        cache.put(key, value);

        if (log.isTraceEnabled()) {
            log.trace("Put to CaffeineLocalCache: name={}, key={}, requestedTtl={}, actualTtl={}",
                    name, key, ttl, defaultTtl);
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public Map<Object, ValueWrapper> getAll(Set<Object> keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.isEmpty()) {
            return new HashMap<>();
        }

        Map<Object, ValueWrapper> result = new HashMap<>();
        for (Object key : keys) {
            if (key == null) {
                continue; // 跳过 null key
            }
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                result.put(key, wrapper);
            }
        }
        return result;
    }

    @Override
    public void putAll(Map<Object, Object> entries, Duration ttl) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        // 批量写入
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) {
                continue;
            }
            try {
                put(key, value, ttl);
            } catch (Exception e) {
                log.warn("Failed to put entry to CaffeineLocalCache: key={}", key, e);
                // 继续处理其他条目
            }
        }
    }

    // ========== 原子操作 ==========

    @Override
    public long increment(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Caffeine 不支持真正的原子操作，使用 get + put 实现（线程安全但非原子）
        // 注意：在高并发场景下可能丢失一致性，但功能可用
        Object currentValue = cache.getIfPresent(key);
        long current = 0L;
        if (currentValue instanceof Number nu) {
            current = nu.longValue();
        } else if (currentValue != null) {
            // 尝试转换为 Long
            try {
                current = Long.parseLong(currentValue.toString());
            } catch (NumberFormatException _) {
                log.warn("Failed to parse current value as Long, treating as 0: key={}, value={}", key, currentValue);
            }
        }

        long newValue = current + 1;
        cache.put(key, newValue);
        return newValue;
    }

    @Override
    public long incrementBy(Object key, long delta) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Caffeine 不支持真正的原子操作，使用 get + put 实现（线程安全但非原子）
        Object currentValue = cache.getIfPresent(key);
        long current = 0L;
        if (currentValue instanceof Number nu) {
            current = nu.longValue();
        } else if (currentValue != null) {
            try {
                current = Long.parseLong(currentValue.toString());
            } catch (NumberFormatException _) {
                log.warn("Failed to parse current value as Long, treating as 0: key={}, value={}", key, currentValue);
            }
        }

        long newValue = current + delta;
        cache.put(key, newValue);
        return newValue;
    }

    @Override
    public long decrement(Object key) {
        // 递减等价于递增 -1
        return incrementBy(key, -1);
    }

    @Override
    public boolean expire(Object key, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        // Caffeine 不支持单个 key 的过期时间设置，只能使用全局策略
        // 如果 key 不存在，返回 false
        if (cache.getIfPresent(key) == null) {
            return false;
        }

        // 由于 Caffeine 的限制，无法为单个 key 设置过期时间
        // 这里抛出 UnsupportedOperationException，让调用方知道此操作不支持
        throw new UnsupportedOperationException(
                "CaffeineLocalCache does not support per-key expiration. " +
                        "All keys use the global expiration policy configured at cache creation time.");
    }

}

