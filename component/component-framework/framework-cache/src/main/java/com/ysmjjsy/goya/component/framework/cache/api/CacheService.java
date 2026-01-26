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
     * @param key       键
     * @param <T>       泛型
     * @return 命中返回值，否则返回 null
     */
    <T> T get(String cacheName, Object key);

    /**
     * 获取缓存值。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param type      期望类型
     * @param <T>       泛型
     * @return 命中返回值，否则返回 null
     */
    <T> T get(String cacheName, Object key, Class<T> type);

    /**
     * 获取缓存值（Optional 形式）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param <T>       泛型
     * @return Optional
     */
    <T> Optional<T> getOptional(String cacheName, Object key);

    /**
     * 获取缓存值（Optional 形式）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param type      期望类型
     * @param <T>       泛型
     * @return Optional
     */
    <T> Optional<T> getOptional(String cacheName, Object key, Class<T> type);

    /**
     * 写入缓存（使用该 cacheName 的默认 TTL）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param value     值
     */
    void put(String cacheName, Object key, Object value);

    /**
     * 写入缓存（为该条记录指定 TTL）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param value     值
     * @param ttl       过期时间（为空或非正数表示使用默认 TTL；Duration.ZERO 表示立即过期）
     */
    void put(String cacheName, Object key, Object value, Duration ttl);

    /**
     * 删除缓存项。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @return 是否实际删除（命中才为 true）
     */
    boolean delete(String cacheName, Object key);

    /**
     * 清空某个缓存区域。
     *
     * @param cacheName 缓存名
     */
    void clear(String cacheName);

    /**
     * 是否存在key
     *
     * @param cacheName cacheName
     * @param key       key
     * @return 是否存在
     */
    boolean exists(String cacheName, Object key);

    /**
     * 批量获取。
     *
     * @param cacheName 缓存名
     * @param keys      键集合
     * @return map：key -> value（仅包含命中的项）
     */
    Map<Object, Object> getAll(String cacheName, Collection<?> keys);

    /**
     * 获取或加载（缓存未命中则调用 loader 计算并写入缓存）。
     *
     * <p>实现应保证同一 key 的并发加载尽可能收敛（避免击穿）。</p>
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param loader    加载器
     * @param <T>       泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Supplier<T> loader);

    /**
     * 获取或加载（缓存未命中则调用 loader 计算并写入缓存）。
     *
     * <p>实现应保证同一 key 的并发加载尽可能收敛（避免击穿）。</p>
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param type      期望类型
     * @param loader    加载器
     * @param <T>       泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader);

    /**
     * 获取或加载（并为该条记录指定 TTL）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param ttl       TTL
     * @param loader    加载器
     * @param <T>       泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Duration ttl, Supplier<T> loader);

    /**
     * 获取或加载（并为该条记录指定 TTL）。
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param type      期望类型
     * @param ttl       TTL
     * @param loader    加载器
     * @param <T>       泛型
     * @return 值
     */
    <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader);

    /**
     * 原子写入：仅当 key 不存在时写入，并设置 TTL。
     *
     * <p>典型用途：</p>
     * <ul>
     *   <li>幂等（一次性消费 token）</li>
     *   <li>分布式互斥（轻量锁/占位）</li>
     *   <li>防重提交</li>
     * </ul>
     *
     * <p><b>原子性要求：</b>在 Redisson/Redis 实现中必须满足分布式原子性；在 Caffeine 实现中只保证单 JVM 原子性。</p>
     *
     * @param cacheName 缓存命名空间
     * @param key       缓存 key
     * @param value     写入值（业务可用常量 "1"）
     * @param expire    TTL；不建议为 null（幂等 key 不设 TTL 容易产生永久脏数据）
     * @param <K>       key 类型
     * @param <V>       value 类型
     * @return true 表示本次成功写入（之前不存在）；false 表示之前已存在
     */
    <K, V> boolean putIfAbsent(String cacheName, K key, V value, Duration expire);

    /**
     * 计数：原子自增，并在“首次创建”时设置 TTL。
     *
     * <p>为什么强调“首次创建才设置 TTL”？</p>
     * <ul>
     *   <li>常见错误是 INCR 后再 EXPIRE，非原子会导致 key 永不过期，造成计数/限流失效。</li>
     *   <li>该方法要求把“自增 + 首次设置 TTL”合成一个原子操作。</li>
     * </ul>
     *
     * <p><b>原子性要求：</b>Redisson/Redis 实现必须满足分布式原子性；Caffeine 实现只保证单 JVM。</p>
     *
     * @param cacheName   缓存命名空间
     * @param key         计数 key
     * @param delta       增量（可为负数，但需业务自行约束）
     * @param ttlOnCreate 首次创建时设置的 TTL；为 null 或 <=0 表示不设置 TTL（不推荐）
     * @param <K>         key 类型
     * @return 自增后的值
     */
    <K> long incrByWithTtlOnCreate(String cacheName, K key, long delta, Duration ttlOnCreate);

    /**
     * 获取计数值。
     *
     * @param cacheName 缓存命名空间
     * @param key       计数 key
     * @param <K>       key 类型
     * @return 当前计数；不存在返回 null
     */
    <K> Long getCounter(String cacheName, K key);

    /**
     * 重置计数（删除 key）。
     *
     * @param cacheName 缓存命名空间
     * @param key       计数 key
     * @param <K>       key 类型
     */
    <K> void resetCounter(String cacheName, K key);
}
