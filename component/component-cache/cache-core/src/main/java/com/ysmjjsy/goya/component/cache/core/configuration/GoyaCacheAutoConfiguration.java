package com.ysmjjsy.goya.component.cache.core.configuration;

import com.ysmjjsy.goya.component.cache.core.metrics.DefaultCacheMetrics;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.core.support.DefaultCacheKeySerializer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache-core] GoyaCacheAutoConfiguration auto configure.");
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
}
