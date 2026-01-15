package com.ysmjjsy.goya.component.cache.multilevel.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ysmjjsy.goya.component.cache.core.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.core.support.CacheBloomFilter;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.core.support.DefaultCacheKeySerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Guava 布隆过滤器实现</p>
 * <p>基于 Guava 的 BloomFilter 实现缓存穿透防护</p>
 * <p>支持按 cacheName 创建不同的布隆过滤器</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 检查 key 是否可能存在
 * if (bloomFilter.mightContain("userCache", "user:123")) {
 *     // 可能存在，继续查询缓存
 * }
 *
 * // 添加 key 到布隆过滤器
 * bloomFilter.put("userCache", "user:123");
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15 11:53
 * @see CacheBloomFilter
 * @see BloomFilter
 */
@Slf4j
public class GuavaCacheBloomFilter implements CacheBloomFilter {

    /**
     * 布隆过滤器 Map
     * Key: cacheName
     * Value: BloomFilterWrapper
     */
    private final ConcurrentHashMap<String, BloomFilterWrapper> filters = new ConcurrentHashMap<>();

    /**
     * 配置提供者（用于获取 CacheSpecification）
     */
    private final BloomFilterConfigProvider configProvider;

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer keySerializer;

    /**
     * 监控指标（可选）
     */
    private final CacheMetrics metrics;

    /**
     * 扩容阈值（当前插入量 / 预期插入量）
     */
    private static final double RESIZE_THRESHOLD = 0.9;

    /**
     * 构造函数
     *
     * @param configProvider 配置提供者
     * @param keySerializer 缓存键序列化器（如果为 null，使用默认序列化器）
     * @param metrics 监控指标（可选，如果为 null 则不记录指标）
     */
    public GuavaCacheBloomFilter(BloomFilterConfigProvider configProvider, CacheKeySerializer keySerializer, CacheMetrics metrics) {
        if (configProvider == null) {
            throw new IllegalArgumentException("ConfigProvider cannot be null");
        }
        this.configProvider = configProvider;
        this.keySerializer = keySerializer != null ? keySerializer : new DefaultCacheKeySerializer();
        this.metrics = metrics;
    }

    /**
     * 构造函数（使用默认 key 序列化器）
     *
     * @param configProvider 配置提供者
     * @param metrics 监控指标（可选，如果为 null 则不记录指标）
     */
    public GuavaCacheBloomFilter(BloomFilterConfigProvider configProvider, CacheMetrics metrics) {
        this(configProvider, null, metrics);
    }

    /**
     * 构造函数（使用默认 key 序列化器和无监控指标）
     *
     * @param configProvider 配置提供者
     */
    public GuavaCacheBloomFilter(BloomFilterConfigProvider configProvider) {
        this(configProvider, null, null);
    }

    /**
     * 检查 key 是否可能存在
     *
     * <p>如果布隆过滤器未初始化，返回 true（允许查询，避免阻塞）。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return 如果 key 可能存在，返回 true；如果确定不存在，返回 false
     */
    @Override
    public boolean mightContain(String cacheName, Object key) {
        if (cacheName == null || key == null) {
            return true;
        }

        BloomFilterWrapper wrapper = filters.get(cacheName);
        if (wrapper == null) {
            // 未初始化，返回 true（允许查询）
            return true;
        }

        // 序列化 key
        byte[] keyBytes = serializeKey(key);
        return wrapper.filter.mightContain(keyBytes);
    }

    /**
     * 异步更新布隆过滤器
     *
     * <p>将 key 添加到布隆过滤器。如果布隆过滤器未初始化，延迟初始化。
     * 如果接近饱和，自动扩容。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return CompletableFuture，正常完成时返回 null，异常完成时包含异常
     */
    @Override
    public <K> CompletableFuture<Void> putAsync(String cacheName, K key) {
        if (cacheName == null || key == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                BloomFilterWrapper wrapper = filters.computeIfAbsent(cacheName, name -> {
                    // 延迟初始化
                    return createFilter(name);
                });

                synchronized (wrapper) {
                    // 检查是否需要扩容
                    if (wrapper.currentInsertions >= wrapper.expectedInsertions * RESIZE_THRESHOLD) {
                        log.info("Bloom filter is接近饱和，开始扩容: cacheName={}, current={}, expected={}",
                                cacheName, wrapper.currentInsertions, wrapper.expectedInsertions);
                        wrapper = resizeFilter(cacheName, wrapper);
                    }

                    // 添加 key 到布隆过滤器
                    byte[] keyBytes = serializeKey(key);
                    wrapper.filter.put(keyBytes);
                    wrapper.currentInsertions++;
                }
            } catch (Exception e) {
                log.warn("Failed to update bloom filter for cacheName: {}, key: {}", cacheName, key, e);
                // 不抛出异常，不影响主流程
            }
        });
    }

    /**
     * 创建布隆过滤器
     *
     * @param cacheName 缓存名称
     * @return BloomFilterWrapper
     */
    private BloomFilterWrapper createFilter(String cacheName) {
        BloomFilterConfig config = configProvider.getConfig(cacheName);
        if (config == null || !config.enabled()) {
            // 未启用布隆过滤器，返回 null（但不会到达这里，因为调用前已检查）
            throw new IllegalStateException("Bloom filter is not enabled for cache: " + cacheName);
        }

        long expectedInsertions = config.expectedInsertions();
        double fpp = config.falsePositiveRate();

        BloomFilter<byte[]> filter = BloomFilter.create(
                Funnels.byteArrayFunnel(),
                expectedInsertions,
                fpp
        );

        log.info("Created bloom filter: cacheName={}, expectedInsertions={}, fpp={}",
                cacheName, expectedInsertions, fpp);

        return new BloomFilterWrapper(filter, expectedInsertions, 0);
    }

    /**
     * 扩容布隆过滤器
     *
     * <p>创建新的布隆过滤器，容量为原来的 2 倍。
     * 注意：旧数据无法迁移，新布隆过滤器从空开始，这会导致旧 key 的缓存穿透保护失效。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>记录扩容前的插入量（用于监控）</li>
     *   <li>创建新的布隆过滤器（容量为原来的 2 倍）</li>
     *   <li>替换旧的布隆过滤器</li>
     *   <li>记录警告日志和监控告警</li>
     * </ol>
     *
     * <p><b>风险：</b>
     * <ul>
     *   <li>扩容后旧 key 的缓存穿透保护失效，可能导致缓存穿透</li>
     *   <li>新布隆过滤器需要重新积累数据</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @param oldWrapper 旧的布隆过滤器包装器
     * @return 新的 BloomFilterWrapper
     */
    private BloomFilterWrapper resizeFilter(String cacheName, BloomFilterWrapper oldWrapper) {
        // 记录扩容前的插入量（用于监控）
        long oldInsertions = oldWrapper.currentInsertions;
        long oldExpectedInsertions = oldWrapper.expectedInsertions;

        BloomFilterConfig config = configProvider.getConfig(cacheName);
        // 扩容 2 倍
        long newExpectedInsertions = oldExpectedInsertions * 2;

        BloomFilter<byte[]> newFilter = BloomFilter.create(
                Funnels.byteArrayFunnel(),
                newExpectedInsertions,
                config.falsePositiveRate()
        );

        BloomFilterWrapper newWrapper = new BloomFilterWrapper(
                newFilter, newExpectedInsertions, 0
        );

        // 替换旧的布隆过滤器
        filters.put(cacheName, newWrapper);

        // 记录警告日志：数据丢失风险
        log.warn("Bloom filter resized: cacheName={}, oldExpected={}, oldInsertions={}, newExpected={}. " +
                        "Warning: Old data is lost, cache penetration protection for old keys will be ineffective until new filter accumulates data.",
                cacheName, oldExpectedInsertions, oldInsertions, newExpectedInsertions);

        // 记录监控指标
        if (metrics != null) {
            metrics.recordBloomFilterResize(cacheName, oldInsertions, oldExpectedInsertions, newExpectedInsertions);
        }

        return newWrapper;
    }

    /**
     * 序列化 key
     *
     * <p>使用 CacheKeySerializer 序列化 key，确保与 Redis key 序列化策略一致。
     *
     * @param key 缓存键
     * @return 序列化后的字节数组
     */
    private byte[] serializeKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        return keySerializer.serialize(key);
    }

    /**
     * 布隆过滤器包装器
     */
    private static class BloomFilterWrapper {
        private final BloomFilter<byte[]> filter;
        private final long expectedInsertions;
        private volatile long currentInsertions;

        public BloomFilterWrapper(BloomFilter<byte[]> filter, long expectedInsertions, long currentInsertions) {
            this.filter = filter;
            this.expectedInsertions = expectedInsertions;
            this.currentInsertions = currentInsertions;
        }
    }

    /**
     * 布隆过滤器配置提供者接口
     *
     * <p>用于从 CacheSpecification 获取布隆过滤器配置。
     */
    public interface BloomFilterConfigProvider {
        /**
         * 获取布隆过滤器配置
         *
         * @param cacheName 缓存名称
         * @return 布隆过滤器配置，如果未启用则返回 null
         */
        BloomFilterConfig getConfig(String cacheName);
    }

    /**
     * 布隆过滤器配置
     */
    public record BloomFilterConfig(boolean enabled, long expectedInsertions, double falsePositiveRate) {

    }
}
