package com.ysmjjsy.goya.component.cache.configuration;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.properties.DefaultPropertiesCacheService;
import com.ysmjjsy.goya.component.cache.properties.PropertiesCacheProcessor;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import com.ysmjjsy.goya.component.cache.service.CacheServiceFactory;
import com.ysmjjsy.goya.component.cache.service.HybridCacheService;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.cache.service.IL2Cache;
import com.ysmjjsy.goya.component.common.service.IPropertiesCacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * <p>缓存自动配置类</p>
 * <p>提供默认的混合缓存服务（HybridCacheService）和缓存服务工厂（CacheServiceFactory）</p>
 * <p>装配逻辑：</p>
 * <ul>
 *     <li>注册 CacheServiceFactory：供用户创建各种缓存实例</li>
 *     <li>注册 HybridCacheService：默认的 ICacheService 实现（L1 固定启用，L2 可选）</li>
 *     <li>启动失效监听器：处理跨节点缓存同步</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 * @see CacheProperties
 * @see HybridCacheService
 * @see CacheServiceFactory
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
     * 注册缓存服务工厂
     * <p>供用户创建各种缓存实例</p>
     *
     * @return CacheServiceFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean(CacheServiceFactory.class)
    public CacheServiceFactory cacheServiceFactory(CacheProperties properties,
                                                   ObjectProvider<IL2Cache> l2Cache,
                                                   ObjectProvider<ICacheInvalidatePublisher> publisher) {
        CacheServiceFactory factory = new CacheServiceFactory(properties,l2Cache.getIfAvailable(),publisher.getIfAvailable());
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [cacheServiceFactory] register.");
        return factory;
    }

    /**
     * 注册默认缓存服务（混合缓存）
     * <p>这是唯一注册为 ICacheService 的 bean</p>
     * <p>L1 固定启用，L2 根据 IL2Cache bean 存在性自动启用</p>
     *
     * @param factory 缓存服务工厂
     * @return HybridCacheService 实例
     */
    @Primary
    @Bean
    @ConditionalOnMissingBean(ICacheService.class)
    public HybridCacheService hybridCacheService(CacheServiceFactory factory) {
        HybridCacheService service = factory.createHybrid();
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [hybridCacheService] register.");
        return service;
    }

    @Bean
    public PropertiesCacheProcessor propertiesCacheProcessor(ICacheService iCacheService){
        PropertiesCacheProcessor processor = new PropertiesCacheProcessor(iCacheService);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [propertiesCacheProcessor] register.");
        return processor;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPropertiesCacheService propertiesCacheService(PropertiesCacheProcessor processor){
        DefaultPropertiesCacheService service = new DefaultPropertiesCacheService(processor);
        log.trace("[Goya] |- component [cache] CacheAutoConfiguration |- bean [propertiesCacheService] register.");
        return service;
    }
}
