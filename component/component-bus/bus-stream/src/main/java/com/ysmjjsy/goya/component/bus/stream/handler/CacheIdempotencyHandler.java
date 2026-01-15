package com.ysmjjsy.goya.component.bus.stream.handler;

import com.ysmjjsy.goya.component.bus.stream.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.cache.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>基于缓存的幂等性处理器</p>
 * <p>使用 ICacheService 存储已处理的事件标识</p>
 * <p>支持原子操作（使用本地锁配合缓存操作，保证单 JVM 内的原子性）</p>
 * <p><strong>设计说明：</strong></p>
 * <ul>
 *   <li>为什么使用本地锁而非分布式锁：component-bus 不直接依赖 redis-boot-starter，只依赖 component-cache 模块。
 *       本地锁配合 ICacheService 可以在单 JVM 内保证原子性，满足大多数场景需求。</li>
 *   <li>LRU 锁映射的限制：锁映射使用 LRU 机制，最大大小为 1000。超过时自动移除最旧的条目（仅当锁未被持有时）。
 *       这意味着在高并发场景下，锁映射大小可能超过 1000，但这是可接受的权衡。</li>
 *   <li>跨 JVM 场景：如果需要在多个 JVM 实例间保证幂等性，需要确保 ICacheService 的底层实现（如 Redis）支持分布式操作。</li>
 * </ul>
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
 */
@Slf4j
@RequiredArgsConstructor
public class CacheIdempotencyHandler implements IIdempotencyHandler {

    private final RedisCacheService redisCacheService;
    private final BusProperties busProperties;

    /**
     * 本地锁映射（基于幂等键的锁）
     * <p>用于保证单 JVM 内的原子性</p>
     * <p>Key: 幂等键，Value: 对应的锁</p>
     * <p>使用 LRU 缓存机制，限制最大大小为 1000</p>
     * <p>使用 Collections.synchronizedMap 包装，确保线程安全</p>
     */
    private final Map<String, ReentrantLock> lockMap = Collections.synchronizedMap(
            createLRULockMap(1000)
    );

    /**
     * 创建 LRU 锁映射
     * <p>限制最大大小为 maxSize，超过时自动移除最旧的条目</p>
     *
     * @param maxSize 最大大小
     * @return LRU 锁映射
     */
    private Map<String, ReentrantLock> createLRULockMap(int maxSize) {
        return new LinkedHashMap<String, ReentrantLock>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ReentrantLock> eldest) {
                // 当大小超过 maxSize 时，移除最旧的条目
                if (size() > maxSize) {
                    ReentrantLock lock = eldest.getValue();
                    // 只有在锁没有被任何线程持有时才移除
                    if (!lock.isLocked() && lock.getQueueLength() == 0) {
                        log.trace("[Goya] |- component [bus] CacheIdempotencyHandler |- removing lock for idempotency key: [{}]",
                                eldest.getKey());
                        return true;
                    }
                    // 如果锁正在被使用，不移除（可能导致大小超过 maxSize，但这是可接受的）
                }
                return false;
            }
        };
    }

    @Override
    public boolean checkAndSet(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // 如果没有幂等键，允许处理
            return true;
        }

        String cacheName = busProperties.idempotency().cacheName();
        Duration ttl = busProperties.idempotency().ttl();

        // 检查是否已存在
        String cached = redisCacheService.get(cacheName, idempotencyKey);
        if (cached != null) {
            log.debug("[Goya] |- component [bus] CacheIdempotencyHandler |- event already processed: [{}]", idempotencyKey);
            return false;
        }

        // 设置幂等键
        redisCacheService.put(cacheName, idempotencyKey, "processed", ttl);
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
        // lockMap 已经使用 Collections.synchronizedMap 包装，确保线程安全
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
                String cached = redisCacheService.get(cacheName, idempotencyKey);
                if (cached != null) {
                    log.debug("[Goya] |- component [bus] CacheIdempotencyHandler |- event already processed (atomic): [{}]",
                            idempotencyKey);
                    return false;
                }

                // 设置幂等键
                redisCacheService.put(cacheName, idempotencyKey, "processed", ttl);
                log.trace("[Goya] |- component [bus] CacheIdempotencyHandler |- set idempotency key (atomic): [{}]",
                        idempotencyKey);
                return true;
            } finally {
                // 释放锁
                lock.unlock();
                
                // 尝试清理锁映射（如果锁没有被其他线程持有）
                // LRU 机制会自动处理，但这里可以主动清理以释放内存
                if (!lock.isLocked() && lock.getQueueLength() == 0) {
                    // 锁没有被持有且没有等待的线程，可以安全移除
                    lockMap.remove(idempotencyKey);
                    log.trace("[Goya] |- component [bus] CacheIdempotencyHandler |- removed lock for idempotency key: [{}]",
                            idempotencyKey);
                }
                
                // 监控锁映射大小
                int lockMapSize = lockMap.size();
                if (lockMapSize > 500) {
                    log.warn("[Goya] |- component [bus] CacheIdempotencyHandler |- lock map size [{}] exceeds warning threshold (500). " +
                                    "This may indicate high concurrency or memory pressure.",
                            lockMapSize);
                }
            }
        } catch (Exception e) {
            log.warn("[Goya] |- component [bus] CacheIdempotencyHandler |- failed to use atomic operation, " +
                    "fallback to non-atomic operation: {}", e.getMessage());
            return checkAndSet(idempotencyKey);
        }
    }
}

