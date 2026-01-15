package com.ysmjjsy.goya.component.cache.multilevel.template;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 抽象计数器缓存模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供原子计数器功能。
 * 支持分布式环境下的原子递增/递减操作，适用于访问量统计、库存扣减、积分累计等场景。
 *
 * <p><b>类型约束：</b>
 * <ul>
 *   <li>此模板继承 {@code AbstractCacheTemplate<K, Long>}，确保缓存值类型为 Long</li>
 *   <li>所有原子操作都操作 Long 类型的值，保证类型安全</li>
 *   <li>如果缓存中存储的不是 Long 类型，可能导致类型转换异常</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供原子计数器操作，保证分布式环境下的数据一致性</li>
 *   <li>支持过期时间管理，自动清理过期数据</li>
 *   <li>支持批量操作，提升性能</li>
 *   <li>自动降级策略，确保功能始终可用</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>原子操作</b>：使用 GoyaCache 的原子操作，通过 CacheOrchestrator 统一编排，确保 L1/L2 数据一致性</li>
 *   <li><b>降级策略</b>：L2 不可用时，自动降级到 L1，确保功能可用</li>
 *   <li><b>过期管理</b>：支持设置过期时间，自动清理过期数据</li>
 * </ul>
 *
 * <p><b>一致性保证：</b>
 * <ul>
 *   <li>根据 {@link CacheSpecification#getConsistencyLevel()} 决定一致性策略</li>
 *   <li><b>STRONG</b>：L2 原子操作成功 + L1 同步成功，任一失败则回滚并抛出异常</li>
 *   <li><b>EVENTUAL</b>：优先使用 L2 原子操作，失败则降级到 L1，L1 结果不强制同步回 L2</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>访问量统计（PV/UV）</li>
 *   <li>点赞数、评论数、转发数</li>
 *   <li>库存扣减</li>
 *   <li>积分累计</li>
 *   <li>订单数量统计</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class UserViewCounter extends AbstractCounterTemplate<Long> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userViewCounter";
 *     }
 *
 *     // 记录用户访问
 *     public void recordView(Long userId) {
 *         increment(userId);
 *     }
 *
 *     // 获取用户访问量
 *     public long getViewCount(Long userId) {
 *         return getValue(userId);
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>原子操作通过 CacheOrchestrator 统一编排，保证 L1/L2 数据一致性</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <K> 计数器键类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractCounterTemplate<K> extends AbstractCacheTemplate<K, Long> {

    /**
     * 原子递增
     *
     * <p>将指定 key 的计数器递增 1，如果 key 不存在则初始化为 0 后递增。
     * 委托给 GoyaCache 的原子操作方法，通过 CacheOrchestrator 统一编排，确保 L1/L2 数据一致性。
     *
     * <p><b>一致性保证：</b>
     * 根据缓存配置的一致性等级决定策略（STRONG 或 EVENTUAL）。
     *
     * @param key 计数器键
     * @return 递增后的值
     * @throws IllegalArgumentException 如果 key 为 null
     * @throws RuntimeException 如果一致性等级为 STRONG 且操作失败
     */
    public long increment(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return getCache().increment(key);
    }

    /**
     * 原子递增（带自定义增量）
     *
     * <p>将指定 key 的计数器递增 delta。
     *
     * @param key 计数器键
     * @param delta 增量（可以为负数）
     * @return 递增后的值
     */
    public long incrementBy(K key, long delta) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return getCache().incrementBy(key, delta);
    }

    /**
     * 原子递减
     *
     * <p>将指定 key 的计数器递减 1，如果 key 不存在则初始化为 0 后递减。
     *
     * @param key 计数器键
     * @return 递减后的值
     */
    public long decrement(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return getCache().decrement(key);
    }

    /**
     * 原子递减（带自定义减量）
     *
     * <p>将指定 key 的计数器递减 delta。
     *
     * @param key 计数器键
     * @param delta 减量（可以为负数，相当于递增）
     * @return 递减后的值
     */
    public long decrementBy(K key, long delta) {
        // 递减 delta 等价于递增 -delta
        return incrementBy(key, -delta);
    }

    /**
     * 获取当前值
     *
     * <p>获取指定 key 的计数器当前值，如果不存在则返回 0。
     *
     * @param key 计数器键
     * @return 当前值，如果不存在则返回 0
     */
    public long getValue(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        Long value = get(key);
        return value != null ? value : 0L;
    }

    /**
     * 设置值
     *
     * <p>设置指定 key 的计数器值。
     *
     * @param key 计数器键
     * @param value 新值
     */
    public void setValue(K key, long value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        put(key, value);
    }

    /**
     * 设置值（带过期时间）
     *
     * <p>设置指定 key 的计数器值，并指定过期时间。
     *
     * @param key 计数器键
     * @param value 新值
     * @param ttl 过期时间
     */
    public void setValue(K key, long value, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        put(key, value, ttl);
    }

    /**
     * 原子递增（带过期时间）
     *
     * <p>将指定 key 的计数器递增 1，并设置过期时间。
     * 如果 key 已存在，只更新过期时间；如果 key 不存在，创建并设置过期时间。
     *
     * @param key 计数器键
     * @param ttl 过期时间
     * @return 递增后的值
     */
    public long increment(K key, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        long value = increment(key);
        // 设置过期时间
        getCache().expire(key, ttl);
        return value;
    }

    /**
     * 原子递增（带自定义增量和过期时间）
     *
     * @param key 计数器键
     * @param delta 增量
     * @param ttl 过期时间
     * @return 递增后的值
     */
    public long incrementBy(K key, long delta, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        long value = incrementBy(key, delta);
        // 设置过期时间
        getCache().expire(key, ttl);
        return value;
    }

    /**
     * 批量获取计数器值
     *
     * <p>批量获取多个 key 的计数器值。
     *
     * @param keys 计数器键集合
     * @return key-value 映射，只包含命中的 key
     */
    public Map<K, Long> batchGetValues(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        Map<K, Long> result = batchGet(keys);
        // 将 null 值转换为 0
        Map<K, Long> finalResult = new HashMap<>();
        for (Map.Entry<K, Long> entry : result.entrySet()) {
            finalResult.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : 0L);
        }
        return finalResult;
    }

    /**
     * 批量递增
     *
     * <p>批量递增多个 key 的计数器值。
     *
     * @param deltas key-delta 映射
     * @return key-value 映射，包含递增后的值
     */
    public Map<K, Long> batchIncrement(Map<K, Long> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return new HashMap<>();
        }

        Map<K, Long> result = new HashMap<>();
        for (Map.Entry<K, Long> entry : deltas.entrySet()) {
            K key = entry.getKey();
            Long delta = entry.getValue();
            if (key != null && delta != null) {
                long newValue = incrementBy(key, delta);
                result.put(key, newValue);
            }
        }
        return result;
    }

    /**
     * 重置计数器
     *
     * <p>将指定 key 的计数器重置为 0。
     *
     * @param key 计数器键
     */
    public void reset(K key) {
        setValue(key, 0L);
    }

    /**
     * 重置计数器（带过期时间）
     *
     * @param key 计数器键
     * @param ttl 过期时间
     */
    public void reset(K key, Duration ttl) {
        setValue(key, 0L, ttl);
    }

}
