package com.ysmjjsy.goya.component.cache.multilevel.lock;

import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>Redis 缓存分布式锁实现</p>
 * <p>基于 RedisService 实现缓存分布式锁，用于缓存击穿防护</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheLock implements CacheLock {

    /**
     * Redis 服务
     */
    private final RedisService redisService;

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer cacheKeySerializer;

    @Override
    public <K> boolean tryLock(String cacheName, K key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String lockKey = buildLockKey(cacheName, key);
            boolean acquired = redisService.tryLock(lockKey, waitTime, leaseTime, unit);
            if (log.isTraceEnabled()) {
                log.trace("Try lock: cacheName={}, key={}, acquired={}", cacheName, key, acquired);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while trying to acquire lock: cacheName={}, key={}", cacheName, key);
            throw e;
        } catch (Exception e) {
            log.error("Failed to acquire lock: cacheName={}, key={}", cacheName, key, e);
            return false;
        }
    }

    @Override
    public <K> void unlock(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String lockKey = buildLockKey(cacheName, key);
            redisService.unlock(lockKey);
            if (log.isTraceEnabled()) {
                log.trace("Unlock: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to unlock: cacheName={}, key={}", cacheName, key, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 构建锁的 key
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return 锁的 key
     */
    private <K> String buildLockKey(String cacheName, K key) {
        return cacheKeySerializer.buildKey("cache:lock:", cacheName, key);
    }
}
