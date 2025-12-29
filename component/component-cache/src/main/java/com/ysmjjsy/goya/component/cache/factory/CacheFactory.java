package com.ysmjjsy.goya.component.cache.factory;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.core.LocalCache;
import com.ysmjjsy.goya.component.cache.core.RemoteCache;
import com.ysmjjsy.goya.component.cache.enums.CacheLevel;
import com.ysmjjsy.goya.component.cache.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.local.NoOpLocalCache;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.remote.NoOpRemoteCache;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.support.SingleFlightLoader;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * 缓存工厂类
 *
 * <p>根据 cacheName 和配置创建 GoyaCache 实例，支持动态配置。
 * 创建的缓存实例是独立的，不会添加到 GoyaCacheManager 的缓存 Map 中。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>根据 cacheName 和 CacheSpecification 创建 GoyaCache 实例</li>
 *   <li>支持 Builder 模式动态配置</li>
 *   <li>支持快速创建（使用默认配置）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>需要动态创建缓存实例时</li>
 *   <li>需要使用不同于配置文件的缓存配置时</li>
 *   <li>需要临时缓存实例时</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:50
 */
@Slf4j
@RequiredArgsConstructor
public class CacheFactory {

    /**
     * 配置规范解析器
     */
    private final CacheSpecificationResolver specificationResolver;

    /**
     * 本地缓存工厂
     */
    private final GoyaCacheManager.LocalCacheFactory localCacheFactory;

    /**
     * 远程缓存工厂
     */
    private final GoyaCacheManager.RemoteCacheFactory remoteCacheFactory;

    /**
     * 布隆过滤器管理器
     */
    private final BloomFilterManager bloomFilterManager;

    /**
     * 缓存回填管理器
     */
    private final CacheRefillManager refillManager;

    /**
     * 缓存事件发布器
     */
    private final CacheEventPublisher eventPublisher;

    /**
     * 降级策略工厂
     */
    private final GoyaCacheManager.FallbackStrategyFactory fallbackStrategyFactory;

    /**
     * 监控指标（可选）
     */
    private final CacheMetrics metrics;

    /**
     * SingleFlight 加载器（防止缓存击穿）
     */
    private final SingleFlightLoader singleFlightLoader;


    /**
     * 根据 cacheName 和 CacheSpecification 创建 GoyaCache 实例
     *
     * <p>使用指定的配置规范创建缓存实例。创建的实例是独立的，不会注册到 GoyaCacheManager。
     * 根据 cacheLevel 配置自动创建对应的 L1/L2 实例（L1_ONLY 使用 NoOpRemoteCache，L2_ONLY 使用 NoOpLocalCache）。
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return GoyaCache 实例
     * @throws IllegalArgumentException 如果 cacheName 或 spec 为 null
     * @throws RuntimeException         如果缓存创建失败
     */
    public <K, V> GoyaCache<K, V> createCache(String cacheName, CacheSpecification spec) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (spec == null) {
            throw new IllegalArgumentException("CacheSpecification cannot be null");
        }

        try {
            // 1. 根据缓存级别创建 LocalCache 和 RemoteCache
            LocalCache l1;
            RemoteCache l2;

            CacheLevel cacheLevel = spec.getCacheLevel();
            if (cacheLevel == null) {
                // 默认多级缓存
                cacheLevel = CacheLevel.L1_L2;
            }

            l2 = switch (cacheLevel) {
                case L1_ONLY -> {
                    // 仅本地缓存：L1 使用实际实现，L2 使用 NoOp
                    l1 = localCacheFactory.create(cacheName, spec);
                    yield new NoOpRemoteCache(cacheName);
                }
                case L2_ONLY -> {
                    // 仅远程缓存：L1 使用 NoOp，L2 使用实际实现
                    l1 = new NoOpLocalCache(cacheName);
                    yield remoteCacheFactory.create(cacheName, spec);
                }
                default -> {
                    // 多级缓存：L1 和 L2 都使用实际实现
                    l1 = localCacheFactory.create(cacheName, spec);
                    yield remoteCacheFactory.create(cacheName, spec);
                }
            };

            if (l1 == null || l2 == null) {
                throw new IllegalStateException("Failed to create LocalCache or RemoteCache for: " + cacheName);
            }

            // 2. 创建降级策略
            FallbackStrategy fallbackStrategy = fallbackStrategyFactory.create(spec.getFallbackStrategyType());

            // 3. 创建 GoyaCache（不注册到 GoyaCacheManager）
            GoyaCache<K, V> cache = new GoyaCache<>(cacheName, l1, l2, spec, bloomFilterManager, refillManager,
                    eventPublisher, fallbackStrategy, metrics, singleFlightLoader);

            log.info("Created independent GoyaCache: name={}, level={}, ttl={}, localMaxSize={}, bloomFilterEnabled={}",
                    cacheName, cacheLevel, spec.getTtl(), spec.getLocalMaxSize(), spec.isEnableBloomFilter());

            return cache;
        } catch (Exception e) {
            log.error("Failed to create GoyaCache: cacheName={}", cacheName, e);
            throw new CommonException("Failed to create GoyaCache: " + cacheName, e);
        }
    }

    /**
     * 创建仅本地缓存（L1_ONLY）
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return GoyaCache 实例
     */
    public <K, V> GoyaCache<K, V> createL1Only(String cacheName, CacheSpecification spec) {
        CacheSpecification.Builder builder = new CacheSpecification.Builder(spec);
        builder.cacheLevel(CacheLevel.L1_ONLY);
        return createCache(cacheName, builder.build());
    }

    /**
     * 创建仅远程缓存（L2_ONLY）
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return GoyaCache 实例
     */
    public <K, V> GoyaCache<K, V> createL2Only(String cacheName, CacheSpecification spec) {
        CacheSpecification.Builder builder = new CacheSpecification.Builder(spec);
        builder.cacheLevel(CacheLevel.L2_ONLY);
        return createCache(cacheName, builder.build());
    }

    /**
     * 创建多级缓存（L1_L2）
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return GoyaCache 实例
     */
    public <K, V> GoyaCache<K, V> createL1L2(String cacheName, CacheSpecification spec) {
        CacheSpecification.Builder builder = new CacheSpecification.Builder(spec);
        builder.cacheLevel(CacheLevel.L1_L2);
        return createCache(cacheName, builder.build());
    }

    /**
     * 根据 cacheName 和配置构建器创建 GoyaCache 实例
     *
     * <p>使用配置构建器动态配置缓存。
     *
     * @param cacheName    缓存名称
     * @param configurator 配置构建器（用于配置 CacheSpecification）
     * @return GoyaCache 实例
     * @throws IllegalArgumentException 如果 cacheName 或 configurator 为 null
     * @throws RuntimeException         如果缓存创建失败
     */
    public <K, V> GoyaCache<K, V> createCache(String cacheName, Consumer<CacheSpecification.Builder> configurator) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (configurator == null) {
            throw new IllegalArgumentException("Configurator cannot be null");
        }

        try {
            // 获取基础配置（如果存在），否则使用默认配置
            CacheSpecification baseSpec = specificationResolver.resolve(cacheName);
            CacheSpecification.Builder builder;

            if (baseSpec != null) {
                // 基于现有配置修改
                builder = new CacheSpecification.Builder(baseSpec);
            } else {
                // 使用默认配置
                builder = new CacheSpecification.Builder();
                // 设置默认 TTL（1 小时）
                builder.ttl(Duration.ofHours(1));
            }

            // 应用配置构建器
            configurator.accept(builder);
            CacheSpecification spec = builder.build();

            return createCache(cacheName, spec);
        } catch (Exception e) {
            log.error("Failed to create GoyaCache with configurator: cacheName={}", cacheName, e);
            throw new CacheException("Failed to create GoyaCache: " + cacheName, e);
        }
    }

    /**
     * 快速创建缓存实例（使用默认配置）
     *
     * <p>使用指定的 TTL 和本地最大容量快速创建缓存实例。
     *
     * @param cacheName    缓存名称
     * @param ttl          过期时间
     * @param localMaxSize 本地缓存最大容量
     * @return GoyaCache 实例
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException         如果缓存创建失败
     */
    public <K, V> GoyaCache<K, V> createCache(String cacheName, Duration ttl, long localMaxSize) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        if (localMaxSize <= 0) {
            throw new IllegalArgumentException("LocalMaxSize must be positive");
        }

        return createCache(cacheName, builder -> {
            builder.ttl(ttl);
            builder.localMaxSize(localMaxSize);
        });
    }

    /**
     * 根据 cacheName 创建缓存实例（从配置解析）
     *
     * <p>从配置解析器获取 cacheName 对应的配置，然后创建缓存实例。
     * 此方法主要用于 GoyaCacheManager 委托创建缓存。
     *
     * @param cacheName 缓存名称
     * @return GoyaCache 实例
     * @throws IllegalArgumentException 如果 cacheName 为 null
     * @throws IllegalStateException    如果配置解析失败
     * @throws RuntimeException         如果缓存创建失败
     */
    public <K, V> GoyaCache<K, V> createCache(String cacheName) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        CacheSpecification spec = specificationResolver.resolve(cacheName);
        if (spec == null) {
            throw new IllegalStateException("Failed to resolve cache specification for: " + cacheName);
        }
        return createCache(cacheName, spec);
    }
}
