package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.servlet.tenant.MultiTenantInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/15 09:39
 */
@Slf4j
@AutoConfiguration
public class ServletTenantAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] ServletTenantAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiTenantInterceptor tenantInterceptor() {
        MultiTenantInterceptor multiTenantInterceptor = new MultiTenantInterceptor();
        log.trace("[Goya] |- component [framework] ServletTenantAutoConfiguration |- bean [tenantInterceptor] register.");
        return multiTenantInterceptor;
    }
}
