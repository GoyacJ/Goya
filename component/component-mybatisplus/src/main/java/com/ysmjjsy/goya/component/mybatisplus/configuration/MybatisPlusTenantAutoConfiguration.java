package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceRegistrar;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceRouter;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantResolver;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantShardDecider;
import com.ysmjjsy.goya.component.mybatisplus.tenant.aspect.TenantRoutingAspect;
import com.ysmjjsy.goya.component.mybatisplus.tenant.defaults.DefaultTenantDataSourceRegistrar;
import com.ysmjjsy.goya.component.mybatisplus.tenant.defaults.DefaultTenantDataSourceRouter;
import com.ysmjjsy.goya.component.mybatisplus.tenant.defaults.DefaultTenantProfileStore;
import com.ysmjjsy.goya.component.mybatisplus.tenant.defaults.DefaultTenantShardDecider;
import com.ysmjjsy.goya.component.mybatisplus.tenant.filter.GoyaTenantRoutingFilter;
import com.ysmjjsy.goya.component.mybatisplus.tenant.handler.GoyaTenantLineHandler;
import com.ysmjjsy.goya.component.mybatisplus.tenant.mapper.TenantProfileMapper;
import com.ysmjjsy.goya.component.mybatisplus.tenant.web.WebTenantResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.mybatis.spring.annotation.MapperScan;

/**
 * <p>多租户自动配置</p>
 *
 * @author goya
 * @since 2026/1/28 22:22
 */
@Slf4j
@AutoConfiguration
@MapperScan("com.ysmjjsy.goya.component.mybatisplus.tenant.mapper")
@EnableConfigurationProperties(GoyaMybatisPlusProperties.class)
@ConditionalOnProperty(prefix = MybatisPlusConst.PROPERTY_MYBATIS_PLUS + ".tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MybatisPlusTenantAutoConfiguration {

    /**
     * 过滤器顺序，保证在 Spring Security 过滤链（通常为 -100）之后执行。
     */
    private static final int TENANT_ROUTING_FILTER_ORDER = -90;

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration auto configure.");
    }

    /**
     * Web 租户解析器
     *
     * @return TenantResolver
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantResolver webTenantResolver() {
        WebTenantResolver webTenantResolver = new WebTenantResolver();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [webTenantResolver] register.");
        return webTenantResolver;
    }

    /**
     * 默认数据源路由器。
     *
     * @return TenantDataSourceRouter
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantDataSourceRouter defaultTenantDataSourceRouter() {
        DefaultTenantDataSourceRouter defaultTenantDataSourceRouter = new DefaultTenantDataSourceRouter();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [defaultTenantDataSourceRouter] register.");
        return defaultTenantDataSourceRouter;
    }

    /**
     * 默认租户数据源注册器。
     *
     * @param routingDataSource 动态数据源
     * @return TenantDataSourceRegistrar
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DynamicRoutingDataSource.class)
    public TenantDataSourceRegistrar tenantDataSourceRegistrar(DynamicRoutingDataSource routingDataSource) {
        DefaultTenantDataSourceRegistrar registrar = new DefaultTenantDataSourceRegistrar(routingDataSource);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantDataSourceRegistrar] register.");
        return registrar;
    }

    /**
     * 默认租户配置存储。
     *
     * @param mapper TenantProfileMapper
     * @return TenantProfileStore
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantProfileStore tenantProfileStore(TenantProfileMapper mapper) {
        DefaultTenantProfileStore store = new DefaultTenantProfileStore(mapper);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantProfileStore] register.");
        return store;
    }

    /**
     * 默认租户模式决策器。
     *
     * @param profileStore 配置存储
     * @param properties 配置项
     * @return TenantShardDecider
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantShardDecider tenantShardDecider(TenantProfileStore profileStore, GoyaMybatisPlusProperties properties) {
        DefaultTenantShardDecider decider = new DefaultTenantShardDecider(profileStore, properties.tenant());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantShardDecider] register.");
        return decider;
    }

    /**
     * 租户列处理器。
     *
     * @param properties 配置项
     * @return GoyaTenantLineHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public GoyaTenantLineHandler goyaTenantLineHandler(GoyaMybatisPlusProperties properties) {
        GoyaTenantLineHandler goyaTenantLineHandler = new GoyaTenantLineHandler(properties.tenant());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [goyaTenantLineHandler] register.");
        return goyaTenantLineHandler;
    }

    /**
     * TenantLine 拦截器。
     *
     * @param handler 处理器
     * @return TenantLineInnerInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(GoyaTenantLineHandler handler) {
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor(handler);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantLineInnerInterceptor] register.");
        return tenantLineInnerInterceptor;
    }

    /**
     * 租户路由过滤器（Web 环境）。
     *
     * @param resolver     解析器
     * @param profileStore 配置存储
     * @param decider      决策器
     * @param router       路由器
     * @param registrar    数据源注册器
     * @param properties   配置项
     * @return GoyaTenantRoutingFilter
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.filter.OncePerRequestFilter")
    public GoyaTenantRoutingFilter goyaTenantRoutingFilter(TenantResolver resolver,
                                                           TenantProfileStore profileStore,
                                                           TenantShardDecider decider,
                                                           TenantDataSourceRouter router,
                                                           TenantDataSourceRegistrar registrar,
                                                           GoyaMybatisPlusProperties properties) {
        GoyaTenantRoutingFilter goyaTenantRoutingFilter = new GoyaTenantRoutingFilter(resolver, profileStore, decider, router, registrar, properties.tenant());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [goyaTenantRoutingFilter] register.");
        return goyaTenantRoutingFilter;
    }

    /**
     * 租户路由过滤器注册，显式放在 Security 过滤链之后。
     *
     * @param filter 租户路由过滤器
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnMissingBean(name = "goyaTenantRoutingFilterRegistration")
    public FilterRegistrationBean<GoyaTenantRoutingFilter> goyaTenantRoutingFilterRegistration(GoyaTenantRoutingFilter filter) {
        FilterRegistrationBean<GoyaTenantRoutingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("goyaTenantRoutingFilter");
        registration.setOrder(TENANT_ROUTING_FILTER_ORDER);
        return registration;
    }

    /**
     * 租户路由切面（非 Web 场景）。
     *
     * @param resolver     解析器
     * @param profileStore 配置存储
     * @param decider      决策器
     * @param router       路由器
     * @param registrar    数据源注册器
     * @param properties   配置项
     * @return TenantRoutingAspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public TenantRoutingAspect tenantRoutingAspect(TenantResolver resolver,
                                                   TenantProfileStore profileStore,
                                                   TenantShardDecider decider,
                                                   TenantDataSourceRouter router,
                                                   TenantDataSourceRegistrar registrar,
                                                   GoyaMybatisPlusProperties properties) {
        TenantRoutingAspect tenantRoutingAspect = new TenantRoutingAspect(resolver, profileStore, decider, router, registrar, properties.tenant());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusTenantAutoConfiguration |- bean [tenantRoutingAspect] register.");
        return tenantRoutingAspect;
    }
}
