package com.ysmjjsy.goya.component.security.authorization.configuration;

import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import com.ysmjjsy.goya.component.security.authorization.configuration.security.ResourceServerSecurityConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * <p>资源服务自动配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityAuthorizationProperties.class)
@ConditionalOnProperty(prefix = "goya.security.resource", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(ResourceServerSecurityConfiguration.class)
public class SecurityResourceAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authorization] SecurityResourceAutoConfiguration auto configure.");
    }
}
