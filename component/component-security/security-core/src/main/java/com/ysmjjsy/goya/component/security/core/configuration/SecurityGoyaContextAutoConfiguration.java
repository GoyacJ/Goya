package com.ysmjjsy.goya.component.security.core.configuration;

import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.GoyaContextAutoConfiguration;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.context.GoyaSecurityContext;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 17:37
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(before = GoyaContextAutoConfiguration.class)
public class SecurityGoyaContextAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [core] SecurityGoyaContextAutoConfiguration auto configure.");
    }

    @Bean
    public GoyaContext goyaSecurityContext(ServerProperties serverProperties,
                                           SecurityCoreProperties securityCoreProperties,
                                           SecurityUserManager securityUserManager,
                                           ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        GoyaSecurityContext goyaSecurityContext = new GoyaSecurityContext(
                serverProperties,
                securityCoreProperties,
                securityUserManager,
                jwtDecoderProvider);
        log.trace("[Goya] |- component [core] SecurityGoyaContextAutoConfiguration |- bean [goyaSecurityContext] register.");
        return goyaSecurityContext;
    }
}
