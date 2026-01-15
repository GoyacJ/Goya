package com.ysmjjsy.goya.component.cache.multilevel.ttl;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.core.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecificationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

/**
 * 默认降级策略实现
 *
 * <p>根据策略类型实现不同的降级行为。
 *
 * @author goya
 * @since 2025/12/26 14:56
 */
@Slf4j
public class DefaultFallbackStrategy implements FallbackStrategy {

    private final Type type;

    /**
     * 配置规范解析器
     */
    private final CacheSpecificationResolver specificationResolver;

    /**
     * 构造函数
     *
     * @param type                  降级策略类型
     * @param specificationResolver 配置规范解析器
     */
    public DefaultFallbackStrategy(Type type, CacheSpecificationResolver specificationResolver) {
        if (type == null) {
            throw new IllegalArgumentException("FallbackStrategy type cannot be null");
        }
        if (specificationResolver == null) {
            throw new IllegalArgumentException("CacheSpecificationResolver cannot be null");
        }
        this.type = type;
        this.specificationResolver = specificationResolver;
    }

    @Override
    public Cache.ValueWrapper onL2Failure(Object key, LocalCache l1, Exception exception) {
        // 快速失败：抛出异常
        return switch (type) {
            case DEGRADE_TO_L1 -> {
                // 降级到 L1：尝试从 L1 获取（即使可能已过期）
                try {
                    Cache.ValueWrapper l1Value = l1.get(key);
                    if (l1Value != null) {
                        log.debug("Degraded to L1 cache for key: {}", key);
                        yield l1Value;
                    }
                } catch (Exception e) {
                    log.warn("Failed to query L1 cache during fallback for key: {}", key, e);
                }
                // L1 也没有数据，返回 null（触发方法执行）
                yield null;
                // L1 也没有数据，返回 null（触发方法执行）
            }
            case FAIL_FAST -> throw new CacheException("L2 cache query failed for key: " + key, exception);
            case IGNORE -> {
                // 忽略错误：记录日志，返回 null（触发方法执行）
                log.warn("Ignoring L2 cache failure for key: {}", key, exception);
                yield null;
            }
        };
    }

    @Override
    public void onL2WriteFailure(Object key, Object value, LocalCache l1, Exception exception) {
        switch (type) {
            case DEGRADE_TO_L1:
                // 降级到 L1：继续写入 L1（保证当前节点可用）
                try {
                    // 从配置获取 L1 TTL
                    String cacheName = l1.getName();
                    CacheSpecification spec = specificationResolver.resolve(cacheName);
                    l1.put(key, value, spec.getLocalTtl());
                    log.debug("Degraded to L1 cache write for key: {}, cacheName: {}", key, cacheName);
                } catch (Exception e) {
                    log.error("Failed to write to L1 cache during fallback for key: {}", key, e);
                    // L1 写入也失败，记录错误但不抛出异常
                }
                break;

            case FAIL_FAST:
                // 快速失败：抛出异常
                throw new CacheException("L2 cache write failed for key: " + key, exception);

            case IGNORE:
                // 忽略错误：记录日志，继续执行
                log.warn("Ignoring L2 cache write failure for key: {}", key, exception);
                break;

            default:
                throw new IllegalStateException("Unknown fallback strategy type: " + type);
        }
    }
}


