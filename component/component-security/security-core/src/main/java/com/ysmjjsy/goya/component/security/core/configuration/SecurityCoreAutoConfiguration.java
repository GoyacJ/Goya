package com.ysmjjsy.goya.component.security.core.configuration;

import com.ysmjjsy.goya.component.framework.context.GoyaContext;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.context.GoyaSecurityContext;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.ISocialUserService;
import com.ysmjjsy.goya.component.security.core.service.IUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>安全核心模块自动配置</p>
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
    public GoyaContext goyaSecurityContext(ServerProperties serverProperties, SecurityCoreProperties securityCoreProperties) {
        GoyaSecurityContext goyaSecurityContext = new GoyaSecurityContext(serverProperties, securityCoreProperties);
        log.trace("[Goya] |- component [core] SecurityCoreAutoConfiguration |- bean [goyaSecurityContext] register.");
        return goyaSecurityContext;
    }

    @Bean
    public SecurityUserManager securityUserManager(IUserService userService,
                                                   ObjectProvider<ISocialUserService> socialUserServiceProvider) {
        SecurityUserManager securityUserManager = new SecurityUserManager(userService, socialUserServiceProvider);
        log.trace("[Goya] |- component [core] SecurityCoreAutoConfiguration |- bean [securityUserManager] register.");
        return securityUserManager;
    }
}
