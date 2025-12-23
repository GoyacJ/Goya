package com.ysmjjsy.goya.component.cache.service;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>L2 分布式缓存服务接口（SPI）</p>
 * <p>定义最小化的 L2 缓存能力，用于标识分布式缓存实现，如 Redis、MongoDB 等</p>
 * <p>注意：不继承 ICacheService，避免污染 Spring Bean 类型体系</p>
 *
 * <p>实现要求：</p>
 * <ul>
 *     <li>必须支持分布式环境</li>
 *     <li>必须线程安全</li>
 *     <li>必须支持分布式锁</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // Redis 实现
 * public class RedisCacheService extends AbstractCacheService implements IL2Cache {
 *     @Override
 *     public String getCacheType() {
 *         return "redis";
 *     }
 * }
 *
 * // MongoDB 实现
 * public class MongodbCacheService extends AbstractCacheService implements IL2Cache {
 *     @Override
 *     public String getCacheType() {
 *         return "mongodb";
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see RemoteCacheService
 * @see AbstractCacheService
 */
public interface IL2Cache {

    // ==================== 基础 CRUD ====================

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @return value
     */
    <K, V> V get(String cacheName, K key);

    /**
     * 根据 key 获取缓存值，如果不存在，则使用 {@code mappingFunction} 计算并缓存
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
     * 批量获取缓存中已存在的值，不存在的 key 不会返回
     *
     * @param cacheName 缓存名称
     * @param keys      要获取的 key 集合
     * @param <K>       key 类型
     * @param <V>       value 类型
     * @return 包含已缓存 key 对应的 value 的 map，不可包含 null
     */
    <K, V> Map<K, @NonNull V> get(String cacheName, Set<? extends K> keys);

    /**
     * 批量获取缓存值，对于不存在的 key 使用 {@code mappingFunction} 计算并缓存
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
    <K> Boolean remove(String cacheName, K key);

    /**
     * 批量删除缓存
     *
     * @param cacheName 缓存名称
     * @param keys      keys
     */
    <K> void remove(String cacheName, Set<? extends K> keys);

    /**
     * 如果key不存在则放入缓存
     *
     * @param cacheName 缓存名称
     * @param key       key
     * @param loader    loader
     * @return value
     */
    <K, V> V computeIfAbsent(String cacheName, K key, Function<K, V> loader);

    // ==================== 分布式锁 ====================

    /**
     * 锁定并执行操作
     * <p>非堵塞的尝试获取一个锁，如果对应的key还没有锁，返回一个AutoReleaseLock，否则立即返回空。</p>
     * <p>锁的超时时间由expire指定。</p>
     *
     * @param cacheName 缓存名称
     * @param key       存储Key
     * @param expire    过期时间{@link Duration}
     * @param action    需要执行的操作 {@link Runnable}
     * @return 是否执行成功
     */
    <K> boolean lockAndRun(String cacheName, K key, Duration expire, Runnable action);

    // ==================== 缓存穿透防护（布隆过滤器）====================

    /**
     * <p>判断 key 是否可能存在（布隆过滤器）</p>
     * <p>用于防止缓存穿透：快速判断不存在的 key，避免查询数据库</p>
     * <p>布隆过滤器特性：</p>
     * <ul>
     *     <li>返回 false：key 一定不存在</li>
     *     <li>返回 true：key 可能存在（存在误判率）</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return true 可能存在，false 一定不存在
     */
    <K> boolean mightContain(String cacheName, K key);

    /**
     * <p>添加 key 到布隆过滤器</p>
     * <p>在写入缓存时自动调用，用户通常不需要手动调用</p>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     */
    <K> void addToBloomFilter(String cacheName, K key);

    // ==================== L2 特性 ====================

    /**
     * 获取 L2 缓存类型标识
     *
     * @return 缓存类型，如 "redis", "mongodb"
     */
    String getCacheType();

    /**
     * 检查 L2 缓存是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 清空所有缓存（可选实现）
     * <p>用于管理和维护场景</p>
     */
    default void clearAll() {
        // 默认空实现，子类可覆盖
    }
}

