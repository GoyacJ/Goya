package com.ysmjjsy.goya.component.cache.multilevel.factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>Caffeine 本地缓存工厂</p>
 * <p>基于 Caffeine 创建本地缓存实例</p>
 * <p>适配 MultiLevelCacheSpec 配置</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineLocalCacheFactory implements LocalCacheFactory {

    /**
     * CaffeineCacheService（用于实现 LocalCacheFactory 接口）
     */
    private final CacheService cacheService;

    @Override
    public LocalCache<?, ?> create(String cacheName, MultiLevelCacheSpec spec) {
        if (cacheName == null || cacheName.isBlank()) {
            throw new IllegalArgumentException("Cache name cannot be null or blank");
        }
        if (spec == null || !spec.isValid()) {
            throw new IllegalArgumentException("Cache spec cannot be null and must be valid");
        }

        // 根据 spec 配置创建 Caffeine Cache
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // 设置最大容量
        if (spec.localMaximumSize() != null && spec.localMaximumSize() > 0) {
            builder.maximumSize(spec.localMaximumSize());
        }

        // 设置 TTL
        if (spec.localTtl() != null && !spec.localTtl().isZero() && !spec.localTtl().isNegative()) {
            builder.expireAfterWrite(spec.localTtl().toMillis(), TimeUnit.MILLISECONDS);
        }

        // 构建 Caffeine Cache
        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = builder.build();

        // 创建适配器，将 Caffeine Cache 适配为 LocalCache
        return new CaffeineLocalCacheAdapter(caffeineCache, spec);
    }

    @Override
    public <K, V> V get(String cacheName, K key) {
        return cacheService.get(cacheName, key);
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value) {
        cacheService.put(cacheName, key, value);
    }

    @Override
    public <K> void delete(String cacheName, K key) {
        cacheService.delete(cacheName, key);
    }

    @Override
    public <K> boolean exists(String cacheName, K key) {
        return cacheService.exists(cacheName, key);
    }

    /**
         * Caffeine LocalCache 适配器
         */
        private record CaffeineLocalCacheAdapter(Cache<Object, Object> caffeineCache,
                                                 MultiLevelCacheSpec spec) implements LocalCache<Object, Object> {
            @Override
            public Object get(Object key) {
                return caffeineCache.getIfPresent(key);
            }

            @Override
            public void put(Object key, Object value) {
                caffeineCache.put(key, value);
            }

            @Override
            public void delete(Object key) {
                caffeineCache.invalidate(key);
            }

            @Override
            public boolean exists(Object key) {
                return caffeineCache.getIfPresent(key) != null;
            }

            @Override
            public void clear() {
                caffeineCache.invalidateAll();
            }
        }
}
