package com.ysmjjsy.goya.component.core.cache;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:17
 */
public interface CacheService {

    /**
     * 获取缓存值
     *
     * <p>从缓存中获取指定 key 的值，如果不存在则返回 null。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存值，如果不存在则返回 null
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K, V> V get(String cacheName, K key);

    /**
     * 写入缓存
     *
     * <p>将键值对写入缓存，使用配置的默认 TTL。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值（可以为 null，如果配置允许）
     * @param <K>       键类型
     * @param <V>       值类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     * @throws IllegalArgumentException 如果 value 为 null 且配置不允许 null 值
     */
    <K, V> void put(String cacheName, K key, V value);

    /**
     * 删除缓存
     *
     * <p>从缓存中移除指定 key 的数据。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K> void delete(String cacheName, K key);

    /**
     * 检查缓存是否存在
     *
     * <p>检查指定 key 是否存在于缓存中。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return true 如果存在，false 如果不存在
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K> boolean exists(String cacheName, K key);
}
