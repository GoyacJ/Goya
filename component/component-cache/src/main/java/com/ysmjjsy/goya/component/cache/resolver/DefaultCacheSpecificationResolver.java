package com.ysmjjsy.goya.component.cache.resolver;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存配置规范解析器实现
 *
 * <p>从 {@link CacheProperties} 读取配置，解析为 {@link CacheSpecification}。
 * 支持配置缓存，提升解析性能。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>从 CacheProperties 读取配置</li>
 *   <li>合并默认配置和特定配置</li>
 *   <li>缓存解析结果（提升性能）</li>
 *   <li>创建 CacheSpecification 实例</li>
 * </ul>
 *
 * <p><b>配置查找顺序：</b>
 * <ol>
 *   <li>查找 cacheName 的特定配置（caches.{cacheName}）</li>
 *   <li>如果不存在，使用默认配置（default）</li>
 *   <li>合并配置（特定配置覆盖默认配置）</li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 ConcurrentHashMap 缓存解析结果，线程安全</li>
 *   <li>使用 computeIfAbsent 确保同一 cacheName 只解析一次</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:29
 */
@Slf4j
public class DefaultCacheSpecificationResolver implements CacheSpecificationResolver {

    /**
     * 配置属性
     */
    private final CacheProperties properties;

    /**
     * 解析结果缓存
     * Key: cacheName
     * Value: CacheSpecification
     */
    private final ConcurrentHashMap<String, CacheSpecification> cache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param properties 配置属性
     * @throws IllegalArgumentException 如果 properties 为 null
     */
    public DefaultCacheSpecificationResolver(CacheProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null");
        }
        this.properties = properties;
    }

    @Override
    public CacheSpecification resolve(String cacheName) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }

        // 从缓存获取
        return cache.computeIfAbsent(cacheName, name -> {
            try {
                return doResolve(name);
            } catch (Exception e) {
                log.error("Failed to resolve cache specification for: {}", name, e);
                throw new IllegalStateException("Failed to resolve cache specification for: " + name, e);
            }
        });
    }

    /**
     * 执行解析
     *
     * @param cacheName 缓存名称
     * @return CacheSpecification 实例
     */
    private CacheSpecification doResolve(String cacheName) {
        // 1. 获取配置（特定配置或默认配置）
        CacheProperties.CacheConfig config = properties.getCacheConfig(cacheName);

        // 2. 创建 TtlStrategy
        TtlStrategy ttlStrategy = createTtlStrategy(config.localTtlStrategy());

        // 3. 解析降级策略类型
        FallbackStrategy.Type fallbackStrategyType = parseFallbackStrategyType(config.fallbackStrategy());

        // 4. 构建 CacheSpecification
        CacheSpecification.Builder builder = new CacheSpecification.Builder()
                .ttl(config.ttl())
                .allowNullValues(config.allowNullValues())
                .localMaxSize(config.localMaxSize())
                .localTtlStrategy(ttlStrategy)
                .enableBloomFilter(config.enableBloomFilter())
                .bloomFilterExpectedInsertions(config.bloomFilterExpectedInsertions())
                .bloomFilterFalsePositiveRate(config.bloomFilterFalsePositiveRate())
                .bloomFilterPersistent(config.bloomFilterPersistent())
                .fallbackStrategyType(fallbackStrategyType);

        CacheSpecification spec = builder.build();

        log.debug("Resolved cache specification: cacheName={}, ttl={}, localMaxSize={}, bloomFilterEnabled={}",
                cacheName, spec.getTtl(), spec.getLocalMaxSize(), spec.isEnableBloomFilter());

        return spec;
    }

    /**
     * 创建 TTL 策略
     *
     * @param strategyConfig TTL 策略配置
     * @return TtlStrategy 实例
     */
    private TtlStrategy createTtlStrategy(CacheProperties.TtlStrategyConfig strategyConfig) {
        if (strategyConfig == null) {
            // 默认使用固定比例策略（80%）
            return new TtlStrategy.FixedRatioStrategy(0.8);
        }

        String type = strategyConfig.type();
        if (type == null || type.isEmpty()) {
            type = "fixed-ratio";
        }

        switch (type.toLowerCase()) {
            case "fixed-ratio":
                double ratio = strategyConfig.ratio();
                if (ratio <= 0 || ratio > 1) {
                    log.warn("Invalid ratio for fixed-ratio strategy: {}, using default 0.8", ratio);
                    ratio = 0.8;
                }
                return new TtlStrategy.FixedRatioStrategy(ratio);

            case "fixed-duration":
                if (strategyConfig.duration() == null) {
                    throw new IllegalStateException("Duration is required for fixed-duration strategy");
                }
                return new TtlStrategy.FixedDurationStrategy(strategyConfig.duration());

            case "independent":
                if (strategyConfig.duration() == null) {
                    throw new IllegalStateException("Duration is required for independent strategy");
                }
                return new TtlStrategy.IndependentStrategy(strategyConfig.duration());

            default:
                log.warn("Unknown TTL strategy type: {}, using default fixed-ratio", type);
                return new TtlStrategy.FixedRatioStrategy(0.8);
        }
    }

    /**
     * 解析降级策略类型
     *
     * @param strategy 策略字符串
     * @return FallbackStrategy.Type
     */
    private FallbackStrategy.Type parseFallbackStrategyType(String strategy) {
        if (strategy == null || strategy.isEmpty()) {
            return FallbackStrategy.Type.DEGRADE_TO_L1;
        }

        return switch (strategy.toLowerCase()) {
            case "degrade-to-l1" -> FallbackStrategy.Type.DEGRADE_TO_L1;
            case "fail-fast" -> FallbackStrategy.Type.FAIL_FAST;
            case "ignore" -> FallbackStrategy.Type.IGNORE;
            default -> {
                log.warn("Unknown fallback strategy type: {}, using default degrade-to-l1", strategy);
                yield FallbackStrategy.Type.DEGRADE_TO_L1;
            }
        };
    }

    /**
     * 清除缓存（用于配置热更新）
     *
     * <p>当配置更新时，可以调用此方法清除缓存，强制重新解析。
     */
    public void clearCache() {
        cache.clear();
        log.info("Cleared cache specification cache");
    }
}
