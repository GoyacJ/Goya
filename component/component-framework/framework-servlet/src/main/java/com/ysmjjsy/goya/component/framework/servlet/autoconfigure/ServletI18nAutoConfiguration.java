package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.I18nProperties;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletI18nProperties;
import com.ysmjjsy.goya.component.framework.servlet.i18n.GoyaLocaleResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 15:29
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@EnableConfigurationProperties(ServletI18nProperties.class)
@Slf4j
public class ServletI18nAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] ServletI18nAutoConfiguration auto configure.");
    }

    /**
     * 注册自定义 LocaleResolver。
     *
     * @param servletProps servlet i18n 配置
     * @param coreProps core i18n 配置（默认 locale）
     * @return LocaleResolver
     */
    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver(ServletI18nProperties servletProps, I18nProperties coreProps) {
        GoyaLocaleResolver resolver = new GoyaLocaleResolver(servletProps, coreProps);
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [localeResolver] register.");
        return resolver;
    }
}
