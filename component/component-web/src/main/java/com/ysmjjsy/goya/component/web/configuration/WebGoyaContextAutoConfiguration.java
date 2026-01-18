package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.framework.context.GoyaContext;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import com.ysmjjsy.goya.component.web.context.GoyaWebContext;
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
public class WebGoyaContextAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [web] DefaultServiceContextHolderAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(GoyaContext.class)
    public GoyaContext goyaWebContext(ServerProperties serverProperties) {
        GoyaWebContext goyaWebContext = new GoyaWebContext(serverProperties);
        log.trace("[Goya] |- component [web] DefaultServiceContextHolderAutoConfiguration |- bean [iPlatformService] register.");
        return goyaWebContext;
    }
}
