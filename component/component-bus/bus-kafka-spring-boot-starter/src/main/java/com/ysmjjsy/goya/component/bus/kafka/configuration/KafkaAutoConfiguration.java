package com.ysmjjsy.goya.component.bus.kafka.configuration;

import com.ysmjjsy.goya.component.bus.kafka.publish.KafkaStreamEventPublisher;
import com.ysmjjsy.goya.component.bus.stream.processor.BusEventListenerHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
     * 注册延迟消息调度器
     * <p>用于实现 Kafka 延迟消息功能（Kafka 不支持原生延迟消息）</p>
     * <p>线程池大小固定为 10，适用于大多数场景。如需自定义，可以通过注册同名 Bean 覆盖</p>
     * <p><strong>注意</strong>：延迟消息使用内存调度，应用重启会丢失未发送的延迟消息</p>
     *
     * @return ScheduledExecutorService
     */
    @Bean
    @ConditionalOnMissingBean(name = "delayedMessageScheduler")
    public ScheduledExecutorService delayedMessageScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10, r -> {
            Thread t = new Thread(r, "kafka-delayed-message-scheduler");
            t.setDaemon(true);
            return t;
        });
        log.trace("[Goya] |- starter [kafka] KafkaAutoConfiguration |- bean [delayedMessageScheduler] register.");
        return scheduler;
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

    /**
     * 注册 Kafka 流事件发布器
     * <p>实现 IRemoteEventPublisher，提供 Kafka 特定的事件发布能力</p>
     * <p>支持延迟消息（通过 ScheduledExecutorService）、顺序消息（通过分区键）、分区</p>
     *
     * @param streamBridge StreamBridge 实例
     * @param delayedMessageScheduler 延迟消息调度器
     * @return KafkaStreamEventPublisher 实例
     */
    @Bean
    @ConditionalOnMissingBean(KafkaStreamEventPublisher.class)
    public KafkaStreamEventPublisher kafkaStreamEventPublisher(StreamBridge streamBridge, ScheduledExecutorService delayedMessageScheduler) {
        KafkaStreamEventPublisher publisher = new KafkaStreamEventPublisher(streamBridge, delayedMessageScheduler);
        log.trace("[Goya] |- starter [kafka] KafkaAutoConfiguration |- bean [kafkaStreamEventPublisher] register.");
        return publisher;
    }
}
