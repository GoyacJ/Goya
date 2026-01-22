package com.ysmjjsy.goya.component.cache.multilevel.configuration;

import com.ysmjjsy.goya.component.cache.core.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.core.metrics.DefaultCacheMetrics;
import com.ysmjjsy.goya.component.cache.core.support.CacheBloomFilter;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.core.support.DefaultCacheKeySerializer;
import com.ysmjjsy.goya.component.cache.multilevel.configuration.properties.MultiLevelProperties;
import com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.cache.multilevel.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.multilevel.factory.CacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.filter.GuavaCacheBloomFilter;
import com.ysmjjsy.goya.component.cache.multilevel.properties.PropertiesCacheService;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.DefaultCacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.multilevel.service.DefaultMultiLevelCacheService;
import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.cache.multilevel.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.multilevel.support.SingleFlightLoader;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.DefaultFallbackStrategy;
import com.ysmjjsy.goya.component.core.crypto.AsymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.core.crypto.SymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.framework.configuration.properties.CryptoProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * <p>多级缓存配置类</p>
 *
 * @author goya
 * @since 2026/1/15 11:38
 */
@Slf4j
@RequiredArgsConstructor
@EnableCaching
@EnableConfigurationProperties(MultiLevelProperties.class)
public class MultiLevelAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [multi-level] MultiLevelAutoConfiguration auto configure.");
    }

    /**
     * 配置解析器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheSpecificationResolver cacheSpecificationResolver(MultiLevelProperties properties) {
        DefaultCacheSpecificationResolver resolver = new DefaultCacheSpecificationResolver(properties);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheSpecificationResolver] register.");
        return resolver;
    }

    /**
     * 缓存键序列化器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheKeySerializer cacheKeySerializer() {
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheKeySerializer] register.");
        return new DefaultCacheKeySerializer();
    }

    /**
     * 布隆过滤器管理器 Bean
     *
     * <p>注意：需要实现 BloomFilterConfigProvider，当前使用临时实现
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheBloomFilter guavaCacheBloomFilter(CacheSpecificationResolver resolver, CacheMetrics metrics, CacheKeySerializer cacheKeySerializer) {
        log.info("Creating BloomFilterManager");
        // 创建 BloomFilterConfigProvider 实现
        GuavaCacheBloomFilter.BloomFilterConfigProvider configProvider = cacheName -> {
            CacheSpecification spec = resolver.resolve(cacheName);
            if (!spec.isEnableBloomFilter()) {
                return null;
            }
            return new GuavaCacheBloomFilter.BloomFilterConfig(
                    true,
                    spec.getBloomFilterExpectedInsertions(),
                    spec.getBloomFilterFalsePositiveRate()
            );
        };
        // 使用注入的 key 序列化器和监控指标
        GuavaCacheBloomFilter bloomFilter = new GuavaCacheBloomFilter(configProvider, cacheKeySerializer, metrics);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [guavaCacheBloomFilter] register.");
        return bloomFilter;
    }

    /**
     * 缓存回填管理器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheRefillManager cacheRefillManager(CacheSpecificationResolver specificationResolver, CacheMetrics metrics) {
        CacheRefillManager manager = new CacheRefillManager(specificationResolver, metrics);
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
    public MultiLevelCacheService defaultMultiLevelCacheService(CacheManager cacheManager, CacheMetrics metrics) {
        DefaultMultiLevelCacheService service = new DefaultMultiLevelCacheService(cacheManager, metrics);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheService] register.");
        return service;
    }

    /**
     * SingleFlight 加载器 Bean
     *
     * <p>用于防止缓存击穿，同一 key 的并发回源请求合并为一个。
     */
    @Bean
    @ConditionalOnMissingBean
    public SingleFlightLoader singleFlightLoader() {
        SingleFlightLoader loader = new SingleFlightLoader();
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [singleFlightLoader] register.");
        return loader;
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
            CacheBloomFilter cacheBloomFilter,
            CacheRefillManager refillManager,
            CacheEventPublisher eventPublisher,
            GoyaCacheManager.FallbackStrategyFactory fallbackStrategyFactory,
            CacheMetrics metrics,
            SingleFlightLoader singleFlightLoader) {
        CacheFactory cacheFactory = new CacheFactory(
                specificationResolver,
                localCacheFactory,
                remoteCacheFactory,
                cacheBloomFilter,
                refillManager,
                eventPublisher,
                fallbackStrategyFactory,
                metrics,
                singleFlightLoader
        );
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheFactory] register.");
        return cacheFactory;
    }

    /**
     * GoyaCacheManager Bean
     *
     * <p>使用 CacheFactory 创建缓存实例，统一缓存创建逻辑。
     */
    @Bean
    @Primary
    public GoyaCacheManager goyaCacheManager(CacheFactory cacheFactory) {
        GoyaCacheManager manager = new GoyaCacheManager(cacheFactory);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [goyaCacheManager] register.");
        return manager;
    }

    @Bean
    public PropertiesCacheService propertiesCacheService() {
        PropertiesCacheService template = new PropertiesCacheService();
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [propertiesCacheTemplate] register.");
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public CryptoProcessor cryptoProcessor(AsymmetricCryptoProcessor asymmetricCryptoProcessor,
                                           SymmetricCryptoProcessor symmetricCryptoProcessor,
                                           CryptoProperties cryptoProperties) {
        CryptoProcessor cryptoProcessor = new CryptoProcessor(asymmetricCryptoProcessor, symmetricCryptoProcessor, cryptoProperties);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [cryptoProcessor] register.");
        return cryptoProcessor;
    }
}
