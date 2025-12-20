package com.ysmjjsy.goya.component.common.configuration;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeRegistry;
import com.ysmjjsy.goya.component.common.configuration.properties.PlatformProperties;
import com.ysmjjsy.goya.component.common.context.ApplicationContentPostProcessor;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.i18n.I18nResolver;
import com.ysmjjsy.goya.component.common.jackson.TimeJacksonComponent;
import com.ysmjjsy.goya.component.common.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.common.strategy.chain.ChainContext;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>common configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(PlatformProperties.class)
@Import(TimeJacksonComponent.class)
public class CommonAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [common] CommonAutoConfiguration auto configure.");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        source.setCacheSeconds(3600);
        source.setFallbackToSystemLocale(false);
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [messageSource] register.");
        return source;
    }

    @Bean
    @Lazy(false)
    public I18nResolver i18nResolver(MessageSource messageSource) {
        I18nResolver resolver = new I18nResolver(messageSource);
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [enumI18nResolver] register.");
        return resolver;
    }

    @Bean
    public ResponseCodeRegistry responseCodeRegistry(List<IResponseCode> codes) {
        ResponseCodeRegistry registry = new ResponseCodeRegistry(codes);
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [responseCodeRegistry] register.");
        return registry;
    }

    @Bean
    public ApplicationContentPostProcessor applicationContentPostProcessor() {
        ApplicationContentPostProcessor bean = new ApplicationContentPostProcessor();
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [applicationContentPostProcessor] register.");
        return bean;
    }

    @Bean
    public StrategyChoose strategyChoose() {
        StrategyChoose bean = new StrategyChoose();
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [strategyChoose] register.");
        return bean;
    }

    @Bean
    public ChainContext chainContext() {
        ChainContext bean = new ChainContext();
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [chainContext] register.");
        return bean;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        // daemon 必须为 true
        BasicThreadFactory.Builder builder = BasicThreadFactory.builder().daemon(true);
        if (SpringContext.isVirtual()) {
            builder.namingPattern("virtual-schedule-pool-%d").wrappedFactory(new VirtualThreadTaskExecutor().getVirtualThreadFactory());
        } else {
            builder.namingPattern("schedule-pool-%d");
        }
        int core = Runtime.getRuntime().availableProcessors() + 1;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core,
                builder.build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
            }
        };
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [scheduledExecutorService] register.");
        return scheduledThreadPoolExecutor;
    }

    @Bean
    public JsonMapper jacksonJsonMapper(JsonMapper.Builder builder) {
        return builder.build();
    }

    @Bean
    @Lazy(false)
    public JsonUtils jsonUtils(){
        JsonUtils jsonUtils = new JsonUtils();
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [jsonUtils] register.");
        return jsonUtils;
    }
}
