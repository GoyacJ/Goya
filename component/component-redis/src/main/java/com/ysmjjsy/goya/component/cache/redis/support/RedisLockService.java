package com.ysmjjsy.goya.component.cache.redis.support;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * <p>Redis 分布式锁服务。</p>
 * <p>封装 Redisson 锁能力，统一命名空间 + 租户隔离。</p>
 *
 * @author goya
 * @since 2026/1/25 23:32
 */
public interface RedisLockService {

    /**
     * 执行互斥逻辑（阻塞获取锁）。
     *
     * @param lockName 锁名
     * @param lease    锁持有时间（到期自动释放）
     * @param task     任务
     * @param <T>      返回类型
     * @return 执行结果
     */
    <T> T withLock(String lockName, Duration lease, Callable<T> task);

    /**
     * 尝试执行互斥逻辑（带等待时间）。
     *
     * @param lockName 锁名
     * @param wait     获取锁最大等待时间
     * @param lease    锁持有时间（到期自动释放）
     * @param task     任务
     * @param <T>      返回类型
     * @return 获取锁成功才执行，否则返回 null
     */
    <T> T tryWithLock(String lockName, Duration wait, Duration lease, Callable<T> task);
}
