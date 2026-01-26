package com.ysmjjsy.goya.component.cache.redis.key;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import org.springframework.util.StringUtils;

/**
 * <p>Redis Key 构建支持类</p>
 *
 * @author goya
 * @since 2026/1/25 23:34
 */
public class RedisKeySupport {

    private final CacheKeySerializer cacheKeySerializer;
    private final String keyPrefix;

    /**
     * 构造函数。
     *
     * @param cacheKeySerializer 缓存 key 序列化器
     * @param goyaRedisProperties goyaRedisProperties
     */
    public RedisKeySupport(CacheKeySerializer cacheKeySerializer, GoyaRedisProperties goyaRedisProperties) {
        this.cacheKeySerializer = cacheKeySerializer;
        this.keyPrefix = goyaRedisProperties.keyPrefix();
    }

    /**
     * 构建 Redis 对象名（适用于 RLock/RTopic/RRateLimiter/RBloomFilter 等“按 name 命名”的结构）。
     *
     * @param namespace 命名空间（如 lock/rl/topic/queue/bf/atomic）
     * @param name 对象名（如 "login"、"order-create"）
     * @return 最终 redis name（String）
     */
    public String name(String namespace, String name) {
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("namespace 不能为空");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name 不能为空");
        }
        // 这里把 namespace 当 cacheName，用 buildKey 统一拼装
        return cacheKeySerializer.buildKey(keyPrefix, namespace, name);
    }

    /**
     * 构建 Redis Key（适用于 KV/Map entry 等需要 bizKey 的场景）。
     *
     * @param namespace 命名空间
     * @param name 对象名
     * @param bizKey 业务 key
     * @return 最终 redis key（String）
     */
    public String key(String namespace, String name, Object bizKey) {
        String base = name(namespace, name);
        // 再用 buildKey 将 base 当作 cacheName，拼上 bizKey
        return cacheKeySerializer.buildKey(keyPrefix, base, bizKey);
    }
}
