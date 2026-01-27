package com.ysmjjsy.goya.component.framework.cache.autoconfigure;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.api.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties.GoyaCacheProperties;
import com.ysmjjsy.goya.component.framework.cache.caffeine.CaffeineCacheService;
import com.ysmjjsy.goya.component.framework.cache.caffeine.GoyaCaffeineCacheManager;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import com.ysmjjsy.goya.component.framework.cache.key.DefaultCacheKeySerializer;
import com.ysmjjsy.goya.component.framework.cache.metrics.DefaultCacheMetrics;
import com.ysmjjsy.goya.component.framework.cache.multi.DefaultMultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
@EnableConfigurationProperties(GoyaCacheProperties.class)
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaCacheAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(CacheKeySerializer.class)
    public CacheKeySerializer defaultCacheKeySerializer(GoyaContext goyaContext) {
        DefaultCacheKeySerializer serializer = new DefaultCacheKeySerializer(goyaContext);
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [defaultCacheKeySerializer] register.");
        return serializer;
    }

    @Bean
    public DefaultCacheMetrics defaultCacheMetrics() {
        DefaultCacheMetrics metrics = new DefaultCacheMetrics();
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [defaultCacheMetrics] register.");
        return metrics;
    }

    @Bean
    @ConditionalOnMissingBean(GoyaCaffeineCacheManager.class)
    public GoyaCaffeineCacheManager goyaCaffeineCacheManager(GoyaCacheProperties cacheProperties) {
        GoyaCaffeineCacheManager cacheManager = new GoyaCaffeineCacheManager(cacheProperties);
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [goyaCaffeineCacheManager] register.");
        return cacheManager;
    }

    /**
     * 本地缓存服务实现
     *
     * <p>业务可直接注入 {@link CaffeineCacheService} 使用本地缓存。</p>
     */
    @Bean
    @ConditionalOnMissingBean(CaffeineCacheService.class)
    public CaffeineCacheService caffeineCacheService(GoyaCaffeineCacheManager cacheManager, CacheKeySerializer cacheKeySerializer, GoyaContext goyaContext) {
        CaffeineCacheService caffeineCacheService = new CaffeineCacheService(cacheManager, cacheKeySerializer, goyaContext);
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [caffeineCacheService] register.");
        return caffeineCacheService;
    }

    /**
     * 以接口形式暴露本地缓存（固定名：localCacheService）。
     *
     * <p>用于多级缓存装配与默认 cacheService 的回退。</p>
     */
    @Bean(name = "localCacheService")
    @ConditionalOnMissingBean(name = "localCacheService")
    public CacheService localCacheService(CaffeineCacheService caffeineCacheService) {
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [localCacheService] register.");
        return caffeineCacheService;
    }

    /**
     * 默认 CacheService（@Primary）。
     *
     * <p>规则：若存在名为 remoteCacheService 的 Bean，则优先使用远程；否则使用本地。</p>
     *
     * <p>注意：用“按名称查找”避免 remoteCacheService 的存在阻止该 Bean 创建。</p>
     */
    @Bean(name = "cacheService")
    @Primary
    @ConditionalOnMissingBean(name = "cacheService")
    public CacheService cacheService(
            @Qualifier("localCacheService") CacheService localCacheService) {
        CacheService remote = getRemoteByName();
        return (remote != null) ? remote : localCacheService;
    }

    /**
     * 多级缓存服务（始终提供，可退化为本地）。
     */
    @Bean(name = "multiLevelCacheService")
    @ConditionalOnMissingBean(MultiLevelCacheService.class)
    public MultiLevelCacheService multiLevelCacheService(
            @Qualifier("localCacheService") CacheService localCacheService,
            ObjectProvider<ApplicationContext> ctxProvider) {

        org.springframework.context.ApplicationContext ctx = ctxProvider.getIfAvailable();
        CacheService remote = (ctx == null) ? null : getRemoteByName();
        DefaultMultiLevelCacheService multiLevelCacheService = new DefaultMultiLevelCacheService(localCacheService, remote);
        log.trace("[Goya] |- component [framework] GoyaCacheAutoConfiguration |- bean [multiLevelCacheService] register.");
        return multiLevelCacheService;
    }

    @Nullable
    private CacheService getRemoteByName() {
        if (!SpringContext.containsBean("remoteCacheService")) {
            return null;
        }
        Object bean = SpringContext.getBean("remoteCacheService");
        if (bean instanceof CacheService cs) {
            return cs;
        }
        throw new IllegalStateException("Bean 'remoteCacheService' 必须实现 CacheService，实际类型：" + bean.getClass().getName());
    }
}
