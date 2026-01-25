package com.ysmjjsy.goya.component.framework.cache.caffeine;

import java.time.Duration;
import java.util.Objects;

/**
 * <p>Caffeine 内部存储值包装</p>
 *
 * <p>用于实现“每条记录 TTL”：expireAtNanos 由 put/getOrLoad 决定。</p>
 * <p>对外不可见：Spring Cache 与 CacheService 返回的仍然是原始 value。</p>
 *
 * @param value 实际值（可能为 Null 标记）
 * @param expireAtNanos 过期时间点（nanoTime 语义）；Long.MAX_VALUE 表示不过期
 *
 * @author goya
 * @since 2026/1/25 21:03
 */
public record CacheValue(Object value, long expireAtNanos) {

    /**
     * 计算过期时间点。
     *
     * @param nowNanos 当前 System.nanoTime()
     * @param ttl TTL（为空或非正表示不过期/交给默认 TTL 由上层决定）
     * @return expireAtNanos
     */
    public static long computeExpireAt(long nowNanos, Duration ttl) {
        if (ttl == null) {
            return Long.MAX_VALUE;
        }
        if (ttl.isZero()) {
            // 立即过期
            return nowNanos;
        }
        if (ttl.isNegative()) {
            return Long.MAX_VALUE;
        }
        long add = ttl.toNanos();
        if (add <= 0) {
            return Long.MAX_VALUE;
        }
        long r = nowNanos + add;
        // 溢出保护
        return r < 0 ? Long.MAX_VALUE : r;
    }

    /**
     * 创建包装值。
     *
     * @param value 值
     * @param expireAtNanos 过期点
     * @return CacheValue
     */
    public static CacheValue of(Object value, long expireAtNanos) {
        return new CacheValue(Objects.requireNonNullElse(value, NullValue.INSTANCE), expireAtNanos);
    }

    /**
     * Null 值标记。
     *
     * <p>避免 Caffeine 不允许存 null，且对外表现为 null。</p>
     */
    public enum NullValue {
        /** 单例 */
        INSTANCE
    }
}