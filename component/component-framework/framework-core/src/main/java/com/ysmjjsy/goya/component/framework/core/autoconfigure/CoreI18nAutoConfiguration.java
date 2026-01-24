package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.I18nProperties;
import com.ysmjjsy.goya.component.framework.core.i18n.DefaultResolver;
import com.ysmjjsy.goya.component.framework.core.i18n.I18nResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * <p>i18n 基础设施自动装配</p>
 *
 * @author goya
 * @since 2026/1/24 15:25
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(I18nProperties.class)
public class CoreI18nAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] CoreI18nAutoConfiguration auto configure.");
    }

    /**
     * 默认 MessageSource（支持热加载的 ReloadableResourceBundleMessageSource）。
     *
     * <p>生产环境也可使用 Reloadable（不设置 cacheSeconds 或设置较大值即可）。</p>
     *
     * @param props i18n 配置项
     * @return MessageSource
     */
    @Bean
    @ConditionalOnMissingBean(MessageSource.class)
    public MessageSource messageSource(I18nProperties props) {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames(props.baseNames().toArray(String[]::new));
        ms.setDefaultEncoding(props.encoding());
        ms.setUseCodeAsDefaultMessage(props.useCodeAsDefaultMessage());
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(60);
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [messageSource] register.");
        return ms;
    }

    /**
     * 默认 I18nResolver。
     *
     * @param messageSource messageSource
     * @return LocaleProvider
     */
    @Bean
    @ConditionalOnMissingBean(I18nResolver.class)
    public I18nResolver defaultResolver(MessageSource messageSource) {
        DefaultResolver resolver = new DefaultResolver(messageSource);
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [defaultResolver] register.");
        return resolver;
    }

}
