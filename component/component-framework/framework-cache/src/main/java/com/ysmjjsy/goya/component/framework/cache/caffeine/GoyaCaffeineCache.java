package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:22
 */
@Slf4j
public class GoyaCaffeineCache implements org.springframework.cache.Cache {

    private final String name;
    private final Cache<Object, CacheValue> cache;
    private final Duration defaultTtl;
    private final boolean allowNullValues;

    /**
     * 构造缓存。
     *
     * @param name 缓存名
     * @param cache caffeine cache
     * @param defaultTtl 默认 TTL
     * @param allowNullValues 是否允许缓存 null
     */
    public GoyaCaffeineCache(String name, Cache<Object, CacheValue> cache, Duration defaultTtl, boolean allowNullValues) {
        this.name = Objects.requireNonNull(name, "name 不能为空");
        this.cache = Objects.requireNonNull(cache, "cache 不能为空");
        this.defaultTtl = defaultTtl;
        this.allowNullValues = allowNullValues;
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
    @NullMarked
    public ValueWrapper get(Object key) {
        CacheValue v = cache.getIfPresent(key);
        if (v == null) {
            return null;
        }
        Object raw = unwrap(v.value());
        return () -> raw;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Object key, Class<T> type) {
        CacheValue v = cache.getIfPresent(key);
        if (v == null) {
            return null;
        }
        Object raw = unwrap(v.value());
        if (raw == null) {
            return null;
        }
        if (type == null) {
            return (T) raw;
        }
        if (!type.isInstance(raw)) {
            throw new IllegalStateException("缓存类型不匹配，key=" + key + "，期望=" + type.getName() + "，实际=" + raw.getClass().getName());
        }
        return type.cast(raw);
    }

    @Override
    @NullMarked
    public <T> T get(Object key, Callable<T> valueLoader) {
        // Spring 的 get(key, loader) 语义：若不存在则加载并缓存
        CacheValue v = cache.get(key, k -> {
            T loaded = call(valueLoader);
            long now = System.nanoTime();
            long expireAt = CacheValue.computeExpireAt(now, defaultTtl);
            return CacheValue.of(loaded, expireAt);
        });
        if (v == null) {
            return null;
        }
        Object raw = unwrap(v.value());
        @SuppressWarnings("unchecked")
        T t = (T) raw;
        return t;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        put(key, value, defaultTtl);
    }

    /**
     * 写入（支持 per-entry TTL，供 CacheService 使用）。
     *
     * @param key 键
     * @param value 值
     * @param ttl TTL（为空或非正表示使用默认 TTL；Duration.ZERO 表示立即过期）
     */
    public void put(Object key, Object value, Duration ttl) {
        if (value == null && !allowNullValues) {
            cache.invalidate(key);
            return;
        }
        Duration useTtl = (ttl == null || ttl.isNegative()) ? defaultTtl : ttl;
        long now = System.nanoTime();
        long expireAt = CacheValue.computeExpireAt(now, useTtl);
        cache.put(key, CacheValue.of(value, expireAt));
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        CacheValue existed = cache.getIfPresent(key);
        if (existed != null) {
            Object raw = unwrap(existed.value());
            return () -> raw;
        }
        put(key, value);
        return null;
    }

    @Override
    @NullMarked
    public void evict(Object key) {
        cache.invalidate(key);
    }

    @Override
    @NullMarked
    public boolean evictIfPresent(Object key) {
        CacheValue existed = cache.getIfPresent(key);
        if (existed == null) {
            return false;
        }
        cache.invalidate(key);
        return true;
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public boolean invalidate() {
        clear();
        return true;
    }

    private Object unwrap(Object v) {
        if (v == CacheValue.NullValue.INSTANCE) {
            return null;
        }
        return v;
    }

    private <T> T call(Callable<T> loader) {
        try {
            return loader.call();
        } catch (Exception e) {
            throw new IllegalStateException("缓存加载失败", e);
        }
    }
}
