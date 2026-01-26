package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisAtomicService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>基于 Redisson {@link RAtomicLong} 的原子计数实现</p>
 * <p>对象名：{@code atomic:{name}}</p>
 *
 * @author goya
 * @since 2026/1/25 23:45
 */
public class RedissonAtomicService implements RedisAtomicService {

    /**
     * Redis 命名空间：atomic。
     */
    private static final String NS = "atomic";

    private final RedissonClient redisson;
    private final RedisKeySupport keys;

    public RedissonAtomicService(RedissonClient redisson, RedisKeySupport keys) {
        this.redisson = Objects.requireNonNull(redisson, "redisson 不能为空");
        this.keys = Objects.requireNonNull(keys, "keys 不能为空");
    }

    @Override
    public long get(String name) {
        return atomic(name).get();
    }

    @Override
    public void set(String name, long value) {
        atomic(name).set(value);
    }

    @Override
    public long incrementAndGet(String name) {
        return atomic(name).incrementAndGet();
    }

    @Override
    public long addAndGet(String name, long delta) {
        return atomic(name).addAndGet(delta);
    }

    @Override
    public boolean compareAndSet(String name, long expect, long update) {
        return atomic(name).compareAndSet(expect, update);
    }

    @Override
    public boolean expire(String name, Duration ttl) {
        if (ttl == null) {
            return false;
        }
        long ms = ttl.toMillis();
        if (ms <= 0) {
            return false;
        }
        // RAtomicLong 实现了 RExpirable，可设置 TTL
        return atomic(name).expire(ms, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean delete(String name) {
        return atomic(name).delete();
    }

    /**
     * 获取原子计数器对象。
     *
     * @param name 计数器名
     * @return RAtomicLong
     */
    private RAtomicLong atomic(String name) {
        return redisson.getAtomicLong(keys.name(NS, name));
    }
}