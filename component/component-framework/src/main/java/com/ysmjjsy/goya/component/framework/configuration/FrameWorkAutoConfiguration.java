package com.ysmjjsy.goya.component.framework.configuration;

import com.ysmjjsy.goya.component.core.event.EventPublisher;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import com.ysmjjsy.goya.component.framework.event.LocalEventPublisher;
import com.ysmjjsy.goya.component.framework.i18n.DefaultResolver;
import com.ysmjjsy.goya.component.framework.i18n.I18nResolver;
import com.ysmjjsy.goya.component.framework.processor.ApplicationContentPostProcessor;
import com.ysmjjsy.goya.component.framework.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.framework.strategy.chain.ChainContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:06
 */
@Slf4j
@AutoConfiguration
public class FrameWorkAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- framework [framework] FrameWorkAutoConfiguration auto configure.");
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
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [messageSource] register.");
        return source;
    }

    @Bean
    @Lazy(false)
    @ConditionalOnMissingBean(I18nResolver.class)
    public I18nResolver defaultI18nResolver(MessageSource messageSource) {
        DefaultResolver defaultResolver = new DefaultResolver(messageSource);
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [defaultI18nResolver] register.");
        return defaultResolver;
    }

    @Bean
    public ApplicationContentPostProcessor applicationContentPostProcessor() {
        ApplicationContentPostProcessor bean = new ApplicationContentPostProcessor();
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [applicationContentPostProcessor] register.");
        return bean;
    }

    @Bean
    public StrategyChoose strategyChoose() {
        StrategyChoose bean = new StrategyChoose();
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [strategyChoose] register.");
        return bean;
    }

    @Bean
    public ChainContext<?> chainContext() {
        ChainContext<?> bean = new ChainContext<>();
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [chainContext] register.");
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
        };
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [scheduledExecutorService] register.");
        return scheduledThreadPoolExecutor;
    }

    @Bean
    @Lazy(false)
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher localEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        LocalEventPublisher localEventPublisher = new LocalEventPublisher(applicationEventPublisher);
        log.trace("[Goya] |- framework [framework] FrameWorkAutoConfiguration |- bean [localEventPublisher] register.");
        return localEventPublisher;
    }
}
