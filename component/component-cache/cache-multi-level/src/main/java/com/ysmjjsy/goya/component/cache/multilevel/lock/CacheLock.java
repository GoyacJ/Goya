package com.ysmjjsy.goya.component.cache.multilevel.lock;

import java.util.concurrent.TimeUnit;

/**
 * <p>缓存分布式锁接口</p>
 * <p>用于缓存击穿防护，防止多个线程同时访问数据库</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * if (cacheLock.tryLock(cacheName, key, waitTime, leaseTime, TimeUnit.SECONDS)) {
 *     try {
 *         // 加载数据
 *     } finally {
 *         cacheLock.unlock(cacheName, key);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15
 */
public interface CacheLock {

    /**
     * 尝试获取分布式锁
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param waitTime  等待时间
     * @param leaseTime 锁的持有时间（-1 表示不自动释放）
     * @param unit      时间单位
     * @param <K>       键类型
     * @return true 如果获取成功，false 如果获取失败
     * @throws InterruptedException 如果等待过程中被中断
     */
    <K> boolean tryLock(String cacheName, K key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    /**
     * 释放分布式锁
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     */
    <K> void unlock(String cacheName, K key);
}
