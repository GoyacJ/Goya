package com.ysmjjsy.goya.component.cache.configuration;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.factory.CacheFactory;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.local.CaffeineLocalCache;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.metrics.DefaultCacheMetrics;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.resolver.DefaultCacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.service.DefaultCacheService;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.ttl.DefaultFallbackStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * GoyaCache Spring Boot 自动配置类
 *
 * <p>自动配置 GoyaCache 相关的 Bean，包括：
 * <ul>
 *   <li>CacheProperties - 配置属性绑定</li>
 *   <li>CacheSpecificationResolver - 配置解析器</li>
 *   <li>BloomFilterManager - 布隆过滤器管理器</li>
 *   <li>CacheRefillManager - 缓存回填管理器</li>
 *   <li>CacheEventPublisher - 缓存事件发布器</li>
 *   <li>GoyaCacheManager - 缓存管理器</li>
 * </ul>
 *
 * <p><b>条件：</b>
 * <ul>
 *   <li>存在 Caffeine 类（@ConditionalOnClass）</li>
 *   <li>不存在 CacheManager Bean（@ConditionalOnMissingBean）</li>
 * </ul>
 *
 * <p><b>与 Spring Boot 的集成点：</b>
 * <ul>
 *   <li>使用 @AutoConfiguration 注册为自动配置类</li>
 *   <li>使用 @EnableConfigurationProperties 启用配置属性绑定</li>
 *   <li>注册的 CacheManager Bean 会被 Spring Cache 使用</li>
 * </ul>
 *
 * @author goya
 * @see CacheProperties
 * @since 2025/12/22
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache] CacheAutoConfiguration auto configure.");
    }

    /**
     * 配置解析器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheSpecificationResolver cacheSpecificationResolver(CacheProperties properties) {
        DefaultCacheSpecificationResolver resolver = new DefaultCacheSpecificationResolver(properties);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheSpecificationResolver] register.");
        return resolver;
    }

    /**
     * 布隆过滤器管理器 Bean
     *
     * <p>注意：需要实现 BloomFilterConfigProvider，当前使用临时实现
     */
    @Bean
    @ConditionalOnMissingBean
    public BloomFilterManager bloomFilterManager(CacheSpecificationResolver resolver, CacheMetrics metrics) {
        log.info("Creating BloomFilterManager");
        // 创建 BloomFilterConfigProvider 实现
        BloomFilterManager.BloomFilterConfigProvider configProvider = cacheName -> {
            CacheSpecification spec = resolver.resolve(cacheName);
            if (!spec.isEnableBloomFilter()) {
                return null;
            }
            return new BloomFilterManager.BloomFilterConfig(
                    true,
                    spec.getBloomFilterExpectedInsertions(),
                    spec.getBloomFilterFalsePositiveRate()
            );
        };
        // 使用默认 key 序列化器和监控指标
        BloomFilterManager bloomFilterManager = new BloomFilterManager(configProvider, null, metrics);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [bloomFilterManager] register.");
        return bloomFilterManager;
    }

    /**
     * 缓存回填管理器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheRefillManager cacheRefillManager(CacheSpecificationResolver specificationResolver) {
        CacheRefillManager manager = new CacheRefillManager(specificationResolver);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheRefillManager] register.");
        return manager;
    }

    /**
     * 缓存事件发布器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheEventPublisher cacheEventPublisher(ApplicationEventPublisher eventPublisher) {
        CacheEventPublisher publisher = new CacheEventPublisher(eventPublisher);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheEventPublisher] register.");
        return publisher;
    }

    /**
     * 降级策略工厂 Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "fallbackStrategyFactory")
    public GoyaCacheManager.FallbackStrategyFactory fallbackStrategyFactory(CacheSpecificationResolver specificationResolver) {
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [fallbackStrategyFactory] register.");
        return type -> new DefaultFallbackStrategy(type, specificationResolver);
    }

    /**
     * 本地缓存工厂 Bean
     *
     * <p>注意：RemoteCache 工厂需要由 redis 模块提供
     */
    @Bean
    @ConditionalOnMissingBean(name = "localCacheFactory")
    public GoyaCacheManager.LocalCacheFactory localCacheFactory() {
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [localCacheFactory] register.");
        return CaffeineLocalCache::new;
    }

    /**
     * 监控指标 Bean（可选）
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheMetrics cacheMetrics() {
        DefaultCacheMetrics metrics = new DefaultCacheMetrics();
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheMetrics] register.");
        return metrics;
    }

    /**
     * 缓存服务 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ICacheService cacheService(CacheManager cacheManager, CacheMetrics metrics) {
        DefaultCacheService service = new DefaultCacheService(cacheManager, metrics);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheService] register.");
        return service;
    }

    /**
     * 缓存工厂 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheFactory cacheFactory(
            CacheSpecificationResolver specificationResolver,
            GoyaCacheManager.LocalCacheFactory localCacheFactory,
            GoyaCacheManager.RemoteCacheFactory remoteCacheFactory,
            BloomFilterManager bloomFilterManager,
            CacheRefillManager refillManager,
            CacheEventPublisher eventPublisher,
            GoyaCacheManager.FallbackStrategyFactory fallbackStrategyFactory,
            CacheMetrics metrics) {
        CacheFactory cacheFactory = new CacheFactory(
                specificationResolver,
                localCacheFactory,
                remoteCacheFactory,
                bloomFilterManager,
                refillManager,
                eventPublisher,
                fallbackStrategyFactory,
                metrics
        );
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheFactory] register.");
        return cacheFactory;
    }

    /**
     * GoyaCacheManager Bean
     *
     * <p>注意：需要 RemoteCacheFactory，如果不存在则使用 NoOpRemoteCache（仅用于测试）
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public GoyaCacheManager goyaCacheManager(
            CacheSpecificationResolver specificationResolver,
            GoyaCacheManager.LocalCacheFactory localCacheFactory,
            GoyaCacheManager.RemoteCacheFactory remoteCacheFactory,
            BloomFilterManager bloomFilterManager,
            CacheRefillManager refillManager,
            CacheEventPublisher eventPublisher,
            GoyaCacheManager.FallbackStrategyFactory fallbackStrategyFactory,
            CacheMetrics metrics) {

        GoyaCacheManager manager = new GoyaCacheManager(
                specificationResolver,
                localCacheFactory,
                remoteCacheFactory,
                bloomFilterManager,
                refillManager,
                eventPublisher,
                fallbackStrategyFactory,
                metrics
        );
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [goyaCacheManager] register.");
        return manager;
    }
}
