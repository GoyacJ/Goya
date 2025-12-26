package com.ysmjjsy.goya.component.cache.service;

import lombok.Getter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 缓存服务接口
 *
 * <p>提供便捷的缓存操作方法，封装 Spring Cache API，简化日常开发使用。
 * 支持基础操作、批量操作和高级功能。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>封装 Spring Cache API，提供类型安全的操作方法</li>
 *   <li>提供批量操作，提升性能</li>
 *   <li>提供高级功能（缓存预热、统计信息等）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>需要手动操作缓存时（不使用 @Cacheable 注解）</li>
 *   <li>需要批量操作缓存时</li>
 *   <li>需要缓存预热或查看统计信息时</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 15:03
 */
public interface ICacheService {

    /**
     * 获取缓存值
     *
     * <p>从缓存中获取指定 key 的值，如果不存在则返回 null。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存值，如果不存在则返回 null
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K, V> V get(String cacheName, K key);

    /**
     * 获取缓存值（带加载器）
     *
     * <p>从缓存中获取指定 key 的值，如果不存在则调用 valueLoader 加载并缓存。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param valueLoader 值加载器（当缓存不存在时调用）
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 缓存值或加载的值
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     * @throws RuntimeException 如果 valueLoader 执行失败
     */
    <K, V> V get(String cacheName, K key, Callable<V> valueLoader);

    /**
     * 写入缓存
     *
     * <p>将键值对写入缓存，使用配置的默认 TTL。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值（可以为 null，如果配置允许）
     * @param <K> 键类型
     * @param <V> 值类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     * @throws IllegalArgumentException 如果 value 为 null 且配置不允许 null 值
     */
    <K, V> void put(String cacheName, K key, V value);

    /**
     * 写入缓存（带自定义 TTL）
     *
     * <p>将键值对写入缓存，使用指定的 TTL。L2 缓存使用传入的 ttl，L1 缓存也使用传入的 ttl
     * （注意：Caffeine 本地缓存可能不支持每个 key 独立的 TTL，会使用全局策略）。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值（可以为 null，如果配置允许）
     * @param ttl 过期时间，必须大于 0
     * @param <K> 键类型
     * @param <V> 值类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     * @throws IllegalArgumentException 如果 value 为 null 且配置不允许 null 值
     * @throws IllegalArgumentException 如果 ttl 为 null 或无效
     */
    <K, V> void put(String cacheName, K key, V value, Duration ttl);

    /**
     * 失效缓存
     *
     * <p>从缓存中移除指定 key 的数据。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param <K> 键类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K> void evict(String cacheName, K key);

    /**
     * 清空缓存
     *
     * <p>清空指定缓存的所有数据。
     *
     * @param cacheName 缓存名称
     * @throws IllegalArgumentException 如果 cacheName 为 null
     */
    void clear(String cacheName);

    /**
     * 检查缓存是否存在
     *
     * <p>检查指定 key 是否存在于缓存中。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param <K> 键类型
     * @return true 如果存在，false 如果不存在
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K> boolean exists(String cacheName, K key);

    /**
     * 批量获取缓存值
     *
     * <p>一次性获取多个 key 的缓存值，提升性能。
     *
     * @param cacheName 缓存名称
     * @param keys 缓存键集合
     * @param <K> 键类型
     * @param <V> 值类型
     * @return key-value 映射，只包含命中的 key
     * @throws IllegalArgumentException 如果 cacheName 或 keys 为 null
     */
    <K, V> Map<K, V> batchGet(String cacheName, Set<K> keys);

    /**
     * 批量写入缓存
     *
     * <p>一次性写入多个键值对，提升性能。
     *
     * @param cacheName 缓存名称
     * @param entries 键值对映射
     * @param <K> 键类型
     * @param <V> 值类型
     * @throws IllegalArgumentException 如果 cacheName 或 entries 为 null
     */
    <K, V> void batchPut(String cacheName, Map<K, V> entries);

    /**
     * 批量失效缓存
     *
     * <p>一次性失效多个 key，提升性能。
     *
     * @param cacheName 缓存名称
     * @param keys 缓存键集合
     * @param <K> 键类型
     * @throws IllegalArgumentException 如果 cacheName 或 keys 为 null
     */
    <K> void batchEvict(String cacheName, Set<K> keys);

    /**
     * 缓存预热
     *
     * <p>使用指定的加载器预加载一批 key 到缓存中。
     * 支持并发加载，提升预热速度。
     *
     * @param cacheName 缓存名称
     * @param loader 值加载器（根据 key 加载值）
     * @param keys 需要预热的 key 集合
     * @param <K> 键类型
     * @param <V> 值类型
     * @throws IllegalArgumentException 如果 cacheName、loader 或 keys 为 null
     */
    <K, V> void warmUp(String cacheName, Function<K, V> loader, Set<K> keys);

    /**
     * 获取缓存统计信息
     *
     * <p>获取指定缓存的统计信息，包括命中率、未命中次数、延迟、热Key等企业级指标。
     *
     * @param cacheName 缓存名称
     * @return 缓存统计信息，如果监控未启用则返回空统计
     * @throws IllegalArgumentException 如果 cacheName 为 null
     */
    CacheStatistics getStatistics(String cacheName);

    /**
     * 获取热Key列表（Top N）
     *
     * <p>返回访问频率最高的Key列表，用于识别热Key和容量规划。
     *
     * @param cacheName 缓存名称
     * @param topN 返回前N个热Key
     * @return 热Key列表，按访问频率降序排列
     * @throws IllegalArgumentException 如果 cacheName 为 null 或 topN <= 0
     */
    List<HotKey> getHotKeys(String cacheName, int topN);

    /**
     * 缓存统计信息
     *
     * <p>包含缓存的各项统计指标，包括基础指标和企业级指标。
     */
    @Getter
    class CacheStatistics {
        private final long l1Hits;
        private final long l2Hits;
        private final long misses;
        private final double hitRate;
        private final long bloomFilterFalsePositives;
        private final double refillSuccessRate;
        private final double l1AvgLatencyMs;
        private final double l2AvgLatencyMs;
        private final double l1P99LatencyMs;
        private final double l2P99LatencyMs;
        private final long sourceLoadCount;
        private final double sourceLoadAvgLatencyMs;

        /**
         * 构造函数（基础指标）
         *
         * @param l1Hits L1 命中次数
         * @param l2Hits L2 命中次数
         * @param misses 未命中次数
         * @param bloomFilterFalsePositives 布隆过滤器误判次数
         * @param refillSuccessRate 回填成功率
         */
        public CacheStatistics(long l1Hits, long l2Hits, long misses,
                               long bloomFilterFalsePositives, double refillSuccessRate) {
            this(l1Hits, l2Hits, misses, bloomFilterFalsePositives, refillSuccessRate,
                    0.0, 0.0, 0.0, 0.0, 0, 0.0);
        }

        /**
         * 构造函数（完整指标）
         *
         * @param l1Hits L1 命中次数
         * @param l2Hits L2 命中次数
         * @param misses 未命中次数
         * @param bloomFilterFalsePositives 布隆过滤器误判次数
         * @param refillSuccessRate 回填成功率
         * @param l1AvgLatencyMs L1 平均延迟（毫秒）
         * @param l2AvgLatencyMs L2 平均延迟（毫秒）
         * @param l1P99LatencyMs L1 P99 延迟（毫秒）
         * @param l2P99LatencyMs L2 P99 延迟（毫秒）
         * @param sourceLoadCount 回源次数
         * @param sourceLoadAvgLatencyMs 回源平均延迟（毫秒）
         */
        public CacheStatistics(long l1Hits, long l2Hits, long misses,
                               long bloomFilterFalsePositives, double refillSuccessRate,
                               double l1AvgLatencyMs, double l2AvgLatencyMs,
                               double l1P99LatencyMs, double l2P99LatencyMs,
                               long sourceLoadCount, double sourceLoadAvgLatencyMs) {
            this.l1Hits = l1Hits;
            this.l2Hits = l2Hits;
            this.misses = misses;
            long total = l1Hits + l2Hits + misses;
            this.hitRate = total > 0 ? (double) (l1Hits + l2Hits) / total : 0.0;
            this.bloomFilterFalsePositives = bloomFilterFalsePositives;
            this.refillSuccessRate = refillSuccessRate;
            this.l1AvgLatencyMs = l1AvgLatencyMs;
            this.l2AvgLatencyMs = l2AvgLatencyMs;
            this.l1P99LatencyMs = l1P99LatencyMs;
            this.l2P99LatencyMs = l2P99LatencyMs;
            this.sourceLoadCount = sourceLoadCount;
            this.sourceLoadAvgLatencyMs = sourceLoadAvgLatencyMs;
        }

        @Override
        public String toString() {
            return "CacheStatistics{" +
                    "l1Hits=" + l1Hits +
                    ", l2Hits=" + l2Hits +
                    ", misses=" + misses +
                    ", hitRate=" + hitRate +
                    ", bloomFilterFalsePositives=" + bloomFilterFalsePositives +
                    ", refillSuccessRate=" + refillSuccessRate +
                    ", l1AvgLatencyMs=" + l1AvgLatencyMs +
                    ", l2AvgLatencyMs=" + l2AvgLatencyMs +
                    ", l1P99LatencyMs=" + l1P99LatencyMs +
                    ", l2P99LatencyMs=" + l2P99LatencyMs +
                    ", sourceLoadCount=" + sourceLoadCount +
                    ", sourceLoadAvgLatencyMs=" + sourceLoadAvgLatencyMs +
                    '}';
        }
    }

    /**
     * 热Key信息
     *
     * <p>包含Key和其访问频率，用于识别热Key和容量规划。
     */
    class HotKey {
        private final Object key;
        private final long accessCount;

        public HotKey(Object key, long accessCount) {
            this.key = key;
            this.accessCount = accessCount;
        }

        public Object getKey() {
            return key;
        }

        public long getAccessCount() {
            return accessCount;
        }

        @Override
        public String toString() {
            return "HotKey{key=" + key + ", accessCount=" + accessCount + '}';
        }
    }
}
