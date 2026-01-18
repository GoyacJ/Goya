package com.ysmjjsy.goya.component.security.core.configuration;

import com.ysmjjsy.goya.component.framework.context.GoyaContext;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.context.GoyaSecurityContext;
import com.ysmjjsy.goya.component.web.configuration.WebGoyaContextAutoConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 17:37
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(before = WebGoyaContextAutoConfiguration.class)
public class SecurityGoyaContextAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [core] SecurityGoyaContextAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(GoyaContext.class)
    public GoyaContext securityGoyaContext(ServerProperties serverProperties, SecurityCoreProperties securityCoreProperties) {
        GoyaSecurityContext goyaSecurityContext = new GoyaSecurityContext(serverProperties, securityCoreProperties);
        log.trace("[Goya] |- component [core] SecurityGoyaContextAutoConfiguration |- bean [securityGoyaContext] register.");
        return goyaSecurityContext;
    }
}
