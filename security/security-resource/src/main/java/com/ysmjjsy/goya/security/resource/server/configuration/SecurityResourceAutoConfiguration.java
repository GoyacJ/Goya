package com.ysmjjsy.goya.security.resource.server.configuration;

import com.ysmjjsy.goya.security.resource.server.configuration.properties.SecurityResourceProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 15:07
 */
@Slf4j
@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityResourceProperties.class)
public class SecurityResourceAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [resource] SecurityResourceAutoConfiguration auto configure.");
    }

}