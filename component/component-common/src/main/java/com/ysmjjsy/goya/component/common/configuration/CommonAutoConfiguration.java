package com.ysmjjsy.goya.component.common.configuration;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeRegistry;
import com.ysmjjsy.goya.component.common.context.ApplicationContentPostProcessor;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.i18n.EnumI18nResolver;
import com.ysmjjsy.goya.component.common.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.common.strategy.chain.ChainContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.VirtualThreadTaskExecutor;

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
public class CommonAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [common] CommonAutoConfiguration auto configure.");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [messageSource] register.");
        return source;
    }

    @Bean
    public EnumI18nResolver enumI18nResolver(MessageSource messageSource) {
        EnumI18nResolver resolver = new EnumI18nResolver(messageSource);
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
        log.trace("[Goya] |- component [common] CommonProjectAutoConfiguration |- bean [strategyChoose] register.");
        return bean;
    }

    @Bean
    public ChainContext chainContext() {
        ChainContext bean = new ChainContext();
        log.trace("[Goya] |- component [common] CommonProjectAutoConfiguration |- bean [chainContext] register.");
        return bean;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
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
        log.trace("[Goya] |- component [common] CommonAsyncConfiguration |- bean [scheduledExecutorService] register.");
        return scheduledThreadPoolExecutor;
    }
}
