package com.ysmjjsy.goya.component.cache.resolver;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.Getter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 单个缓存的配置规范
 *
 * <p>定义每个 cacheName 的完整配置，包括 TTL、容量、布隆过滤器、降级策略等。
 * 支持扩展配置项，不硬编码所有属性，便于未来扩展。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>存储单个缓存的完整配置信息</li>
 *   <li>提供类型安全的常用配置访问</li>
 *   <li>支持扩展配置的存储和访问</li>
 *   <li>计算 L1 TTL（根据策略）</li>
 * </ul>
 *
 * <p><b>配置来源：</b>
 * <ul>
 *   <li>从 {@link CacheProperties} 读取</li>
 *   <li>支持按 cacheName 的特定配置，未配置项使用默认值</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>实例创建后不可变（immutable）</li>
 *   <li>所有字段都是 final 或通过 Builder 模式设置</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:22
 */
@Getter
public class CacheSpecification {


    // ========== 常用配置（类型安全） ==========

    /**
     * L2 缓存的过期时间
     */
    private final Duration ttl;

    /**
     * 是否允许缓存 null 值
     */
    private final boolean allowNullValues;

    /**
     * 本地缓存最大容量（条目数）
     */
    private final long localMaxSize;

    /**
     * L1 TTL 计算策略
     */
    private final TtlStrategy localTtlStrategy;

    // ========== 布隆过滤器配置 ==========

    /**
     * 是否启用布隆过滤器
     */
    private final boolean enableBloomFilter;

    /**
     * 布隆过滤器预期插入量
     */
    private final long bloomFilterExpectedInsertions;

    /**
     * 布隆过滤器误判率
     */
    private final double bloomFilterFalsePositiveRate;

    /**
     * 布隆过滤器是否持久化（未来扩展）
     */
    private final boolean bloomFilterPersistent;

    // ========== 降级策略配置 ==========

    /**
     * 降级策略类型
     */
    private final FallbackStrategy.Type fallbackStrategyType;

    // ========== 扩展配置 ==========

    /**
     * 扩展配置项（Map 存储，支持未来扩展）
     */
    private final Map<String, Object> extendedProperties;

    /**
     * 私有构造函数，使用 Builder 创建实例
     */
    private CacheSpecification(Builder builder) {
        this.ttl = builder.ttl;
        this.allowNullValues = builder.allowNullValues;
        this.localMaxSize = builder.localMaxSize;
        this.localTtlStrategy = builder.localTtlStrategy;
        this.enableBloomFilter = builder.enableBloomFilter;
        this.bloomFilterExpectedInsertions = builder.bloomFilterExpectedInsertions;
        this.bloomFilterFalsePositiveRate = builder.bloomFilterFalsePositiveRate;
        this.bloomFilterPersistent = builder.bloomFilterPersistent;
        this.fallbackStrategyType = builder.fallbackStrategyType;
        this.extendedProperties = new HashMap<>(builder.extendedProperties);
    }

    /**
     * 获取本地缓存 TTL（根据策略计算）
     *
     * <p>根据配置的 {@link TtlStrategy} 计算 L1 TTL。
     *
     * @return L1 缓存的 TTL，必须大于 0
     * @throws IllegalStateException 如果 TTL 策略未配置或计算结果无效
     */
    public Duration getLocalTtl() {
        if (localTtlStrategy == null) {
            throw new IllegalStateException("Local TTL strategy is not configured");
        }
        return localTtlStrategy.calculateLocalTtl(ttl);
    }

    /**
     * 获取扩展配置
     *
     * <p>从扩展配置 Map 中获取指定 key 的配置值，并转换为指定类型。
     *
     * @param key 配置键
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 配置值，如果不存在则返回 null
     * @throws ClassCastException 如果配置值无法转换为目标类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedProperty(String key, Class<T> type) {
        Object value = extendedProperties.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Extended property '" + key + "' is not of type " + type.getName());
        }
        return (T) value;
    }

    /**
     * 设置扩展配置
     *
     * <p>注意：此方法会创建新的 CacheSpecification 实例（因为实例是不可变的）。
     *
     * @param key 配置键
     * @param value 配置值
     * @return 新的 CacheSpecification 实例
     */
    public CacheSpecification withExtendedProperty(String key, Object value) {
        Builder builder = new Builder(this);
        builder.extendedProperties.put(key, value);
        return builder.build();
    }

    public Map<String, Object> getExtendedProperties() {
        // 返回副本，保护不可变性
        return new HashMap<>(extendedProperties);
    }

    // ========== Builder ==========

    /**
     * Builder 模式，用于创建 CacheSpecification 实例
     */
    public static class Builder {
        private Duration ttl;
        private boolean allowNullValues = true;
        private long localMaxSize = 10000;
        private TtlStrategy localTtlStrategy;
        private boolean enableBloomFilter = false;
        private long bloomFilterExpectedInsertions = 1000000;
        private double bloomFilterFalsePositiveRate = 0.03;
        private boolean bloomFilterPersistent = false;
        private FallbackStrategy.Type fallbackStrategyType = FallbackStrategy.Type.DEGRADE_TO_L1;
        private final Map<String, Object> extendedProperties = new HashMap<>();

        /**
         * 创建新的 Builder
         */
        public Builder() {
        }

        /**
         * 从现有 CacheSpecification 创建 Builder（用于修改）
         */
        public Builder(CacheSpecification spec) {
            this.ttl = spec.ttl;
            this.allowNullValues = spec.allowNullValues;
            this.localMaxSize = spec.localMaxSize;
            this.localTtlStrategy = spec.localTtlStrategy;
            this.enableBloomFilter = spec.enableBloomFilter;
            this.bloomFilterExpectedInsertions = spec.bloomFilterExpectedInsertions;
            this.bloomFilterFalsePositiveRate = spec.bloomFilterFalsePositiveRate;
            this.bloomFilterPersistent = spec.bloomFilterPersistent;
            this.fallbackStrategyType = spec.fallbackStrategyType;
            this.extendedProperties.putAll(spec.extendedProperties);
        }

        public Builder ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder allowNullValues(boolean allowNullValues) {
            this.allowNullValues = allowNullValues;
            return this;
        }

        public Builder localMaxSize(long localMaxSize) {
            this.localMaxSize = localMaxSize;
            return this;
        }

        public Builder localTtlStrategy(TtlStrategy localTtlStrategy) {
            this.localTtlStrategy = localTtlStrategy;
            return this;
        }

        public Builder enableBloomFilter(boolean enableBloomFilter) {
            this.enableBloomFilter = enableBloomFilter;
            return this;
        }

        public Builder bloomFilterExpectedInsertions(long bloomFilterExpectedInsertions) {
            this.bloomFilterExpectedInsertions = bloomFilterExpectedInsertions;
            return this;
        }

        public Builder bloomFilterFalsePositiveRate(double bloomFilterFalsePositiveRate) {
            this.bloomFilterFalsePositiveRate = bloomFilterFalsePositiveRate;
            return this;
        }

        public Builder bloomFilterPersistent(boolean bloomFilterPersistent) {
            this.bloomFilterPersistent = bloomFilterPersistent;
            return this;
        }

        public Builder fallbackStrategyType(FallbackStrategy.Type fallbackStrategyType) {
            this.fallbackStrategyType = fallbackStrategyType;
            return this;
        }

        public Builder extendedProperty(String key, Object value) {
            this.extendedProperties.put(key, value);
            return this;
        }

        /**
         * 构建 CacheSpecification 实例
         *
         * @return CacheSpecification 实例
         * @throws IllegalStateException 如果必需配置缺失
         */
        public CacheSpecification build() {
            if (ttl == null || ttl.isNegative() || ttl.isZero()) {
                throw new IllegalStateException("TTL must be positive");
            }
            if (localMaxSize <= 0) {
                throw new IllegalStateException("Local max size must be positive");
            }
            if (localTtlStrategy == null) {
                // 默认使用固定比例策略（80%）
                localTtlStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
            }
            if (bloomFilterFalsePositiveRate <= 0 || bloomFilterFalsePositiveRate >= 1) {
                throw new IllegalStateException("Bloom filter false positive rate must be in (0, 1)");
            }
            return new CacheSpecification(this);
        }
    }
}
