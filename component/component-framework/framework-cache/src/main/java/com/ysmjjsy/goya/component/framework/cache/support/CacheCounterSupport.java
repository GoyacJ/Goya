package com.ysmjjsy.goya.component.framework.cache.support;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * <p>计数操作实现</p>
 *
 * @author goya
 * @since 2026/1/26 15:39
 */
@Slf4j
public abstract class CacheCounterSupport extends CacheSupport<String, Long> {

    protected CacheCounterSupport(String cacheName) {
        super(cacheName);
    }

    protected CacheCounterSupport(String cacheName, Duration expire) {
        super(cacheName, expire);
    }

    /**
     * 自增 1（使用默认 ttlOnCreate）。
     *
     * @param key 计数 key
     * @return 自增后的值
     */
    public long incr(String key) {
        return incr(key, 1L, expire);
    }

    /**
     * 自增 delta（使用默认 ttlOnCreate）。
     *
     * @param key   计数 key
     * @param delta 增量
     * @return 自增后的值
     */
    public long incr(String key, long delta) {
        return incr(key, delta, expire);
    }

    /**
     * 自增 delta，并在首次创建时设置 TTL。
     *
     * <p>推荐用于“周期计数”场景（例如：每分钟/每天次数）。最佳实践是把周期编码到 key 中，
     * TTL 用来兜底清理：</p>
     * <pre>
     * sms_send:20260126:{phone}
     * login_fail:20260126:{userId}
     * </pre>
     *
     * @param key         计数 key
     * @param delta       增量
     * @param ttlOnCreate 首次创建时设置的 TTL；为 null 或 <=0 表示不设置 TTL（不推荐）
     * @return 自增后的值
     */
    public long incr(String key, long delta, Duration ttlOnCreate) {
        return cacheService.incrByWithTtlOnCreate(cacheName, key, delta, ttlOnCreate);
    }

    /**
     * 获取当前计数。
     *
     * @param key 计数 key
     * @return 当前计数；不存在返回 null
     */
    @Override
    public Long get(String key) {
        return cacheService.getCounter(cacheName, key);
    }

    /**
     * 重置计数（删除 key）。
     *
     * @param key 计数 key
     */
    public void reset(String key) {
        cacheService.resetCounter(cacheName, key);
    }
}
