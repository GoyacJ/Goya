package com.ysmjjsy.goya.security.core.configuration;

import com.ysmjjsy.goya.security.core.context.SecurityPlatformService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 15:44
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityCoreProperties.class)
public class SecurityCoreAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [core] SecurityCoreAutoConfiguration auto configure.");
    }

    @Bean
    public SecurityPlatformService securityPlatformService(ServerProperties serverProperties, SecurityCoreProperties securityCoreProperties){
        SecurityPlatformService securityPlatformService = new SecurityPlatformService(serverProperties,securityCoreProperties);
        log.trace("[Goya] |- component [core] SecurityCoreAutoConfiguration |- bean [securityPlatformService] register.");
        return securityPlatformService;
    }

}
