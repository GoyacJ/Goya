package com.ysmjjsy.goya.component.cache.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.serializer.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.serializer.DefaultCacheKeySerializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器管理器
 *
 * <p>为每个 cacheName 维护独立的布隆过滤器，用于缓存穿透保护。
 * 支持动态扩容，当布隆过滤器接近饱和时自动扩容。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>为每个 cacheName 创建和管理独立的布隆过滤器</li>
 *   <li>提供 mightContain 和 put 操作</li>
 *   <li>支持动态扩容（当接近饱和时）</li>
 *   <li>延迟初始化（首次使用时创建）</li>
 * </ul>
 *
 * <p><b>调用关系：</b>
 * <ul>
 *   <li>由 {@link com.goya.cache.core.GoyaCache} 在 get() 和 put() 时调用</li>
 *   <li>需要从 {@link com.goya.cache.config.CacheSpecification} 读取配置</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li><b>mightContain()：</b>
 *     <ol>
 *       <li>查找该 cacheName 的布隆过滤器</li>
 *       <li>如果不存在，返回 true（允许查询，避免阻塞）</li>
 *       <li>如果存在，调用布隆过滤器的 mightContain</li>
 *     </ol>
 *   </li>
 *   <li><b>putAsync()：</b>
 *     <ol>
 *       <li>延迟初始化布隆过滤器（如果不存在）</li>
 *       <li>检查是否需要扩容（当前插入量 >= 预期插入量的 90%）</li>
 *       <li>如果需要，创建新的布隆过滤器并扩容</li>
 *       <li>将 key 添加到布隆过滤器</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储布隆过滤器，线程安全</li>
 *   <li>putAsync 操作在独立线程中执行</li>
 *   <li>扩容操作使用 synchronized 保护（同一 cacheName 的布隆过滤器）</li>
 * </ul>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>如果布隆过滤器初始化失败，记录错误日志，返回 true（允许查询）</li>
 *   <li>如果 put 操作失败，记录警告日志，不影响主流程</li>
 * </ul>
 *
 * <p><b>注意：</b>
 * <ul>
 *   <li>布隆过滤器不支持删除，因此只用于缓存穿透保护，不用于缓存命中判断</li>
 *   <li>布隆过滤器有误判率（通常 3%），因此即使判断"不存在"，仍查询 L2</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:51
 */
@Slf4j
public class BloomFilterManager {

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
    public BloomFilterManager(BloomFilterConfigProvider configProvider, CacheKeySerializer keySerializer, CacheMetrics metrics) {
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
    public BloomFilterManager(BloomFilterConfigProvider configProvider, CacheMetrics metrics) {
        this(configProvider, null, metrics);
    }

    /**
     * 构造函数（使用默认 key 序列化器和无监控指标）
     *
     * @param configProvider 配置提供者
     */
    public BloomFilterManager(BloomFilterConfigProvider configProvider) {
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
    public CompletableFuture<Void> putAsync(String cacheName, Object key) {
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
        if (config == null || !config.isEnabled()) {
            // 未启用布隆过滤器，返回 null（但不会到达这里，因为调用前已检查）
            throw new IllegalStateException("Bloom filter is not enabled for cache: " + cacheName);
        }

        long expectedInsertions = config.getExpectedInsertions();
        double fpp = config.getFalsePositiveRate();

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
                config.getFalsePositiveRate()
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
    @Getter
    public static class BloomFilterConfig {
        private final boolean enabled;
        private final long expectedInsertions;
        private final double falsePositiveRate;

        public BloomFilterConfig(boolean enabled, long expectedInsertions, double falsePositiveRate) {
            this.enabled = enabled;
            this.expectedInsertions = expectedInsertions;
            this.falsePositiveRate = falsePositiveRate;
        }

    }
}
