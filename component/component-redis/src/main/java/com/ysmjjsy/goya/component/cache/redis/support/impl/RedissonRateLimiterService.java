package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisRateLimiterService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * <p>基于 Redisson 的分布式限流实现</p>
 *
 * @author goya
 * @since 2026/1/25 23:37
 */
@RequiredArgsConstructor
public class RedissonRateLimiterService implements RedisRateLimiterService {

    private static final String NS = "rl";

    private final RedissonClient redisson;
    private final RedisKeySupport keys;

    @Override
    public void initIfAbsent(String name, long permitsPerPeriod, Duration period) {
        if (permitsPerPeriod <= 0) {
            throw new IllegalArgumentException("permitsPerPeriod 必须 > 0");
        }
        RRateLimiter limiter = redisson.getRateLimiter(keys.name(NS, name));
        // trySetRate：仅在未初始化时生效；已初始化不会覆盖
        limiter.trySetRate(RateType.OVERALL, permitsPerPeriod, period);
    }

    @Override
    public boolean tryAcquire(String name, long permits) {
        if (permits <= 0) {
            permits = 1;
        }
        RRateLimiter limiter = redisson.getRateLimiter(keys.name(NS, name));
        return limiter.tryAcquire(permits);
    }
}