package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties.GoyaCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Caffeine CacheManager（支持 per-entry TTL）</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>对每个 cacheName 单独创建一个 Caffeine cache（不同 TTL/容量）</li>
 *   <li>内部存储 {@link CacheValue} 并通过 {@link Expiry} 计算动态过期</li>
 *   <li>支持懒创建：调用 {@link #getCache(String)} 时自动创建</li>
 *   <li>实现 {@link #getCacheNames()} 以满足 Spring CacheManager 规范</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/12 22:34
 */
@Slf4j
public class GoyaCaffeineCacheManager implements CacheManager {

    private final GoyaCacheProperties properties;
    /**
     * 缓存实例容器：cacheName -> Cache。
     */
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    /**
     * 构造 CacheManager。
     *
     * @param properties 配置项
     */
    public GoyaCaffeineCacheManager(GoyaCacheProperties properties) {
        this.properties = properties;

        // 预注册配置中声明的 cacheName（不强制创建实例，但让 getCacheNames 可见）
        if (this.properties.caches() != null) {
            for (String name : this.properties.caches().keySet()) {
                if (name != null && !name.isBlank()) {
                    cacheMap.putIfAbsent(name, nullCachePlaceholder(name));
                }
            }
        }
    }
    @Override
    @NullMarked
    public Cache getCache(String name) {
        // computeIfAbsent 无法区分“预注册但尚未创建”的占位，这里用手动逻辑
        Cache existing = cacheMap.get(name);
        if (existing != null && existing != NullCache.INSTANCE) {
            return existing;
        }
        Cache created = createCache(name);
        cacheMap.put(name, created);
        return created;
    }

    /**
     * 返回所有已知 cacheName。
     *
     * <p>包含：</p>
     * <ul>
     *   <li>配置文件中声明的 cacheName</li>
     *   <li>运行时通过 {@link #getCache(String)} 懒创建的 cacheName</li>
     * </ul>
     *
     * @return cacheName 集合（只读视图）
     */
    @Override
    @NullMarked
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    private Cache createCache(String name) {
        CacheSpec spec = properties.caches() == null ? null : properties.caches().get(name);
        Duration ttl = (spec != null && spec.ttl() != null) ? spec.ttl() : properties.defaultTtl();
        long maxSize = (spec != null && spec.maximumSize() > 0) ? spec.maximumSize() : properties.defaultMaximumSize();

        var caffeine = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfter(new Expiry<Object, CacheValue>() {
                    @Override
                    public long expireAfterCreate(Object key, CacheValue value, long currentTime) {
                        return remainingNanos(value);
                    }

                    @Override
                    public long expireAfterUpdate(Object key, CacheValue value, long currentTime, long currentDuration) {
                        return remainingNanos(value);
                    }

                    @Override
                    public long expireAfterRead(Object key, CacheValue value, long currentTime, long currentDuration) {
                        // 读不刷新 TTL（写入后过期）
                        return currentDuration;
                    }

                    private long remainingNanos(CacheValue v) {
                        long exp = v.expireAtNanos();
                        if (exp == Long.MAX_VALUE) {
                            return Long.MAX_VALUE;
                        }
                        long now = System.nanoTime();
                        long remain = exp - now;
                        return Math.max(0L, remain);
                    }
                });

        if (properties.recordStats()) {
            caffeine.recordStats();
        }

        com.github.benmanes.caffeine.cache.Cache<Object, CacheValue> nativeCache = caffeine.build();
        return new GoyaCaffeineCache(name, nativeCache, ttl, properties.allowNullValues());
    }

    /**
     * 用于预注册 cacheName 的占位。
     *
     * <p>Spring 只要求能列出 cacheName，不要求一定已有实例。</p>
     *
     * @param name 缓存名
     * @return 占位 Cache
     */
    @SuppressWarnings("all")
    private Cache nullCachePlaceholder(String name) {
        return NullCache.INSTANCE;
    }

    /**
     * 空占位 Cache（不应被真正使用）。
     *
     * <p>仅用于 {@link #getCacheNames()} 可见性预注册。</p>
     */
    @SuppressWarnings("all")
    private enum NullCache implements Cache {
        /** 单例 */
        INSTANCE;

        @Override
        @NullMarked
        public String getName() {
            return "NULL";
        }

        @Override
        @NullMarked
        public Object getNativeCache() {
            return this;
        }

        @Override
        public ValueWrapper get(Object key) {
            return null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return null;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            return null;
        }

        @Override
        public void put(Object key, Object value) {
            // no-op
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            return null;
        }

        @Override
        public void evict(Object key) {
            // no-op
        }

        @Override
        public boolean evictIfPresent(Object key) {
            return false;
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public boolean invalidate() {
            return true;
        }
    }
}
