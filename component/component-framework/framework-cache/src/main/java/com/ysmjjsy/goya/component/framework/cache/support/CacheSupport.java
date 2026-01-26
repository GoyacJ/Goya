package com.ysmjjsy.goya.component.framework.cache.support;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * <p>缓存支持</p>
 *
 * @author goya
 * @since 2026/1/26 14:41
 */
@Slf4j
public abstract class CacheSupport<K, V> {

    @Getter
    protected final String cacheName;

    @Autowired
    protected CacheService cacheService;

    @Getter
    @Setter
    protected Duration expire;

    protected CacheSupport(String cacheName) {
        this.cacheName = cacheName;
    }

    protected CacheSupport(String cacheName, Duration expire) {
        this.cacheName = cacheName;
        this.expire = expire;
    }

    /**
     * 获取缓存
     *
     * @param key key
     * @return value
     */
    public V get(K key) {
        return cacheService.get(cacheName, key);
    }

    /**
     * 写入缓存
     *
     * @param key   key
     * @param value value
     */
    public void put(K key, V value) {
        cacheService.put(cacheName, key, value, expire);
    }

    /**
     * 写入缓存
     *
     * @param key    key
     * @param value  value
     * @param expire expire
     */
    public void put(K key, V value, Duration expire) {
        cacheService.put(cacheName, key, value, Objects.isNull(expire) ? this.expire : expire);
    }

    /**
     * 删除缓存
     *
     * @param key key
     */
    public void delete(K key) {
        cacheService.delete(cacheName, key);
    }

    /**
     * 是否存在
     *
     * @param key key
     * @return 是否存在
     */
    public boolean exists(K key) {
        return cacheService.exists(cacheName, key);
    }

    /**
     * 判断是否相等
     *
     * @param key  key
     * @param newV value
     * @return 是否相等
     */
    public boolean checkEquals(K key, V newV) {
        V origin = get(key);
        return ObjectUtils.nullSafeEquals(origin, newV);
    }

}
