package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.GoyaProperties;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.context.GoyaWebContext;
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
 * @since 2026/1/24 23:54
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(GoyaProperties.class)
public class GoyaContextAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaContextAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(GoyaContext.class)
    public GoyaContext goyaWebContext(ServerProperties serverProperties){
        GoyaWebContext goyaWebContext = new GoyaWebContext(serverProperties);
        log.trace("[Goya] |- component [framework] GoyaServletAutoConfiguration |- bean [goyaWebContext] register.");
        return goyaWebContext;
    }
}
