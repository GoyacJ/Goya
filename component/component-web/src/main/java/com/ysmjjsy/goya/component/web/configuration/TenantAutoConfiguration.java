package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.web.tenant.MultiTenantInterceptor;
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
public class TenantAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[GOYA] |- component [web] TenantAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiTenantInterceptor tenantInterceptor() {
        MultiTenantInterceptor multiTenantInterceptor = new MultiTenantInterceptor();
        log.trace("[GOYA] |- Bean [Idempotent Interceptor] Configure.");
        return multiTenantInterceptor;
    }
}
