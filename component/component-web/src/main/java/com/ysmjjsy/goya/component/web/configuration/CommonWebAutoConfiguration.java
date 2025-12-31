package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.common.service.IPlatformService;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import com.ysmjjsy.goya.component.web.service.WebPlatformService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:17
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
public class CommonWebAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[GOYA] |- component [web] DefaultServiceContextHolderAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(IPlatformService.class)
    public IPlatformService iPlatformService(ServerProperties serverProperties) {
        WebPlatformService defaultServiceContextHolder = new WebPlatformService(serverProperties);
        log.trace("[GOYA] |- component [web] DefaultServiceContextHolderAutoConfiguration |- bean [iPlatformService] register.");
        return defaultServiceContextHolder;
    }
}
