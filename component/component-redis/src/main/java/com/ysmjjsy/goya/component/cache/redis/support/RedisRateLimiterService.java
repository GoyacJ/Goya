package com.ysmjjsy.goya.component.cache.redis.support;

import java.time.Duration;

/**
 * <p>Redis 分布式限流服务。</p>
 *
 * @author goya
 * @since 2026/1/25 23:36
 */
public interface RedisRateLimiterService {

    /**
     * 配置限流器（若已存在则保持现有配置）。
     *
     * @param name             限流器名
     * @param permitsPerPeriod 每周期允许的请求数
     * @param period           周期
     */
    void initIfAbsent(String name, long permitsPerPeriod, Duration period);

    /**
     * 尝试获取令牌。
     *
     * @param name    限流器名
     * @param permits 需要的令牌数
     * @return 是否获取成功
     */
    boolean tryAcquire(String name, long permits);
}