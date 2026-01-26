package com.ysmjjsy.goya.component.framework.bus.autoconfigure;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.LocalBusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.StreamBridgeBusBinder;
import com.ysmjjsy.goya.component.framework.bus.event.BusEventPublisher;
import com.ysmjjsy.goya.component.framework.bus.event.SpringBusEventPublisher;
import com.ysmjjsy.goya.component.framework.bus.message.BusListenerRegistry;
import com.ysmjjsy.goya.component.framework.bus.message.BusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusListenerRegistry;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.*;
import com.ysmjjsy.goya.component.framework.bus.stream.BusStreamInboundAdapter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
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
    // -------- Event --------
    @Bean
    @ConditionalOnMissingBean
    public BusEventPublisher busEventPublisher(ApplicationEventPublisher publisher) {
        return new SpringBusEventPublisher(publisher);
    }

    // -------- Runtime core --------
    @Bean
    @ConditionalOnMissingBean
    public BusChannels busChannels(TaskExecutor taskExecutor) {
        DefaultBusChannels defaultBusChannels = new DefaultBusChannels(taskExecutor);
        return defaultBusChannels;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusErrorLogger busErrorLogger() {
        return new BusErrorLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public BindingResolver bindingResolver(BusProperties props) {
        return new BindingResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusBinderRegistry busBinderRegistry(ObjectProvider<List<BusBinder>> bindersProvider,
                                               ObjectProvider<StreamBridgeBusBinder> streamBinderProvider,
                                               BusProperties props) {
        List<BusBinder> binders = new ArrayList<>();
        List<BusBinder> provided = bindersProvider.getIfAvailable();
        if (provided != null) binders.addAll(provided);

        // Always ensure local binder exists
        boolean hasLocal = binders.stream().anyMatch(b -> "local".equalsIgnoreCase(b.name()));
        if (!hasLocal) binders.add(new LocalBusBinder());

        // Optionally add StreamBridge binder if present AND preferStreamBridge=true
        StreamBridgeBusBinder stream = streamBinderProvider.getIfAvailable();
        if (stream != null && props.preferStreamBridge()) {
            boolean hasStream = binders.stream().anyMatch(b -> "stream".equalsIgnoreCase(b.name()));
            if (!hasStream) binders.add(stream);
        }

        return new BusBinderRegistry(binders);
    }

    @Bean
    @ConditionalOnMissingBean
    public BinderSelector binderSelector(BusProperties props, BusBinderRegistry registry) {
        return new BinderSelector(props, registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusBindingLifecycle busBindingLifecycle(BusProperties props, BindingResolver resolver, BusChannels channels, BusBinderRegistry registry) {
        return new BusBindingLifecycle(props, resolver, channels, registry);
    }

    // -------- Messaging --------
    @Bean
    @ConditionalOnMissingBean
    public BusMessageProducer busMessageProducer(BusChannels channels) {
        return new DefaultBusMessageProducer(channels);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusListenerRegistry busListenerRegistry(ApplicationContext applicationContext) {
        return new DefaultBusListenerRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusMessageDispatcher busMessageDispatcher(BusListenerRegistry registry, BusChannels channels) {
        return new BusMessageDispatcher(registry, channels);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusMessageListenerRegistrar busMessageListenerRegistrar(BusMessageDispatcher dispatcher) {
        return new BusMessageListenerRegistrar(dispatcher);
    }

    @Bean
    public SmartInitializer busDispatcherInitializer(BusMessageDispatcher dispatcher) {
        return new SmartInitializer(dispatcher);
    }

    /**
     * 在容器启动完成后，将 inbound 通道与监听器方法建立订阅关系。
     */
    public static final class SmartInitializer implements org.springframework.beans.factory.SmartInitializingSingleton {
        private final BusMessageDispatcher dispatcher;
        public SmartInitializer(BusMessageDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }
        @Override
        public void afterSingletonsInstantiated() {
            dispatcher.subscribeAll();
        }
    }

    // -------- StreamBridge optional --------
    @Bean
    @ConditionalOnClass(StreamBridge.class)
    @ConditionalOnMissingBean
    public StreamBridgeBusBinder streamBridgeBusBinder(StreamBridge streamBridge, BusProperties props) {
        return new StreamBridgeBusBinder(streamBridge, props);
    }

    @Bean
    @ConditionalOnClass(StreamBridge.class)
    @ConditionalOnMissingBean
    public BusStreamInboundAdapter busStreamInboundAdapter(BusChannels channels) {
        return new BusStreamInboundAdapter(channels);
    }

    /**
     * 把 dispatcher 订阅到 inbound 通道。
     */
    @Bean
    public BusInboundWiring busInboundWiring(BusChannels channels, BusMessageDispatcher dispatcher) {
        return new BusInboundWiring(channels, dispatcher);
    }

    /**
     * 把默认错误日志订阅者订阅到 error 通道。
     */
    @Bean
    public BusErrorWiring busErrorWiring(BusChannels channels, BusErrorLogger logger) {
        return new BusErrorWiring(channels, logger);
    }

    /**
     * inbound wiring：独立成 bean，便于未来扩展（比如增加拦截器/过滤器）。
     */
    public static final class BusInboundWiring {
        public BusInboundWiring(BusChannels channels, BusMessageDispatcher dispatcher) {
            channels.inbound().subscribe(dispatcher);
        }
    }

    /**
     * error wiring：独立成 bean，便于未来扩展（比如多个订阅者、按 stage 分流等）。
     */
    public static final class BusErrorWiring {
        public BusErrorWiring(BusChannels channels, BusErrorLogger logger) {
            channels.error().subscribe(logger);
        }
    }
}
