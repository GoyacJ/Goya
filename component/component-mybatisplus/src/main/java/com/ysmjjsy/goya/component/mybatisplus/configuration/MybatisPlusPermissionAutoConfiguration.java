package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.framework.security.context.ResourceResolver;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeDslParser;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilterBuilder;
import com.ysmjjsy.goya.component.framework.security.spi.PolicyRepository;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import com.ysmjjsy.goya.component.mybatisplus.permission.DataResourceResolver;
import com.ysmjjsy.goya.component.mybatisplus.permission.JSqlRangeDslParser;
import com.ysmjjsy.goya.component.mybatisplus.permission.JSqlRangeFilterBuilder;
import com.ysmjjsy.goya.component.mybatisplus.permission.converter.PolicyConverter;
import com.ysmjjsy.goya.component.mybatisplus.permission.converter.ResourceConverter;
import com.ysmjjsy.goya.component.mybatisplus.permission.handler.GoyaDataPermissionHandler;
import com.ysmjjsy.goya.component.mybatisplus.permission.handler.GoyaDataPermissionInterceptor;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourcePolicyMapper;
import com.ysmjjsy.goya.component.mybatisplus.permission.repository.MybatisPolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>动态权限自动配置</p>
 *
 * @author goya
 * @since 2026/1/28 23:26
 */
@Slf4j
@AutoConfiguration
@MapperScan("com.ysmjjsy.goya.component.mybatisplus.permission.mapper")
@ComponentScan("com.ysmjjsy.goya.component.mybatisplus.permission.converter")
@ConditionalOnProperty(prefix = MybatisPlusConst.PROPERTY_MYBATIS_PLUS + ".permission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MybatisPlusPermissionAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration auto configure.");
    }

    /**
     * 默认资源解析器。
     *
     * @return ResourceResolver
     */
    @Bean
    @ConditionalOnMissingBean
    public ResourceResolver dataResourceResolver(DataResourceMapper mapper, ResourceConverter resourceConverter) {
        DataResourceResolver dataResourceResolver = new DataResourceResolver(mapper, resourceConverter);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [dataResourceResolver] register.");
        return dataResourceResolver;
    }

    /**
     * 默认策略仓储。
     *
     * @return PolicyRepository
     */
    @Bean
    @ConditionalOnMissingBean
    public PolicyRepository mybatisPolicyRepository(DataResourcePolicyMapper mapper, PolicyConverter policyConverter) {
        MybatisPolicyRepository mybatisPolicyRepository = new MybatisPolicyRepository(mapper, policyConverter);
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [mybatisPolicyRepository] register.");
        return mybatisPolicyRepository;
    }

    /**
     * DSL 解析器。
     *
     * @return RangeDslParser
     */
    @Bean
    @ConditionalOnMissingBean
    public RangeDslParser jSqlRangeDslParser() {
        JSqlRangeDslParser jSqlRangeDslParser = new JSqlRangeDslParser();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [jSqlRangeDslParser] register.");
        return jSqlRangeDslParser;
    }

    /**
     * 范围过滤器构建器。
     *
     * @return RangeFilterBuilder
     */
    @Bean
    @ConditionalOnMissingBean
    public RangeFilterBuilder jSqlRangeFilterBuilder() {
        JSqlRangeFilterBuilder jSqlRangeFilterBuilder = new JSqlRangeFilterBuilder();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [jSqlRangeFilterBuilder] register.");
        return jSqlRangeFilterBuilder;
    }

    /**
     * 数据权限处理器。
     *
     * @param authorizationService 鉴权服务
     * @param properties           配置项
     * @return GoyaDataPermissionHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public GoyaDataPermissionHandler goyaDataPermissionHandler(AuthorizationService authorizationService,
                                                               GoyaMybatisPlusProperties properties) {
        GoyaDataPermissionHandler goyaDataPermissionHandler = new GoyaDataPermissionHandler(authorizationService, properties.permission());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [goyaDataPermissionHandler] register.");
        return goyaDataPermissionHandler;
    }

    /**
     * 数据权限拦截器。
     *
     * @param handler    处理器
     * @param properties 配置项
     * @return DataPermissionInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionInterceptor goyaDataPermissionInterceptor(GoyaDataPermissionHandler handler,
                                                                   GoyaMybatisPlusProperties properties) {
        GoyaDataPermissionInterceptor goyaDataPermissionInterceptor = new GoyaDataPermissionInterceptor(handler, properties.permission());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [goyaDataPermissionInterceptor] register.");
        return goyaDataPermissionInterceptor;
    }
}
