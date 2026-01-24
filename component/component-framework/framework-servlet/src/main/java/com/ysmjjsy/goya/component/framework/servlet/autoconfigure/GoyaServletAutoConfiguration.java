package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.web.RequestInfoExtractor;
import com.ysmjjsy.goya.component.framework.servlet.web.ServletRequestInfoExtractor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 23:54
 */
@Slf4j
@AutoConfiguration
public class GoyaServletAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaServletAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestInfoExtractor servletRequestInfoExtractor(){
        ServletRequestInfoExtractor servletRequestInfoExtractor = new ServletRequestInfoExtractor();
        log.trace("[Goya] |- component [framework] GoyaServletAutoConfiguration |- bean [servletRequestInfoExtractor] register.");
        return servletRequestInfoExtractor;
    }
}
