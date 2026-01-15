package com.ysmjjsy.goya.component.cache.multilevel.factory;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCacheFactory;
import com.ysmjjsy.goya.component.cache.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * <p>Redis 远程缓存工厂</p>
 * <p>基于 RedisCacheService 创建远程缓存实例</p>
 * <p>适配 MultiLevelCacheSpec 配置</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRemoteCacheFactory implements RemoteCacheFactory {

    /**
     * Redis 缓存服务
     */
    private final RedisCacheService redisCacheService;

    /**
     * CacheService 实现（用于实现 RemoteCacheFactory 接口）
     */
    private final CacheService cacheService;

    @Override
    public RemoteCache<?, ?> create(String cacheName, MultiLevelCacheSpec spec) {
        if (cacheName == null || cacheName.isBlank()) {
            throw new IllegalArgumentException("Cache name cannot be null or blank");
        }
        if (spec == null || !spec.isValid()) {
            throw new IllegalArgumentException("Cache spec cannot be null and must be valid");
        }

        // 创建适配器，将 RedisCacheService 适配为 RemoteCache
        return new RedisRemoteCacheAdapter(cacheName, redisCacheService, spec);
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
         * Redis RemoteCache 适配器
         */
        private record RedisRemoteCacheAdapter(String cacheName, RedisCacheService redisCacheService,
                                               MultiLevelCacheSpec spec) implements RemoteCache<Object, Object> {
            @Override
            public Object get(Object key) {
                return redisCacheService.get(cacheName, key);
            }

            @Override
            public void put(Object key, Object value) {
                // 使用 spec 中的 remoteTtl
                Duration ttl = spec.remoteTtl();
                if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
                    redisCacheService.put(cacheName, key, value, ttl);
                } else {
                    redisCacheService.put(cacheName, key, value);
                }
            }

            @Override
            public void delete(Object key) {
                redisCacheService.delete(cacheName, key);
            }

            @Override
            public boolean exists(Object key) {
                return redisCacheService.exists(cacheName, key);
            }

            @Override
            public void clear() {
                // Redis 清空操作：由于 RedisCacheService 没有提供按 cacheName 清空的方法，
                // 这里记录警告。实际应用中可以通过 RedisService 删除匹配的 key 来实现
                // 或者扩展 RedisCacheService 提供 clear(String cacheName) 方法
                log.warn("Clear operation for remote cache is not fully supported. " +
                        "Please use delete() for specific keys or implement clear logic in RedisCacheService. cacheName={}", cacheName);
            }
        }
}
