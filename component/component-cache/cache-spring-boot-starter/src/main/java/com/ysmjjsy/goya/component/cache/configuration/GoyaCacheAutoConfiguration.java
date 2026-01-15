package com.ysmjjsy.goya.component.cache.configuration;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.core.support.CacheBloomFilter;
import com.ysmjjsy.goya.component.cache.caffeine.CaffeineCacheService;
import com.ysmjjsy.goya.component.cache.multilevel.configuration.properties.MultiLevelProperties;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheManager;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheOrchestrator;
import com.ysmjjsy.goya.component.cache.multilevel.factory.CaffeineLocalCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.factory.MultiLevelCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.factory.RedisRemoteCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationPublisher;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationSubscriber;
import com.ysmjjsy.goya.component.cache.multilevel.subscribe.RedisCacheInvalidationSubscriber;
import com.ysmjjsy.goya.component.cache.redis.service.RedisCacheService;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

/**
 * <p>Goya 缓存自动配置类</p>
 * <p>整合所有缓存模块，提供统一的多级缓存能力</p>
 * <p>装配逻辑：</p>
 * <ul>
 *     <li>根据配置和可用性自动判断缓存模式（单级/多级）</li>
 *     <li>注册工厂实现（CaffeineLocalCacheFactory、RedisRemoteCacheFactory）</li>
 *     <li>注册多级缓存相关 Bean（如果启用多级缓存）</li>
 *     <li>根据配置决定默认 CacheManager</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/15 11:50
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(MultiLevelProperties.class)
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache] GoyaCacheAutoConfiguration auto configure.");
    }

    /**
     * 注册 CaffeineLocalCacheFactory（如果 CaffeineCacheService 可用）
     */
    @Bean
    @ConditionalOnBean(CaffeineCacheService.class)
    @ConditionalOnMissingBean(LocalCacheFactory.class)
    public LocalCacheFactory caffeineLocalCacheFactory(CaffeineCacheService caffeineCacheService) {
        LocalCacheFactory factory = new CaffeineLocalCacheFactory(caffeineCacheService);
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [caffeineLocalCacheFactory] register.");
        return factory;
    }

    /**
     * 注册 RedisRemoteCacheFactory（如果 RedisCacheService 可用）
     */
    @Bean
    @ConditionalOnBean(RedisCacheService.class)
    @ConditionalOnMissingBean(RemoteCacheFactory.class)
    public RemoteCacheFactory redisRemoteCacheFactory(
            RedisCacheService redisCacheService,
            CacheService cacheService) {
        RemoteCacheFactory factory = new RedisRemoteCacheFactory(redisCacheService, cacheService);
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [redisRemoteCacheFactory] register.");
        return factory;
    }

    /**
     * 注册 RedisCacheInvalidationSubscriber（如果 RedisService 可用）
     */
    @Bean
    @ConditionalOnBean(RedisService.class)
    @ConditionalOnMissingBean(CacheInvalidationSubscriber.class)
    public CacheInvalidationSubscriber redisCacheInvalidationSubscriber(RedisService redisService) {
        CacheInvalidationSubscriber subscriber = new RedisCacheInvalidationSubscriber(redisService);
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [redisCacheInvalidationSubscriber] register.");
        return subscriber;
    }

    /**
     * 注册 MultiLevelCacheOrchestrator（如果启用多级缓存）
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "goya.cache.multi-level",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnBean({LocalCacheFactory.class, RemoteCacheFactory.class})
    @ConditionalOnMissingBean(MultiLevelCacheOrchestrator.class)
    public MultiLevelCacheOrchestrator multiLevelCacheOrchestrator(
            org.springframework.beans.factory.ObjectProvider<CacheInvalidationPublisher> cacheInvalidationPublisherProvider,
            org.springframework.beans.factory.ObjectProvider<CacheBloomFilter> cacheBloomFilterProvider,
            org.springframework.beans.factory.ObjectProvider<CacheInvalidationSubscriber> cacheInvalidationSubscriberProvider) {
        CacheInvalidationPublisher cacheInvalidationPublisher = cacheInvalidationPublisherProvider.getIfAvailable();
        CacheBloomFilter cacheBloomFilter = cacheBloomFilterProvider.getIfAvailable();
        CacheInvalidationSubscriber cacheInvalidationSubscriber = cacheInvalidationSubscriberProvider.getIfAvailable();
        
        MultiLevelCacheOrchestrator orchestrator = new MultiLevelCacheOrchestrator(
                cacheInvalidationPublisher,
                cacheBloomFilter,
                cacheInvalidationSubscriber
        );
        orchestrator.init();
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [multiLevelCacheOrchestrator] register.");
        return orchestrator;
    }

    /**
     * 注册 MultiLevelCacheFactory（如果启用多级缓存）
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "goya.cache.multi-level",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnBean({LocalCacheFactory.class, RemoteCacheFactory.class, MultiLevelCacheOrchestrator.class})
    @ConditionalOnMissingBean(MultiLevelCacheFactory.class)
    public MultiLevelCacheFactory multiLevelCacheFactory(
            LocalCacheFactory localCacheFactory,
            RemoteCacheFactory remoteCacheFactory,
            MultiLevelCacheOrchestrator orchestrator) {
        MultiLevelCacheFactory factory = new MultiLevelCacheFactory(
                localCacheFactory,
                remoteCacheFactory,
                orchestrator
        );
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [multiLevelCacheFactory] register.");
        return factory;
    }

    /**
     * 注册 MultiLevelCacheManager（如果启用多级缓存，且作为默认 CacheManager）
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "goya.cache.multi-level",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnBean({MultiLevelCacheFactory.class, MultiLevelProperties.class})
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager multiLevelCacheManager(
            MultiLevelCacheFactory cacheFactory,
            MultiLevelProperties properties) {
        CacheManager cacheManager = new MultiLevelCacheManager(cacheFactory, properties);
        log.trace("[Goya] |- component [cache] GoyaCacheAutoConfiguration |- bean [multiLevelCacheManager] register.");
        return cacheManager;
    }
}
