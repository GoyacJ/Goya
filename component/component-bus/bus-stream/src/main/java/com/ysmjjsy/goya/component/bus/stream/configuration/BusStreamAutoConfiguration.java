package com.ysmjjsy.goya.component.bus.stream.configuration;

import com.ysmjjsy.goya.component.bus.stream.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.bus.stream.deserializer.EventClassWhitelist;
import com.ysmjjsy.goya.component.bus.stream.deserializer.EventDeserializer;
import com.ysmjjsy.goya.component.bus.stream.handler.CacheIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.stream.handler.IIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.stream.processor.BusEventListenerHandler;
import com.ysmjjsy.goya.component.bus.stream.processor.BusEventListenerScanner;
import com.ysmjjsy.goya.component.bus.stream.processor.IEventInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.DeserializeInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.IdempotencyInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.InvokeInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.RouteInterceptor;
import com.ysmjjsy.goya.component.bus.stream.publish.LocalBusEventPublisher;
import com.ysmjjsy.goya.component.bus.stream.publish.StreamBusEventPublisher;
import com.ysmjjsy.goya.component.bus.stream.service.DefaultBusService;
import com.ysmjjsy.goya.component.bus.stream.service.IBusService;
import com.ysmjjsy.goya.component.framework.strategy.StrategyChoose;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

/**
 * <p>事件总线自动配置类</p>
 * <p>注册 IBusService、BusEventListenerScanner、BusEventListenerHandler 等核心组件</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties(BusProperties.class)
public class BusStreamAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [bus] BusAutoConfiguration auto configure.");
    }

    /**
     * 注册 IBusService
     * <p>如果已存在则跳过</p>
     *
     * @param strategyChoose 策略选择器
     * @return IBusService 实例
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public IBusService busService(StrategyChoose strategyChoose, BusProperties busProperties) {
        DefaultBusService service = new DefaultBusService(strategyChoose, busProperties);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [busService] register.");
        return service;
    }

    /**
     * 注册 BusEventListenerScanner
     * <p>用于扫描 @BusEventListener 注解的方法，记录元数据</p>
     * <p>使用 static 方法避免 BeanPostProcessor 警告</p>
     * <p>不依赖任何业务 Bean，避免在创建时触发依赖链</p>
     *
     * @return BusEventListenerScanner 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public static BusEventListenerScanner busEventListenerScanner() {
        BusEventListenerScanner scanner = new BusEventListenerScanner();
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [busEventListenerScanner] register.");
        return scanner;
    }

    /**
     * 注册 EventClassWhitelist
     * <p>用于限制可以加载的事件类，防止恶意类加载攻击</p>
     *
     * @param busProperties 总线配置属性
     * @return EventClassWhitelist 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public EventClassWhitelist eventClassWhitelist(BusProperties busProperties) {
        BusProperties.Deserialization deserialization = busProperties.deserialization();
        EventClassWhitelist whitelist = new EventClassWhitelist(deserialization.allowedPackages());
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [eventClassWhitelist] register with packages: [{}]",
                deserialization.allowedPackages());
        return whitelist;
    }

    /**
     * 注册 EventDeserializer
     * <p>负责从 Message<?> 中反序列化事件，供所有 starter 复用</p>
     *
     * @param whitelistProvider 类加载白名单提供者（延迟注入）
     * @return EventDeserializer 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public EventDeserializer eventDeserializer(ObjectProvider<EventClassWhitelist> whitelistProvider) {
        EventDeserializer deserializer = new EventDeserializer(whitelistProvider);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [eventDeserializer] register.");
        return deserializer;
    }

    /**
     * 注册 BusEventListenerHandler
     * <p>负责事件路由、幂等性检查等业务逻辑</p>
     * <p>作为普通 Bean，可以正常依赖业务组件</p>
     *
     * @param scanner                     扫描器
     * @param idempotencyHandlerProvider  幂等性处理器提供者（延迟注入）
     * @param eventDeserializerProvider  事件反序列化器提供者（延迟注入）
     * @param interceptorsProvider       拦截器列表提供者（延迟注入）
     * @return BusEventListenerHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BusEventListenerHandler busEventListenerHandler(BusEventListenerScanner scanner,
                                                           ObjectProvider<IIdempotencyHandler> idempotencyHandlerProvider,
                                                           ObjectProvider<EventDeserializer> eventDeserializerProvider,
                                                           ObjectProvider<java.util.List<IEventInterceptor>> interceptorsProvider,
                                                           BusProperties busProperties) {
        BusEventListenerHandler handler = new BusEventListenerHandler(scanner, idempotencyHandlerProvider, eventDeserializerProvider, interceptorsProvider, busProperties);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [busEventListenerHandler] register.");
        return handler;
    }

    /**
     * 注册默认拦截器
     * <p>按顺序注册：DeserializeInterceptor、IdempotencyInterceptor、RouteInterceptor、InvokeInterceptor</p>
     *
     * @param scanner                     扫描器
     * @param idempotencyHandlerProvider  幂等性处理器提供者
     * @param eventDeserializerProvider  事件反序列化器提供者
     * @return 默认拦截器列表
     */
    @Bean
    @ConditionalOnMissingBean(name = "eventInterceptors")
    public List<IEventInterceptor> eventInterceptors(
            BusEventListenerScanner scanner,
            ObjectProvider<IIdempotencyHandler> idempotencyHandlerProvider,
            ObjectProvider<EventDeserializer> eventDeserializerProvider,
            BusProperties busProperties) {
        List<IEventInterceptor> interceptors = new java.util.ArrayList<>();
        interceptors.add(new DeserializeInterceptor(eventDeserializerProvider));
        interceptors.add(new IdempotencyInterceptor(idempotencyHandlerProvider));
        interceptors.add(new RouteInterceptor(scanner));
        interceptors.add(new InvokeInterceptor(idempotencyHandlerProvider, busProperties));
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [eventInterceptors] register with [{}] interceptors.",
                interceptors.size());
        return interceptors;
    }

    /**
     * 注册幂等性处理器
     *
     * @param cacheService  缓存服务
     * @param busProperties 总线配置属性
     * @return IIdempotencyHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public IIdempotencyHandler idempotencyHandler(ICacheService cacheService, BusProperties busProperties) {
        CacheIdempotencyHandler cacheIdempotencyHandler = new CacheIdempotencyHandler(cacheService, busProperties);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [idempotencyHandler] register.");
        return cacheIdempotencyHandler;
    }

    /**
     * 注册本地事件发布器
     *
     * @param eventPublisher 应用事件发布器
     * @param handlerProvider 事件监听器处理器提供者
     * @return LocalEventPublisher 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalBusEventPublisher localEventPublisher(ApplicationEventPublisher eventPublisher,
                                                      ObjectProvider<BusEventListenerHandler> handlerProvider) {
        LocalBusEventPublisher publisher = new LocalBusEventPublisher(eventPublisher, handlerProvider);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [localEventPublisher] register.");
        return publisher;
    }

    /**
     * 注册 StreamEventPublisher
     * <p>使用 ObjectProvider 延迟注入，避免加载顺序问题</p>
     * <p>如果 StreamBridge 不存在，则返回 null（不会报错）</p>
     * <p>在发布远程事件时，如果 StreamEventPublisher 不存在，会打印警告日志</p>
     *
     * @param streamBridgeProvider StreamBridge 提供者（延迟注入）
     * @return StreamEventPublisher 实例，如果 StreamBridge 不存在则返回 null
     */
    @Bean
    @ConditionalOnMissingBean
    public StreamBusEventPublisher streamEventPublisher(ObjectProvider<StreamBridge> streamBridgeProvider) {
        StreamBridge streamBridge = streamBridgeProvider.getIfAvailable();
        if (streamBridge == null) {
            log.debug("[Goya] |- component [bus] BusAutoConfiguration |- StreamBridge not available, StreamEventPublisher will not be created");
            return null;
        }
        StreamBusEventPublisher publisher = new StreamBusEventPublisher(streamBridge);
        log.trace("[Goya] |- component [bus] BusAutoConfiguration |- bean [streamEventPublisher] register.");
        return publisher;
    }

}

