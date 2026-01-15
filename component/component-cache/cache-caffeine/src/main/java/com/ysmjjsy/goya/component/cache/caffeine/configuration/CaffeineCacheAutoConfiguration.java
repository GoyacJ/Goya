package com.ysmjjsy.goya.component.cache.caffeine.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.cache.caffeine.CaffeineCacheService;
import com.ysmjjsy.goya.component.cache.caffeine.CaffeineFactory;
import com.ysmjjsy.goya.component.cache.caffeine.GoyaCaffeineCacheManager;
import com.ysmjjsy.goya.component.cache.caffeine.configuration.properties.CaffeineCacheProperties;
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
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 23:07
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration auto configure.");
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
    public CaffeineCacheService caffeineCacheService(CaffeineCacheManager caffeineCacheManager) {
        CaffeineCacheService localEventPublisher = new CaffeineCacheService(caffeineCacheManager);
        log.trace("[Goya] |- framework [framework] CaffeineCacheAutoConfiguration |- bean [caffeineCacheService] register.");
        return localEventPublisher;
    }
}
