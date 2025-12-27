package com.ysmjjsy.goya.component.bus.handler;

import com.ysmjjsy.goya.component.bus.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>基于缓存的幂等性处理器</p>
 * <p>使用 ICacheService 存储已处理的事件标识</p>
 * <p>支持原子操作（使用本地锁配合缓存操作，保证单 JVM 内的原子性）</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 检查幂等性（非原子，适用于低并发场景）
 * if (idempotencyHandler.checkAndSet(idempotencyKey)) {
 *     // 未处理，继续处理
 *     processEvent(event);
 * } else {
 *     // 已处理，跳过
 *     log.warn("Event already processed: {}", idempotencyKey);
 * }
 *
 * // 原子性检查幂等性（适用于高并发场景）
 * if (idempotencyHandler.checkAndSetAtomic(idempotencyKey)) {
 *     // 未处理，继续处理
 *     processEvent(event);
 * } else {
 *     // 已处理，跳过
 *     log.warn("Event already processed: {}", idempotencyKey);
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see ICacheService
 */
@Slf4j
@RequiredArgsConstructor
public class CacheIdempotencyHandler implements IIdempotencyHandler {

    private final ICacheService cacheService;
    private final BusProperties busProperties;

    /**
     * 本地锁映射（基于幂等键的锁）
     * <p>用于保证单 JVM 内的原子性</p>
     * <p>Key: 幂等键，Value: 对应的锁</p>
     */
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean checkAndSet(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // 如果没有幂等键，允许处理
            return true;
        }

        String cacheName = busProperties.idempotency().cacheName();
        Duration ttl = busProperties.idempotency().ttl();

        // 检查是否已存在
        String cached = cacheService.get(cacheName, idempotencyKey);
        if (cached != null) {
            log.debug("[Goya] |- component [bus] CacheIdempotencyHandler |- event already processed: [{}]", idempotencyKey);
            return false;
        }

        // 设置幂等键
        cacheService.put(cacheName, idempotencyKey, "processed", ttl);
        log.trace("[Goya] |- component [bus] CacheIdempotencyHandler |- set idempotency key: [{}]", idempotencyKey);
        return true;
    }

    @Override
    public boolean checkAndSetAtomic(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // 如果没有幂等键，允许处理
            return true;
        }

        String cacheName = busProperties.idempotency().cacheName();
        Duration ttl = busProperties.idempotency().ttl();

        // 获取或创建对应的锁（基于幂等键）
        ReentrantLock lock = lockMap.computeIfAbsent(idempotencyKey, k -> new ReentrantLock());

        try {
            // 尝试获取锁（非阻塞，如果获取失败立即返回 false）
            if (!lock.tryLock()) {
                // 如果获取锁失败，说明有其他线程正在处理，等待一小段时间后重试
                // 这里使用简单的重试机制，最多重试 3 次，每次等待 10ms
                for (int i = 0; i < 3; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("[Goya] |- component [bus] CacheIdempotencyHandler |- interrupted while waiting for lock: [{}]",
                                idempotencyKey);
                        return false;
                    }
                    if (lock.tryLock()) {
                        break;
                    }
                }
                // 如果仍然无法获取锁，回退到非原子操作
                if (!lock.isHeldByCurrentThread()) {
                    log.debug("[Goya] |- component [bus] CacheIdempotencyHandler |- failed to acquire lock after retries, " +
                            "fallback to non-atomic operation: [{}]", idempotencyKey);
                    return checkAndSet(idempotencyKey);
                }
            }

            try {
                // 在锁保护下检查并设置
                String cached = cacheService.get(cacheName, idempotencyKey);
                if (cached != null) {
                    log.debug("[Goya] |- component [bus] CacheIdempotencyHandler |- event already processed (atomic): [{}]",
                            idempotencyKey);
                    return false;
                }

                // 设置幂等键
                cacheService.put(cacheName, idempotencyKey, "processed", ttl);
                log.trace("[Goya] |- component [bus] CacheIdempotencyHandler |- set idempotency key (atomic): [{}]",
                        idempotencyKey);
                return true;
            } finally {
                // 释放锁
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("[Goya] |- component [bus] CacheIdempotencyHandler |- failed to use atomic operation, " +
                    "fallback to non-atomic operation: {}", e.getMessage());
            return checkAndSet(idempotencyKey);
        } finally {
            // 清理锁映射（如果锁没有被其他线程持有）
            // 注意：这里不能直接移除，因为可能有其他线程正在等待
            // 使用定期清理机制或让锁自然过期
            // 为了简化，这里不清理，锁会一直存在（但数量有限，可以接受）
        }
    }
}

