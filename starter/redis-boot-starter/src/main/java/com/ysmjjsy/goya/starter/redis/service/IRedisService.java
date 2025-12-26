package com.ysmjjsy.goya.starter.redis.service;

import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 服务接口
 *
 * <p>提供 Redis 特有和高级功能，封装 Redisson API，简化日常开发使用。
 * 支持分布式锁、发布订阅、原子操作、过期时间管理、管道操作和高级数据结构。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>提供分布式锁功能</li>
 *   <li>提供发布订阅功能</li>
 *   <li>提供原子操作（incr/decr/append 等）</li>
 *   <li>提供过期时间管理</li>
 *   <li>提供管道操作（批量执行）</li>
 *   <li>提供高级数据结构操作（List/Set/SortedSet/Hash）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>需要分布式锁时</li>
 *   <li>需要发布订阅消息时</li>
 *   <li>需要原子操作时</li>
 *   <li>需要管理过期时间时</li>
 *   <li>需要批量操作时</li>
 *   <li>需要使用高级数据结构时</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 15:00
 */
public interface IRedisService {

    // ========== 分布式锁 ==========

    /**
     * 获取分布式锁
     *
     * <p>获取指定 key 的分布式锁实例。需要手动调用 lock() 或 tryLock() 方法。
     *
     * @param key 锁的 key
     * @return 分布式锁实例
     * @throws IllegalArgumentException 如果 key 为 null
     */
    RLock getLock(String key);

    /**
     * 尝试获取分布式锁
     *
     * <p>在指定时间内尝试获取锁，如果获取成功则返回 true，否则返回 false。
     *
     * @param key 锁的 key
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间（-1 表示不自动释放）
     * @param unit 时间单位
     * @return true 如果获取成功，false 如果获取失败
     * @throws IllegalArgumentException 如果 key 为 null 或参数无效
     * @throws InterruptedException 如果等待过程中被中断
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    /**
     * 释放分布式锁
     *
     * <p>释放指定 key 的分布式锁。
     *
     * @param key 锁的 key
     * @throws IllegalArgumentException 如果 key 为 null
     */
    void unlock(String key);

    // ========== 发布订阅 ==========

    /**
     * 发布消息
     *
     * <p>向指定频道发布消息。
     *
     * @param channel 频道名称
     * @param message 消息内容
     * @throws IllegalArgumentException 如果 channel 或 message 为 null
     */
    void publish(String channel, Object message);

    /**
     * 订阅频道
     *
     * <p>订阅指定频道，接收消息。
     *
     * @param channel 频道名称
     * @param listener 消息监听器
     * @param <T> 消息类型
     * @return 订阅 ID（用于取消订阅）
     * @throws IllegalArgumentException 如果 channel 或 listener 为 null
     */
    <T> int subscribe(String channel, MessageListener<T> listener);

    /**
     * 取消订阅
     *
     * <p>取消指定频道的订阅。
     *
     * @param channel 频道名称
     * @param listenerId 订阅 ID
     * @throws IllegalArgumentException 如果 channel 为 null
     */
    void unsubscribe(String channel, int listenerId);

    // ========== 原子操作 ==========

    /**
     * 递增
     *
     * <p>将指定 key 的值递增 1，如果 key 不存在则初始化为 0 后递增。
     *
     * @param key 键
     * @return 递增后的值
     * @throws IllegalArgumentException 如果 key 为 null
     */
    long increment(String key);

    /**
     * 递减
     *
     * <p>将指定 key 的值递减 1，如果 key 不存在则初始化为 0 后递减。
     *
     * @param key 键
     * @return 递减后的值
     * @throws IllegalArgumentException 如果 key 为 null
     */
    long decrement(String key);

    /**
     * 递增指定值
     *
     * <p>将指定 key 的值递增 delta。
     *
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     * @throws IllegalArgumentException 如果 key 为 null
     */
    long incrementBy(String key, long delta);

    /**
     * 追加字符串
     *
     * <p>将指定字符串追加到 key 的值后面。
     *
     * @param key 键
     * @param value 要追加的字符串
     * @return 追加后的字符串长度
     * @throws IllegalArgumentException 如果 key 或 value 为 null
     */
    int append(String key, String value);

    // ========== 过期时间管理 ==========

    /**
     * 设置过期时间
     *
     * <p>为指定 key 设置过期时间。
     *
     * @param key 键
     * @param time 过期时间
     * @param unit 时间单位
     * @return true 如果设置成功，false 如果 key 不存在
     * @throws IllegalArgumentException 如果 key 为 null 或参数无效
     */
    boolean expire(String key, long time, TimeUnit unit);

    /**
     * 获取过期时间
     *
     * <p>获取指定 key 的剩余过期时间。
     *
     * @param key 键
     * @return 剩余过期时间（毫秒），-1 表示永不过期，-2 表示 key 不存在
     * @throws IllegalArgumentException 如果 key 为 null
     */
    long getExpire(String key);

    /**
     * 移除过期时间
     *
     * <p>移除指定 key 的过期时间，使其永不过期。
     *
     * @param key 键
     * @return true 如果移除成功，false 如果 key 不存在或没有过期时间
     * @throws IllegalArgumentException 如果 key 为 null
     */
    boolean persist(String key);

    // ========== 管道操作 ==========

    /**
     * 执行管道操作
     *
     * <p>批量执行多个 Redis 操作，提升性能。
     *
     * @param operations 操作列表（每个操作返回 RFuture）
     * @param <T> 返回值类型
     * @return 操作结果列表（按操作顺序）
     * @throws IllegalArgumentException 如果 operations 为 null
     * @throws RuntimeException 如果管道执行失败
     */
    <T> List<T> pipeline(List<Function<RBatch, RFuture<T>>> operations);

    // ========== 高级数据结构 - List ==========

    /**
     * 获取 List
     *
     * <p>获取指定 key 的 List 数据结构。
     *
     * @param key 键
     * @param <T> 元素类型
     * @return List 实例
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <T> RList<T> getList(String key);

    /**
     * 向 List 添加元素
     *
     * <p>向指定 key 的 List 末尾添加元素。
     *
     * @param key 键
     * @param values 要添加的元素
     * @return 添加后的 List 长度
     * @throws IllegalArgumentException 如果 key 为 null 或 values 为 null
     */
    int addToList(String key, Object... values);

    /**
     * 从 List 获取元素
     *
     * <p>获取指定 key 的 List 中指定索引的元素。
     *
     * @param key 键
     * @param index 索引
     * @param <T> 元素类型
     * @return 元素值，如果索引超出范围则返回 null
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <T> T getFromList(String key, int index);

    /**
     * 获取 List 长度
     *
     * <p>获取指定 key 的 List 的长度。
     *
     * @param key 键
     * @return List 长度，如果 key 不存在则返回 0
     * @throws IllegalArgumentException 如果 key 为 null
     */
    int getListSize(String key);

    // ========== 高级数据结构 - Set ==========

    /**
     * 获取 Set
     *
     * <p>获取指定 key 的 Set 数据结构。
     *
     * @param key 键
     * @param <T> 元素类型
     * @return Set 实例
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <T> RSet<T> getSet(String key);

    /**
     * 向 Set 添加元素
     *
     * <p>向指定 key 的 Set 添加元素。
     *
     * @param key 键
     * @param values 要添加的元素
     * @return 添加的元素数量（不包括已存在的元素）
     * @throws IllegalArgumentException 如果 key 为 null 或 values 为 null
     */
    int addToSet(String key, Object... values);

    /**
     * 检查 Set 是否包含元素
     *
     * <p>检查指定 key 的 Set 是否包含指定元素。
     *
     * @param key 键
     * @param value 元素值
     * @return true 如果包含，false 如果不包含或 key 不存在
     * @throws IllegalArgumentException 如果 key 或 value 为 null
     */
    boolean containsInSet(String key, Object value);

    /**
     * 获取 Set 大小
     *
     * <p>获取指定 key 的 Set 的大小。
     *
     * @param key 键
     * @return Set 大小，如果 key 不存在则返回 0
     * @throws IllegalArgumentException 如果 key 为 null
     */
    int getSetSize(String key);

    // ========== 高级数据结构 - SortedSet ==========

    /**
     * 获取 SortedSet
     *
     * <p>获取指定 key 的 SortedSet 数据结构。
     *
     * @param key 键
     * @param <T> 元素类型
     * @return SortedSet 实例
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <T> RSortedSet<T> getSortedSet(String key);

    /**
     * 向 SortedSet 添加元素
     *
     * <p>向指定 key 的 SortedSet 添加元素（带分数）。
     *
     * @param key 键
     * @param score 分数
     * @param value 元素值
     * @return true 如果添加成功，false 如果元素已存在
     * @throws IllegalArgumentException 如果 key 或 value 为 null
     */
    boolean addToSortedSet(String key, double score, Object value);

    /**
     * 获取 SortedSet 排名范围内的元素
     *
     * <p>获取指定 key 的 SortedSet 中排名在指定范围内的元素。
     *
     * @param key 键
     * @param startRank 起始排名（包含）
     * @param endRank 结束排名（包含）
     * @param <T> 元素类型
     * @return 元素集合
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <T> Set<T> getSortedSetRange(String key, int startRank, int endRank);

    // ========== 高级数据结构 - Hash ==========

    /**
     * 获取 Hash
     *
     * <p>获取指定 key 的 Hash 数据结构。
     *
     * @param key 键
     * @param <K> 字段类型
     * @param <V> 值类型
     * @return Hash 实例
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <K, V> RMap<K, V> getHash(String key);

    /**
     * 向 Hash 设置字段
     *
     * <p>向指定 key 的 Hash 设置字段值。
     *
     * @param key 键
     * @param field 字段名
     * @param value 字段值
     * @throws IllegalArgumentException 如果 key、field 或 value 为 null
     */
    void setHashField(String key, Object field, Object value);

    /**
     * 从 Hash 获取字段值
     *
     * <p>从指定 key 的 Hash 获取指定字段的值。
     *
     * @param key 键
     * @param field 字段名
     * @param <T> 值类型
     * @return 字段值，如果字段不存在则返回 null
     * @throws IllegalArgumentException 如果 key 或 field 为 null
     */
    <T> T getHashField(String key, Object field);

    /**
     * 获取 Hash 所有字段和值
     *
     * <p>获取指定 key 的 Hash 的所有字段和值。
     *
     * @param key 键
     * @param <K> 字段类型
     * @param <V> 值类型
     * @return 字段-值映射，如果 key 不存在则返回空 Map
     * @throws IllegalArgumentException 如果 key 为 null
     */
    <K, V> Map<K, V> getHashAll(String key);

    /**
     * 获取 Hash 大小
     *
     * <p>获取指定 key 的 Hash 的大小（字段数量）。
     *
     * @param key 键
     * @return Hash 大小，如果 key 不存在则返回 0
     * @throws IllegalArgumentException 如果 key 为 null
     */
    int getHashSize(String key);
}

