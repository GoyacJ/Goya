package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaNetUtils;
import com.ysmjjsy.goya.component.framework.servlet.web.GlobalExceptionHandler;
import com.ysmjjsy.goya.component.mybatisplus.audit.AuditorProvider;
import com.ysmjjsy.goya.component.mybatisplus.audit.GoyaMetaObjectHandler;
import com.ysmjjsy.goya.component.mybatisplus.audit.defaults.DefaultAuditorProvider;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.context.filter.AccessContextFilter;
import com.ysmjjsy.goya.component.mybatisplus.context.web.WebAccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.exception.MybatisExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>MyBatis-Plus 自动配置</p>
 *
 * @author goya
 * @since 2026/1/24 01:29
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(GoyaMybatisPlusProperties.class)
public class GoyaMybatisPlusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration auto configure.");
    }

    /**
     * MyBatis-Plus 总拦截器（包含各 InnerInterceptor，顺序固定）。
     * <p>
     * 上层若需要额外插件（如分页），建议通过覆盖 {@link MybatisPlusInterceptor} Bean 的方式统一控制，
     * 或在本模块后续提供“可选分页装配”（但不改变本模块顺序约束）。
     *
     * @param props                  配置属性
     * @param tenantLineProvider     租户拦截器
     * @param dataPermissionProvider 权限拦截器
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(GoyaMybatisPlusProperties props,
                                                         ObjectProvider<TenantLineInnerInterceptor> tenantLineProvider,
                                                         ObjectProvider<DataPermissionInterceptor> dataPermissionProvider,
                                                         BlockAttackInnerInterceptor blockAttackInnerInterceptor,
                                                         PaginationInnerInterceptor paginationInnerInterceptor,
                                                         OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor) {

        List<InnerInterceptor> chain = new ArrayList<>();
        if (props.safety().blockAttack()) {
            chain.add(blockAttackInnerInterceptor);
        }

        // TenantLine 多租户插件 必须放到第一位
        TenantLineInnerInterceptor tenantLine = tenantLineProvider.getIfAvailable();
        if (props.tenant().enabled() && tenantLine != null) {
            chain.add(tenantLine);
        }

        // DataPermission（仅查询）
        DataPermissionInterceptor dataPermission = dataPermissionProvider.getIfAvailable();
        if (props.permission().enabled() && dataPermission != null) {
            chain.add(dataPermission);
        }

        // Pagination
        chain.add(paginationInnerInterceptor);

        // 乐观锁
        chain.add(optimisticLockerInnerInterceptor);

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        chain.forEach(interceptor::addInnerInterceptor);
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [mybatisPlusInterceptor] register.");
        return interceptor;
    }

    /**
     * 审计人提供者。
     *
     * @return AuditorProvider
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditorProvider defaultAuditorProvider() {
        DefaultAuditorProvider defaultAuditorProvider = new DefaultAuditorProvider();
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [defaultAuditorProvider] register.");
        return defaultAuditorProvider;
    }

    /**
     * 审计字段处理器。
     *
     * @param auditorProvider 审计人提供者
     * @return MetaObjectHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler goyaMetaObjectHandler(AuditorProvider auditorProvider) {
        GoyaMetaObjectHandler goyaMetaObjectHandler = new GoyaMetaObjectHandler(auditorProvider);
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [goyaMetaObjectHandler] register.");
        return goyaMetaObjectHandler;
    }

    /**
     * 使用网卡信息绑定雪花生成器
     * 防止集群雪花ID重复
     */
    @Bean
    public IdentifierGenerator idGenerator() {
        return new DefaultIdentifierGenerator(GoyaNetUtils.getLocalhost());
    }

    /**
     * 默认访问上下文解析器（基于请求头）。
     *
     * @return AccessContextResolver
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.filter.OncePerRequestFilter")
    public AccessContextResolver accessContextResolver() {
        WebAccessContextResolver resolver = new WebAccessContextResolver();
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [accessContextResolver] register.");
        return resolver;
    }

    /**
     * 访问上下文过滤器（Web 环境）。
     *
     * @param resolver 解析器
     * @return AccessContextFilter
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.filter.OncePerRequestFilter")
    public AccessContextFilter accessContextFilter(AccessContextResolver resolver) {
        AccessContextFilter filter = new AccessContextFilter(resolver);
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [accessContextFilter] register.");
        return filter;
    }

    /**
     * BlockAttack 拦截器。
     *
     * @return BlockAttackInnerInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public BlockAttackInnerInterceptor blockAttackInnerInterceptor() {
        BlockAttackInnerInterceptor blockAttackInnerInterceptor = new BlockAttackInnerInterceptor();
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [blockAttackInnerInterceptor] register.");
        return blockAttackInnerInterceptor;
    }

    /**
     * 分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 分页合理化
        paginationInnerInterceptor.setOverflow(true);
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [paginationInnerInterceptor] register.");
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件
     */
    @Bean
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [optimisticLockerInnerInterceptor] register.");
        return optimisticLockerInnerInterceptor;
    }

    /**
     * 异常处理器
     */
    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler(GlobalExceptionHandler globalExceptionHandler) {
        MybatisExceptionHandler mybatisExceptionHandler = new MybatisExceptionHandler(globalExceptionHandler);
        log.trace("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration |- bean [mybatisExceptionHandler] register.");
        return mybatisExceptionHandler;
    }
}
