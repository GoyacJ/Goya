package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantResolver;
import com.ysmjjsy.goya.component.mybatisplus.tenant.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/28 22:22
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = MybatisPlusConst.PROPERTY_MYBATIS_PLUS + ".tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan("com.ysmjjsy.goya.component.mybatisplus.tenant.profile")
public class MybatisPlusTenantAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration auto configure.");
    }

    @Bean
    public TenantResolver headerTenantResolver() {
        HeaderTenantResolver headerTenantResolver = new HeaderTenantResolver();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [headerTenantResolver] register.");
        return headerTenantResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantProfileStore cachedTenantProfileStore(CacheService cacheService,
                                                       TenantProfileRepository repository) {
        CachedTenantProfileStore cachedTenantProfileStore = new CachedTenantProfileStore(
                cacheService,
                repository,
                Duration.ofMinutes(5),
                Duration.ofMinutes(10)
        );
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [cachedTenantProfileStore] register.");
        return cachedTenantProfileStore;
    }

    @Bean
    @ConditionalOnMissingBean
    public GoyaTenantLineHandler goyaTenantLineHandler(TenantProfileStore tenantProfileStore) {
        GoyaTenantLineHandler goyaTenantLineHandler = new GoyaTenantLineHandler(tenantProfileStore);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [goyaTenantLineHandler] register.");
        return goyaTenantLineHandler;
    }

    /**
     * Web 场景租户路由过滤器。
     * <p>
     * 当 classpath 存在 Servlet API 且启用多租户时生效。
     *
     * @param tenantResolver     租户解析器
     * @param tenantProfileStore 租户画像存储
     * @param props              配置属性
     * @return 过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantRoutingFilter tenantRoutingFilter(TenantResolver tenantResolver,
                                                   TenantProfileStore tenantProfileStore,
                                                   AccessContextResolver accessContextResolver,
                                                   GoyaMybatisPlusProperties props) {
        TenantRoutingFilter tenantRoutingFilter = new TenantRoutingFilter(
                tenantResolver,
                tenantProfileStore,
                accessContextResolver,
                props
        );
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantRoutingFilter] register.");
        return tenantRoutingFilter;
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantRoutingAspect tenantRoutingAspect(TenantResolver tenantResolver,
                                                   TenantProfileStore tenantProfileStore,
                                                   AccessContextResolver accessContextResolver,
                                                   GoyaMybatisPlusProperties props) {
        return new TenantRoutingAspect(
                tenantResolver,
                tenantProfileStore,
                accessContextResolver,
                props
        );
    }

}
