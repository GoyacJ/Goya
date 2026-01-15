package com.ysmjjsy.goya.component.cache.redis.constants;

import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;

/**
 * <p>Redis 常量定义接口</p>
 * <p>包含 Redis 相关的配置前缀、默认值、分隔符等常量</p>
 *
 * @author goya
 * @since 2025/12/21 23:55
 */
public interface RedisConst {

    /* ---------- 配置属性前缀 ---------- */

    /**
     * Redis 配置前缀
     * 配置示例: platform.redis.lock-timeout=30s
     */
    String PROPERTY_REDIS = CacheConst.PROPERTY_CACHE + ".redis";

    /* ---------- Redis 键前缀 ---------- */

    /**
     * Redis 键前缀
     */
    String REDIS_PREFIX = "redis:";

    /**
     * Redis 锁键前缀
     */
    String REDIS_LOCK_PREFIX = REDIS_PREFIX + "lock:";

    /**
     * Redis 计数器键前缀
     */
    String REDIS_COUNTER_PREFIX = REDIS_PREFIX + "counter:";

    /**
     * Redis 信号量键前缀
     */
    String REDIS_SEMAPHORE_PREFIX = REDIS_PREFIX + "semaphore:";

    /**
     * Redis 倒计时门闩键前缀
     */
    String REDIS_COUNTDOWN_PREFIX = REDIS_PREFIX + "countdown:";

    /**
     * Redis 发布订阅主题前缀
     */
    String REDIS_TOPIC_PREFIX = REDIS_PREFIX + "topic:";

    /**
     * 缓存失效消息主题
     */
    String CACHE_INVALIDATE_TOPIC = REDIS_TOPIC_PREFIX + "cache:invalidate";

    /**
     * Redis 缓存名称前缀
     */
    String REDIS_CACHE_PREFIX = "redis:cache:";

    /**
     * Redis Map Cache 名称前缀
     */
    String REDIS_MAP_CACHE_PREFIX = REDIS_CACHE_PREFIX + "map:";

    /**
     * Redis Bucket 名称前缀
     */
    String REDIS_BUCKET_PREFIX = REDIS_CACHE_PREFIX + "bucket:";
}

