package com.ysmjjsy.goya.component.cache.multilevel.factory;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCacheFactory;
import com.ysmjjsy.goya.component.cache.redis.configuration.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

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

    /**
     * Redisson 客户端（用于清空操作）
     */
    private final RedissonClient redissonClient;

    /**
     * 缓存键序列化器（用于构建 key 模式）
     */
    private final CacheKeySerializer cacheKeySerializer;

    /**
     * Redis 配置属性（用于获取 key 前缀）
     */
    private final GoyaRedisProperties goyaRedisProperties;

    @Override
    public RemoteCache<?, ?> create(String cacheName, MultiLevelCacheSpec spec) {
        if (cacheName == null || cacheName.isBlank()) {
            throw new IllegalArgumentException("Cache name cannot be null or blank");
        }
        if (spec == null || !spec.isValid()) {
            throw new IllegalArgumentException("Cache spec cannot be null and must be valid");
        }

        // 创建适配器，将 RedisCacheService 适配为 RemoteCache
        return new RedisRemoteCacheAdapter(cacheName, redisCacheService, spec, redissonClient, cacheKeySerializer, goyaRedisProperties);
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
                                               MultiLevelCacheSpec spec, RedissonClient redissonClient,
                                               CacheKeySerializer cacheKeySerializer,
                                               GoyaRedisProperties goyaRedisProperties) implements RemoteCache<Object, Object> {
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
                try {
                    // 构建 key 模式：{prefix}:{cacheName}:*
                    String keyPattern = cacheKeySerializer.buildKey(
                            goyaRedisProperties.keyPrefix(),
                            cacheName,
                            "*"
                    );

                    // 使用 Redisson 的 RKeys 删除匹配的 key
                    RKeys rKeys = redissonClient.getKeys();
                    long deletedCount = rKeys.deleteByPattern(keyPattern);

                    // 使用外部类的日志
                    RedisRemoteCacheFactory.log.debug("Cleared remote cache: cacheName={}, pattern={}, deletedCount={}",
                            cacheName, keyPattern, deletedCount);
                } catch (Exception e) {
                    RedisRemoteCacheFactory.log.error("Failed to clear remote cache: cacheName={}", cacheName, e);
                    // 不抛出异常，避免影响主流程
                }
            }
        }
}
