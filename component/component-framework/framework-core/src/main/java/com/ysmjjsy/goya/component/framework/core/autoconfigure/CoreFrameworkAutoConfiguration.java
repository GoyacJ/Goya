package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.core.processor.ApplicationContentPostProcessor;
import com.ysmjjsy.goya.component.framework.core.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.framework.core.strategy.chain.ChainContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
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
public class CoreFrameworkAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] FrameWorkAutoConfiguration auto configure.");
    }

    @Bean
    public ApplicationContentPostProcessor applicationContentPostProcessor() {
        ApplicationContentPostProcessor bean = new ApplicationContentPostProcessor();
        log.trace("[Goya] |- component [framework] FrameWorkAutoConfiguration |- bean [applicationContentPostProcessor] register.");
        return bean;
    }

    @Bean
    public StrategyChoose strategyChoose() {
        StrategyChoose bean = new StrategyChoose();
        log.trace("[Goya] |- component [framework] FrameWorkAutoConfiguration |- bean [strategyChoose] register.");
        return bean;
    }

    @Bean
    public ChainContext<?> chainContext() {
        ChainContext<?> bean = new ChainContext<>();
        log.trace("[Goya] |- component [framework] FrameWorkAutoConfiguration |- bean [chainContext] register.");
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
        log.trace("[Goya] |- component [framework] FrameWorkAutoConfiguration |- bean [scheduledExecutorService] register.");
        return scheduledThreadPoolExecutor;
    }
}
