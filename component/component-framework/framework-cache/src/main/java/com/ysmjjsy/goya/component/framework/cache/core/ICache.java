package com.ysmjjsy.goya.component.framework.cache.core;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:15
 */
public interface ICache<K, V> {

    /**
     * get value
     *
     * @param key key
     * @return value
     */
    V get(K key);

    /**
     * put cache
     *
     * @param key   key
     * @param value value
     */
    void put(K key, V value);

    /**
     * delete cache
     *
     * @param key key
     */
    void delete(K key);

    /**
     * exists key
     *
     * @param key key
     * @return value
     */
    boolean exists(K key);

    /**
     * 清空所有缓存
     */
    void clear();
}
