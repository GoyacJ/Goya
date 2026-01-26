package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.IdempotentCacheManager;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.IdempotentInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 09:58
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class ServletIdempotentAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [framework] ServletIdempotentAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentCacheManager idempotentCacheManager(GoyaWebProperties webProperties) {
        IdempotentCacheManager idempotentStampManager = new IdempotentCacheManager(webProperties.idempotent());
        log.trace("[Goya] |- component [framework] SecurityAutoConfiguration |- bean [idempotentStampManager] register.");
        return idempotentStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(IdempotentCacheManager.class)
    public IdempotentInterceptor idempotentInterceptor(IdempotentCacheManager idempotentCacheManager) {
        IdempotentInterceptor interceptor = new IdempotentInterceptor(idempotentCacheManager);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [idempotentInterceptor] register.");
        return interceptor;
    }
}
