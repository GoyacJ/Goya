package com.ysmjjsy.goya.component.cache.core.configuration;

import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.core.support.DefaultCacheKeySerializer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 13:37
 */
@Slf4j
@AutoConfiguration
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache-core] GoyaCacheAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(CacheKeySerializer.class)
    public CacheKeySerializer cacheKeySerializer() {
        DefaultCacheKeySerializer serializer = new DefaultCacheKeySerializer();
        log.trace("[Goya] |- component [cache-core] GoyaCacheAutoConfiguration |- bean [cacheKeySerializer] register.");
        return serializer;
    }
}
