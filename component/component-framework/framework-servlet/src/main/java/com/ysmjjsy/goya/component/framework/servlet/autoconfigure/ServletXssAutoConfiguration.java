package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.servlet.xss.XssHttpServletFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 09:58
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class ServletXssAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [framework] ServletXssAutoConfiguration auto configure.");
    }
    @Bean
    @ConditionalOnMissingBean
    public XssHttpServletFilter xssHttpServletFilter() {
        XssHttpServletFilter filter = new XssHttpServletFilter();
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [xssHttpServletFilter] register.");
        return filter;
    }
}
