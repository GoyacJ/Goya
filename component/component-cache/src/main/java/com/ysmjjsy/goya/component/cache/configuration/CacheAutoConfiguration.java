package com.ysmjjsy.goya.component.cache.configuration;

import com.ysmjjsy.goya.component.cache.annotation.CacheType;
import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.enums.CacheTypeEnum;
import com.ysmjjsy.goya.component.cache.service.CaffeineCacheService;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>缓存自动配置类</p>
 * <p>根据配置自动注册缓存服务实现</p>
 * <ul>
 *     <li>默认使用 Caffeine 本地缓存</li>
 *     <li>当存在 Redis 依赖时，优先使用 Redis 缓存</li>
 *     <li>支持通过配置切换缓存类型</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/19 17:29
 * @see CacheProperties
 * @see ICacheService
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
     * 注册 Caffeine 缓存服务
     * <p>当不存在其他 ICacheService 实现时自动注册</p>
     * <p>可通过配置 {@code platform.cache.type=caffeine} 明确指定使用 Caffeine</p>
     *
     * @return CaffeineCacheService 实例
     */
    @Bean
    @ConditionalOnMissingBean(ICacheService.class)
    @CacheType(CacheTypeEnum.CAFFEINE)
    public CaffeineCacheService caffeineCacheService(CacheProperties cacheProperties) {
        CaffeineCacheService service = new CaffeineCacheService(cacheProperties);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [caffeineCacheService] register.");
        return service;
    }
}
