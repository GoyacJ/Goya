package com.ysmjjsy.goya.starter.kafka.configuration;

import com.ysmjjsy.goya.component.bus.processor.BusEventListenerHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * <p>Kafka 自动配置类</p>
 * <p>配置 Kafka Binder，StreamEventPublisher 在 component-bus 中自动注册</p>
 * <p>仅在存在 StreamBridge 时注册（即引入了 spring-cloud-starter-stream-kafka）</p>
 * <p>注意：具体的 Publisher 实现（StreamEventPublisher）在 component-bus 中，使用 @ConditionalOnBean(StreamBridge.class) 条件装配</p>
 * <p>反序列化逻辑已抽取到 component-bus 的 EventDeserializer，供所有 starter 复用</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 * @see StreamBridge
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(StreamBridge.class)
@RequiredArgsConstructor
public class KafkaAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- starter [kafka] KafkaAutoConfiguration auto configure.");
    }

    /**
     * 注册 Kafka 事件消费者
     * <p>基于 Spring Cloud Stream 函数式编程实现消息接收</p>
     * <p>通过 BusEventListenerHandler.handleRemoteMessage() 统一处理远程事件</p>
     * <p>反序列化逻辑已抽取到 component-bus 的 EventDeserializer</p>
     * <p>仅在存在 BusEventListenerHandler 时注册</p>
     *
     * @param handler 事件监听器处理器
     * @return Consumer
     */
    @Bean
    @ConditionalOnBean(BusEventListenerHandler.class)
    public Consumer<Message<?>> kafkaBusEventConsumer(BusEventListenerHandler handler) {
        return message -> {
            if (message == null) {
                log.warn("[Goya] |- starter [kafka] BusEventConsumer |- received null message");
                return;
            }

            // 直接调用 BusEventListenerHandler 的统一处理方法
            // 内部会使用 EventDeserializer 进行反序列化，并路由到对应的监听器
            handler.handleRemoteMessage(message);
        };
    }
}
