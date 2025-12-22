package com.ysmjjsy.goya.component.cache.service;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>缓存通用能力</p>
 *
 * @author goya
 * @since 2025/12/21 22:47
 */
public interface ICacheService {

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @return value
     */
    <K, V> V get(String cacheName, @NotBlank K key);

    /**
     * 根据 key 获取缓存值，如果不存在，则使用 {@code mappingFunction} 计算并缓存。
     *
     * @param cacheName       缓存名称
     * @param key             缓存 key
     * @param mappingFunction 值加载函数
     * @param <K>             key 类型
     * @param <V>             value 类型
     * @return 缓存中已有的值或计算得到的值
     */
    <K, V> V get(String cacheName, K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * 批量获取缓存中已存在的值，不存在的 key 不会返回。
     *
     * @param cacheName 缓存名称
     * @param keys      要获取的 key 集合
     * @param <K>       key 类型
     * @param <V>       value 类型
     * @return 包含已缓存 key 对应的 value 的 map，不可包含 null
     */
    <K, V> Map<K, @NonNull V> get(String cacheName, Set<? extends K> keys);

    /**
     * 批量获取缓存值，对于不存在的 key 使用 {@code mappingFunction} 计算并缓存。
     *
     * @param cacheName       缓存名称
     * @param keys            要获取的 key 集合
     * @param mappingFunction 批量计算值的函数
     * @param <K>             key 类型
     * @param <V>             value 类型
     * @return 包含所有 key 对应的 value 的 map
     */
    <K, V> Map<K, @NonNull V> get(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction
    );

    /**
     * 放入缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @param value     value
     */
    <K, V> void put(String cacheName, @NotBlank K key, V value);

    /**
     * 保存与Key对应的值
     *
     * @param cacheName 缓存名称
     * @param key       存储Key
     * @param value     与Key对应的value
     * @param duration  过期时间
     */
    <K, V> void put(String cacheName, K key, V value, Duration duration);

    /**
     * 删除缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @return 是否删除成功
     */
    <K> Boolean remove(String cacheName, @NotBlank K key);

    /**
     * 删除缓存
     *
     * @param cacheName 缓存名称
     * @param keys      keys
     */
    <K> void remove(String cacheName, @NotBlank Set<? extends K> keys);

    /**
     * 是否存在
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @return 是否存在
     */
    default <K, V> boolean containKey(String cacheName, K key) {
        V value = get(cacheName, key);
        return ObjectUtils.isNotEmpty(value);
    }

    /**
     * 如果key不存在则放入缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @param loader    loader
     * @return value
     */
    <K, V> V computeIfAbsent(String cacheName, K key, Function<K, V> loader);

    /**
     * 锁定值
     * <p>
     * 非堵塞的尝试获取一个锁，如果对应的key还没有锁，返回一个AutoReleaseLock，否则立即返回空。
     * 如果Cache实例是本地的，它是一个本地锁，在本JVM中有效；如果是redis等远程缓存，它是一个不十分严格的分布式锁。
     * 锁的超时时间由expire指定。多级缓存的情况会使用最后一级做tryLock操作。
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @param expire    expire
     */
    <K> void tryLock(String cacheName, K key, Duration expire);

    /**
     * 锁定并执行操作
     * <p>
     * 非堵塞的尝试获取一个锁，如果对应的key还没有锁，返回一个AutoReleaseLock，否则立即返回空。
     * 如果Cache实例是本地的，它是一个本地锁，在本JVM中有效；如果是redis等远程缓存，它是一个不十分严格的分布式锁。
     * 锁的超时时间由expire和timeUnit指定。多级缓存的情况会使用最后一级做tryLock操作。
     *
     * @param cacheName 缓存名称
     * @param key       存储Key
     * @param expire    过期时间{@link Duration}
     * @param action    需要执行的操作 {@link Runnable}
     * @return 是否执行成功
     * @see <a href="https://github.com/alibaba/jetcache/wiki/CacheAPI_CN">JetCache Wiki</a>
     */
    <K> boolean lockAndRun(String cacheName, K key, Duration expire, Runnable action);

}
