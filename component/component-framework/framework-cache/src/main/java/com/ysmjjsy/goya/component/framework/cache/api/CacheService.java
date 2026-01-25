package com.ysmjjsy.goya.component.framework.cache.api;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>通用缓存服务接口</p>
 *
 * <p>设计目标：</p>
 * <ul>
 *   <li>业务代码依赖该接口，不绑定具体缓存产品</li>
 *   <li>支持常用能力：读/写/删除/批量/带加载/单条 TTL</li>
 *   <li>实现层可基于 Caffeine/Redis 等扩展</li>
 * </ul>
 *
 * <p>cacheName 用于区分不同缓存区域（不同 TTL、不同容量、不同统计策略）。</p>
 *
 * @author goya
 * @since 2026/1/12 22:17
 */
public interface CacheService {

    /**
     * 获取缓存值。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param type 期望类型
     * @param <T> 泛型
     * @return 命中返回值，否则返回 null
     */
    <T> T get(String cacheName, Object key, Class<T> type);

    /**
     * 获取缓存值（Optional 形式）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param type 期望类型
     * @param <T> 泛型
     * @return Optional
     */
    <T> Optional<T> getOptional(String cacheName, Object key, Class<T> type);

    /**
     * 写入缓存（使用该 cacheName 的默认 TTL）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param value 值
     */
    void put(String cacheName, Object key, Object value);

    /**
     * 写入缓存（为该条记录指定 TTL）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param value 值
     * @param ttl 过期时间（为空或非正数表示使用默认 TTL；Duration.ZERO 表示立即过期）
     */
    void put(String cacheName, Object key, Object value, Duration ttl);

    /**
     * 删除缓存项。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @return 是否实际删除（命中才为 true）
     */
    boolean evict(String cacheName, Object key);

    /**
     * 清空某个缓存区域。
     *
     * @param cacheName 缓存名
     */
    void clear(String cacheName);

    /**
     * 批量获取。
     *
     * @param cacheName 缓存名
     * @param keys 键集合
     * @return map：key -> value（仅包含命中的项）
     */
    Map<Object, Object> getAll(String cacheName, Collection<?> keys);

    /**
     * 获取或加载（缓存未命中则调用 loader 计算并写入缓存）。
     *
     * <p>实现应保证同一 key 的并发加载尽可能收敛（避免击穿）。</p>
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param type 期望类型
     * @param loader 加载器
     * @param <T> 泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader);

    /**
     * 获取或加载（并为该条记录指定 TTL）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @param type 期望类型
     * @param ttl TTL
     * @param loader 加载器
     * @param <T> 泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader);
}
