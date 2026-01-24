package com.ysmjjsy.goya.component.framework.cache.autoconfigure;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties.CaffeineCacheProperties;
import com.ysmjjsy.goya.component.framework.cache.caffeine.CaffeineCacheService;
import com.ysmjjsy.goya.component.framework.cache.caffeine.CaffeineFactory;
import com.ysmjjsy.goya.component.framework.cache.caffeine.GoyaCaffeineCacheManager;
import com.ysmjjsy.goya.component.framework.cache.core.CacheService;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import com.ysmjjsy.goya.component.framework.cache.key.DefaultCacheKeySerializer;
import com.ysmjjsy.goya.component.framework.cache.metrics.DefaultCacheMetrics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * <p>Goya 缓存核心自动配置类</p>
 * <p>注册缓存核心组件：CacheKeySerializer、CacheBloomFilter</p>
 *
 * @author goya
 * @since 2026/1/15 13:37
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaCacheAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(CacheKeySerializer.class)
    public CacheKeySerializer defaultCacheKeySerializer() {
        DefaultCacheKeySerializer serializer = new DefaultCacheKeySerializer();
        log.trace("[Goya] |- component [cache-core] GoyaCacheAutoConfiguration |- bean [defaultCacheKeySerializer] register.");
        return serializer;
    }

    @Bean
    public DefaultCacheMetrics defaultCacheMetrics(){
        DefaultCacheMetrics metrics = new DefaultCacheMetrics();
        log.trace("[Goya] |- component [cache-core] GoyaCacheAutoConfiguration |- bean [defaultCacheMetrics] register.");
        return metrics;
    }

    @Bean
    public Caffeine<Object, Object> caffeine(CaffeineFactory caffeineFactory) {
        Caffeine<Object, Object> caffeine = caffeineFactory.createCaffeine();
        log.trace("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration |- bean [caffeine] register.");
        return caffeine;
    }

    @Bean
    public CaffeineFactory caffeineFactory(CaffeineCacheProperties caffeineCacheProperties) {
        CaffeineFactory caffeineFactory = new CaffeineFactory(caffeineCacheProperties);
        log.trace("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration |- bean [caffeineFactory] register.");
        return caffeineFactory;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CaffeineCacheManager caffeineCacheManager(CaffeineFactory caffeineFactory) {
        GoyaCaffeineCacheManager caffeineCacheManager = new GoyaCaffeineCacheManager(caffeineFactory);
        log.trace("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration |- bean [caffeineCacheManager] register.");
        return caffeineCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    public CacheService caffeineCacheService(CaffeineCacheManager caffeineCacheManager) {
        CaffeineCacheService cacheService = new CaffeineCacheService(caffeineCacheManager);
        log.trace("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration |- bean [caffeineCacheService] register.");
        return cacheService;
    }
}
