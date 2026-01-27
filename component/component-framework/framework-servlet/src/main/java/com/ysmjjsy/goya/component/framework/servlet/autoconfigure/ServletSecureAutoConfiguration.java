package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimitedCacheManager;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimitedInterceptor;
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
public class ServletSecureAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [framework] ServletSecureAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessLimitedCacheManager accessLimitedCacheManager(GoyaWebProperties webProperties) {
        AccessLimitedCacheManager accessLimitedStampManager = new AccessLimitedCacheManager(webProperties.accessLimited());
        log.trace("[Goya] |- component [framework] ServletSecureAutoConfiguration |- bean [accessLimitedStampManager] register.");
        return accessLimitedStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AccessLimitedCacheManager.class)
    public AccessLimitedInterceptor accessLimitedInterceptor(AccessLimitedCacheManager accessLimitedCacheManager) {
        AccessLimitedInterceptor interceptor = new AccessLimitedInterceptor(accessLimitedCacheManager);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [accessLimitedInterceptor] register.");
        return interceptor;
    }
}
