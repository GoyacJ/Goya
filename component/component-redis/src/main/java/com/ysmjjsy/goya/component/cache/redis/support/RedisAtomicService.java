package com.ysmjjsy.goya.component.cache.redis.support;

import java.time.Duration;

/**
 * <p>Redis 原子计数服务</p>
 * <p>基于 Redisson {@code RAtomicLong}，用于：</p>
 * <ul>
 *   <li>分布式计数器</li>
 *   <li>简单发号器（注意业务是否需要更复杂的号段/雪花等）</li>
 *   <li>限流统计、PV/UV 计数</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 23:44
 */
public interface RedisAtomicService {

    /**
     * 获取当前值。
     *
     * @param name 计数器名
     * @return 当前值
     */
    long get(String name);

    /**
     * 设置值。
     *
     * @param name  计数器名
     * @param value 值
     */
    void set(String name, long value);

    /**
     * 自增并返回新值。
     *
     * @param name 计数器名
     * @return 新值
     */
    long incrementAndGet(String name);

    /**
     * 增加 delta 并返回新值。
     *
     * @param name  计数器名
     * @param delta 增量
     * @return 新值
     */
    long addAndGet(String name, long delta);

    /**
     * CAS 更新。
     *
     * @param name   计数器名
     * @param expect 期望值
     * @param update 更新值
     * @return 是否成功
     */
    boolean compareAndSet(String name, long expect, long update);

    /**
     * 设置过期时间。
     *
     * @param name 计数器名
     * @param ttl  生存时间（null 或 <=0 表示不设置）
     * @return 是否设置成功
     */
    boolean expire(String name, Duration ttl);

    /**
     * 删除计数器。
     *
     * @param name 计数器名
     * @return 是否删除成功
     */
    boolean delete(String name);
}
