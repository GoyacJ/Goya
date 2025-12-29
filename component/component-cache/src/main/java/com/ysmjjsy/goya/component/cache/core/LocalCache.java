package com.ysmjjsy.goya.component.cache.core;

import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * 本地缓存抽象接口
 *
 * <p>扩展 Spring Cache 的 {@link Cache} 接口，为多级缓存编排提供本地缓存（L1）能力。
 * 本地缓存通常使用进程内内存存储，如 Caffeine，提供极低延迟的访问。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>提供本地缓存的读写操作</li>
 *   <li>支持 TTL 配置的写入操作</li>
 *   <li>支持批量操作以提升性能</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>继承 {@link Cache} 接口，完全兼容 Spring Cache SPI</li>
 *   <li>实现类将被 {@link GoyaCache} 使用，作为 L1 缓存层</li>
 * </ul>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法必须是线程安全的</li>
 *   <li>实现类应支持高并发读写操作</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:43
 */

public interface LocalCache extends Cache {

    /**
     * 写入缓存，指定 TTL
     *
     * <p>将键值对写入本地缓存，并设置过期时间。如果 key 已存在，将覆盖旧值。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>验证 key 和 value 非 null（value 可能为 NullValueWrapper）</li>
     *   <li>根据 TTL 配置写入缓存</li>
     *   <li>如果缓存已满，触发淘汰策略</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 key 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 TTL 为 null 或负数，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果缓存写入失败（如内存不足），抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param key 缓存键，不能为 null
     * @param value 缓存值，可以为 null（使用 NullValueWrapper 包装）
     * @param ttl 过期时间，必须大于 0
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果缓存操作失败
     */
    void put(Object key, Object value, Duration ttl);

    /**
     * 批量获取缓存值
     *
     * <p>一次性获取多个 key 的缓存值，提升性能。对于未命中的 key，返回的 Map 中不包含该 key。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>遍历 keys 集合，查询每个 key</li>
     *   <li>将命中的 key-value 对放入结果 Map</li>
     *   <li>返回结果 Map（只包含命中的 key）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 keys 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 keys 中包含 null，跳过该 key（不抛出异常）</li>
     *   <li>如果批量查询失败，抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param keys 缓存键集合，不能为 null
     * @return 命中的 key-value 映射，key 为缓存键，value 为 ValueWrapper，不会为 null，但可能为空 Map
     * @throws IllegalArgumentException 如果 keys 为 null
     * @throws RuntimeException 如果批量查询失败
     */
    Map<Object, ValueWrapper> getAll(Set<Object> keys);

    /**
     * 批量写入缓存
     *
     * <p>一次性写入多个键值对，提升性能。所有条目使用相同的 TTL。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>遍历 entries，逐个写入缓存</li>
     *   <li>如果某个 key 写入失败，记录日志但继续处理其他 key</li>
     *   <li>所有 key 处理完成后返回</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 entries 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 entries 中包含 null key，跳过该条目（不抛出异常）</li>
     *   <li>如果 TTL 无效，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果部分 key 写入失败，记录警告日志但不中断操作</li>
     * </ul>
     *
     * @param entries 键值对映射，不能为 null
     * @param ttl 过期时间，必须大于 0，所有条目共享此 TTL
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果批量写入完全失败
     */
    void putAll(Map<Object, Object> entries, Duration ttl);

    // ========== 原子操作 ==========

    /**
     * 原子递增
     *
     * <p>将指定 key 的值递增 1，如果 key 不存在则初始化为 0 后递增。
     * 此操作是原子的，保证并发安全。
     *
     * <p><b>注意：</b>如果实现类不支持原子操作，应抛出 {@link UnsupportedOperationException}。
     *
     * @param key 缓存键，不能为 null
     * @return 递增后的值
     * @throws IllegalArgumentException 如果 key 为 null
     * @throws UnsupportedOperationException 如果实现类不支持原子操作
     * @throws RuntimeException 如果操作失败
     */
    long increment(Object key);

    /**
     * 原子递增（带增量）
     *
     * <p>将指定 key 的值递增 delta，如果 key 不存在则初始化为 0 后递增。
     * 此操作是原子的，保证并发安全。
     *
     * <p><b>注意：</b>如果实现类不支持原子操作，应抛出 {@link UnsupportedOperationException}。
     *
     * @param key 缓存键，不能为 null
     * @param delta 增量（可以为负数，相当于递减）
     * @return 递增后的值
     * @throws IllegalArgumentException 如果 key 为 null
     * @throws UnsupportedOperationException 如果实现类不支持原子操作
     * @throws RuntimeException 如果操作失败
     */
    long incrementBy(Object key, long delta);

    /**
     * 原子递减
     *
     * <p>将指定 key 的值递减 1，如果 key 不存在则初始化为 0 后递减。
     * 此操作是原子的，保证并发安全。
     *
     * <p><b>注意：</b>如果实现类不支持原子操作，应抛出 {@link UnsupportedOperationException}。
     *
     * @param key 缓存键，不能为 null
     * @return 递减后的值
     * @throws IllegalArgumentException 如果 key 为 null
     * @throws UnsupportedOperationException 如果实现类不支持原子操作
     * @throws RuntimeException 如果操作失败
     */
    long decrement(Object key);

    /**
     * 设置过期时间
     *
     * <p>为指定 key 设置过期时间。如果 key 不存在，返回 false。
     *
     * <p><b>注意：</b>如果实现类不支持此操作，应抛出 {@link UnsupportedOperationException}。
     *
     * @param key 缓存键，不能为 null
     * @param ttl 过期时间，必须大于 0
     * @return true 如果设置成功，false 如果 key 不存在
     * @throws IllegalArgumentException 如果 key 为 null 或 ttl 无效
     * @throws UnsupportedOperationException 如果实现类不支持此操作
     * @throws RuntimeException 如果操作失败
     */
    boolean expire(Object key, Duration ttl);

    /**
     * 判断是否为 NoOp 实现
     *
     * <p>用于判断当前实现是否为 NoOp（空操作）实现。
     * NoOp 实现通常用于禁用本地缓存或作为占位符。
     *
     * <p><b>默认实现：</b>返回 false，表示这是一个真实的缓存实现。
     * NoOp 实现类应该覆盖此方法返回 true。
     *
     * @return true 如果是 NoOp 实现，false 如果是真实的缓存实现
     */
    default boolean isNoOp() {
        return false;
    }
}

