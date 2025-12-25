package com.ysmjjsy.goya.component.bus.handler;

import com.ysmjjsy.goya.component.bus.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>基于缓存的幂等性处理器</p>
 * <p>使用 ICacheService 存储已处理的事件标识</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 检查幂等性
 * if (idempotencyHandler.checkAndSet(idempotencyKey)) {
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

    @Override
    public boolean checkAndSet(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // 如果没有幂等键，允许处理
            return true;
        }

        String cacheName = busProperties.idempotency().cacheName();
        java.time.Duration ttl = busProperties.idempotency().ttl();

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
}

