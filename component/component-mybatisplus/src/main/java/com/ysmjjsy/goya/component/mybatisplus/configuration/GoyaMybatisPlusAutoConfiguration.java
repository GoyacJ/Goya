package com.ysmjjsy.goya.component.mybatisplus.configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.permission.GoyaDataPermissionHandler;
import com.ysmjjsy.goya.component.mybatisplus.tenant.GoyaTenantLineHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
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
     * @param props                 配置属性
     * @param tenantLineHandler     租户行处理器
     * @param dataPermissionHandler 动态权限处理器
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(GoyaMybatisPlusProperties props,
                                                         @Autowired(required = false) GoyaTenantLineHandler tenantLineHandler,
                                                         @Autowired(required = false) GoyaDataPermissionHandler dataPermissionHandler) {

        List<InnerInterceptor> chain = new ArrayList<>();

        // 1) 安全护栏：阻断无 WHERE 的 update/delete
        if (props.safety().blockAttack()) {
            chain.add(new BlockAttackInnerInterceptor());
        }

        // 2) 多租户：TenantLine
        if (tenantLineHandler != null) {
            chain.add(new TenantLineInnerInterceptor(tenantLineHandler));
        }

        // 3) 动态权限：DataPermission
        if (props.permission().enabled()) {
            chain.add(new DataPermissionInterceptor(dataPermissionHandler));
        }

        // 4) Pagination（本契约不强制装配，留给上层按需启用）

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        chain.forEach(interceptor::addInnerInterceptor);
        return interceptor;
    }
}
