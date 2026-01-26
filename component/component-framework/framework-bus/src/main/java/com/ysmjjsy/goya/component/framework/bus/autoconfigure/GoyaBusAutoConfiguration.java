package com.ysmjjsy.goya.component.framework.bus.autoconfigure;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.LocalBusBinder;
import com.ysmjjsy.goya.component.framework.bus.event.BusEventPublisher;
import com.ysmjjsy.goya.component.framework.bus.event.SpringBusEventPublisher;
import com.ysmjjsy.goya.component.framework.bus.message.BusListenerRegistry;
import com.ysmjjsy.goya.component.framework.bus.message.BusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusListenerRegistry;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.*;
import com.ysmjjsy.goya.component.framework.bus.stream.BusStreamInboundAdapter;
import com.ysmjjsy.goya.component.framework.bus.stream.StreamBridgeBusBinder;
import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

/**
 *
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 00:30
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(BusProperties.class)
public class GoyaBusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaBusAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public BusChannels busChannels(@Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
                                       TaskExecutor applicationTaskExecutor) {
        DefaultBusChannels defaultBusChannels = new DefaultBusChannels(applicationTaskExecutor);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busChannels] register.");
        return defaultBusChannels;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusMessageProducer busMessageProducer(BusChannels channels) {
        DefaultBusMessageProducer defaultBusMessageProducer = new DefaultBusMessageProducer(channels);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busMessageProducer] register.");
        return defaultBusMessageProducer;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusListenerRegistry busListenerRegistry(ApplicationContext applicationContext) {
        DefaultBusListenerRegistry defaultBusListenerRegistry = new DefaultBusListenerRegistry(applicationContext);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busListenerRegistry] register.");
        return defaultBusListenerRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusMessageDispatcher busMessageDispatcher(BusListenerRegistry registry, BusChannels channels) {
        BusMessageDispatcher busMessageDispatcher = new BusMessageDispatcher(registry, channels);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busMessageDispatcher] register.");
        return busMessageDispatcher;
    }

    @Bean
    @ConditionalOnMissingBean
    public BindingResolver bindingResolver(BusProperties props) {
        DefaultBindingResolver bindingResolver = new DefaultBindingResolver(props);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [bindingResolver] register.");
        return bindingResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalBusBinder localBusBinder() {
        LocalBusBinder localBusBinder = new LocalBusBinder();
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [localBusBinder] register.");
        return localBusBinder;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusBinderRegistry busBinderRegistry(List<BusBinder> binders) {
        BusBinderRegistry busBinderRegistry = new BusBinderRegistry(binders);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busBinderRegistry] register.");
        return busBinderRegistry;
    }

    @Bean
    public BusBindingLifecycle busBindingLifecycle(BusProperties props,
                                                   BindingResolver resolver,
                                                   BusChannels channels,
                                                   BusBinderRegistry registry) {
        BusBindingLifecycle busBindingLifecycle = new BusBindingLifecycle(props, resolver, channels, registry);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busBindingLifecycle] register.");
        return busBindingLifecycle;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusErrorLogger busErrorLogger() {
        BusErrorLogger busErrorLogger = new BusErrorLogger();
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busErrorLogger] register.");
        return busErrorLogger;
    }

    @Bean
    public BusErrorSubscriber busErrorSubscriber(BusChannels channels, BusErrorLogger logger) {
        BusErrorSubscriber busErrorSubscriber = new BusErrorSubscriber(channels, logger);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busErrorSubscriber] register.");
        return busErrorSubscriber;
    }

    public static final class BusErrorSubscriber {
        public BusErrorSubscriber(BusChannels channels, BusErrorLogger logger) {
            channels.error().subscribe(logger);
        }
    }

    // ---------------- Event 默认实现（Spring Event） ----------------

    @Bean
    @ConditionalOnMissingBean
    public BusEventPublisher springBusEventPublisher(ApplicationEventPublisher publisher) {
        SpringBusEventPublisher springBusEventPublisher = new SpringBusEventPublisher(publisher);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [springBusEventPublisher] register.");
        return springBusEventPublisher;
    }

    // ---------------- Cloud Stream：可选 StreamBridge binder ----------------

    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.stream.function.StreamBridge")
    @ConditionalOnProperty(prefix = PropertyConst.PROPERTY_BUS, name = "prefer-stream-bridge", havingValue = "true")
    public BusBinder streamBridgeBusBinder(Object streamBridge, BusChannels channels) {
        StreamBridgeBusBinder streamBridgeBusBinder = new StreamBridgeBusBinder(streamBridge, channels);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [streamBridgeBusBinder] register.");
        return streamBridgeBusBinder;
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.stream.function.StreamBridge")
    public BusStreamInboundAdapter busStreamInboundAdapter(BusChannels channels) {
        BusStreamInboundAdapter busStreamInboundAdapter = new BusStreamInboundAdapter(channels);
        log.trace("[Goya] |- component [framework] GoyaBusAutoConfiguration |- bean [busStreamInboundAdapter] register.");
        return busStreamInboundAdapter;
    }
}
