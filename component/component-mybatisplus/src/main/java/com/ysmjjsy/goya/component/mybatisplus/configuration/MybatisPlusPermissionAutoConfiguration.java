package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import com.ysmjjsy.goya.component.mybatisplus.permission.GoyaDataPermissionHandler;
import com.ysmjjsy.goya.component.mybatisplus.permission.cache.PermissionPredicateCacheService;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.DefaultPermissionCompiler;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.PermissionCompiler;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ColumnNameValidator;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.DefaultResourceRegistry;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ResourceRegistry;
import com.ysmjjsy.goya.component.mybatisplus.permission.store.PermissionRuleStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/28 23:26
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = MybatisPlusConst.PROPERTY_MYBATIS_PLUS + ".permission", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan("com.ysmjjsy.goya.component.mybatisplus.permission.rule")
public class MybatisPlusPermissionAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration auto configure.");
    }

    @Bean
    public ColumnNameValidator columnNameValidator() {
        ColumnNameValidator columnNameValidator = new ColumnNameValidator();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [columnNameValidator] register.");
        return columnNameValidator;
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceRegistry defaultResourceRegistry() {
        DefaultResourceRegistry defaultResourceRegistry = new DefaultResourceRegistry();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [defaultResourceRegistry] register.");
        return defaultResourceRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionCompiler defaultPermissionCompiler(){
        DefaultPermissionCompiler defaultPermissionCompiler = new DefaultPermissionCompiler();
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [defaultPermissionCompiler] register.");
        return defaultPermissionCompiler;
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionPredicateCacheService permissionPredicateCacheService(CacheService cacheService,
                                                                           PermissionRuleStore ruleStore,
                                                                           PermissionCompiler compiler,
                                                                           ResourceRegistry registry,
                                                                           GoyaMybatisPlusProperties props) {
        PermissionPredicateCacheService permissionPredicateCacheService = new PermissionPredicateCacheService(
                cacheService,
                ruleStore,
                compiler,
                registry,
                props.permission().compiledCacheTtl()
        );
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [permissionPredicateCacheService] register.");
        return permissionPredicateCacheService;
    }

    @Bean
    @ConditionalOnMissingBean
    public GoyaDataPermissionHandler goyaDataPermissionHandler(PermissionPredicateCacheService cacheService,
                                                               ResourceRegistry registry,
                                                               GoyaMybatisPlusProperties props) {
        GoyaDataPermissionHandler goyaDataPermissionHandler = new GoyaDataPermissionHandler(cacheService, registry, props.permission().failClosed());
        log.trace("[Goya] |- component [mybatis-plus] MybatisPlusPermissionAutoConfiguration |- bean [goyaDataPermissionHandler] register.");
        return goyaDataPermissionHandler;
    }
}
