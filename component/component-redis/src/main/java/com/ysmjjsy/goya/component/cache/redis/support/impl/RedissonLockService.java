package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * <p>基于 Redisson 的分布式锁实现。</p>
 *
 * @author goya
 * @since 2026/1/25 23:33
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonLockService implements RedisLockService {

    private static final String NS = "lock";

    private final RedissonClient redisson;
    private final RedisKeySupport redisKeySupport;

    @Override
    public <T> T withLock(String lockName, Duration lease, Callable<T> task) {
        Objects.requireNonNull(task, "task 不能为空");
        long leaseMs = (lease == null) ? 30_000L : Math.max(1L, lease.toMillis());

        String lockKey = redisKeySupport.name(NS, lockName);
        RLock lock = redisson.getLock(lockKey);

        lock.lock(leaseMs, TimeUnit.MILLISECONDS);
        try {
            return call(task);
        } finally {
            safeUnlock(lock);
        }
    }

    @Override
    public <T> T tryWithLock(String lockName, Duration wait, Duration lease, Callable<T> task) {
        Objects.requireNonNull(task, "task 不能为空");
        long waitMs = (wait == null) ? 0L : Math.max(0L, wait.toMillis());
        long leaseMs = (lease == null) ? 30_000L : Math.max(1L, lease.toMillis());

        String lockKey = redisKeySupport.name(NS, lockName);
        RLock lock = redisson.getLock(lockKey);

        boolean locked;
        try {
            locked = lock.tryLock(waitMs, leaseMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return null;
        }
        if (!locked) {
            return null;
        }
        try {
            return call(task);
        } finally {
            safeUnlock(lock);
        }
    }

    private <T> T call(Callable<T> task) {
        try {
            return task.call();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("分布式锁任务执行失败", e);
        }
    }

    private void safeUnlock(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception _) {
            // ignore
        }
    }
}