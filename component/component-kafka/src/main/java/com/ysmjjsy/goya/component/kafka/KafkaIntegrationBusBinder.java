package com.ysmjjsy.goya.component.kafka;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>Kafka Binder：把 Bus 的 Integration 骨架“接线”到 Kafka</p>
 * <p>
 * 重要原则：
 * <ul>
 *   <li>尽量复用官方配置与官方组件：KafkaTemplate / ConsumerFactory（来源于 spring.kafka.* 自动装配）</li>
 *   <li>本类只做接线，不在这里发明重试/DLQ/并发/ACK 等策略（交给 Spring Kafka / Broker 官方能力）</li>
 * </ul>
 * <p>
 * Outbound：SubscribableChannel -> KafkaProducerMessageHandler
 * Inbound：KafkaMessageDrivenChannelAdapter -> SubscribableChannel
 * 同时：把 inbound 端（监听容器层面）发生的异常发布到 Bus 全局 error 通道，便于统一观测/告警。
 *
 * @author goya
 * @since 2026/1/27 00:08
 */
public final class KafkaIntegrationBusBinder implements BusBinder, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaIntegrationBusBinder.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    /**
     * 关键：用官方的 ConcurrentKafkaListenerContainerFactory 创建容器，
     * 这样可以继承 spring.kafka.* 以及用户对工厂的自定义（并发、ack、errorHandler 等）。
     */
    private final ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory;

    /**
     * Bus 运行时通道（包含全局 error 通道）
     */
    private final BusChannels channels;

    /**
     * 记录创建的 inbound adapter，便于应用关闭时优雅 stop。
     */
    private final List<KafkaMessageDrivenChannelAdapter<Object, Object>> inboundAdapters = new CopyOnWriteArrayList<>();

    public KafkaIntegrationBusBinder(KafkaTemplate<Object, Object> kafkaTemplate,
                                     ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory,
                                     BusChannels channels) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.containerFactory = Objects.requireNonNull(containerFactory);
        this.channels = Objects.requireNonNull(channels);
    }

    @Override
    public String name() {
        return "kafka";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        KafkaProducerMessageHandler<Object, Object> handler = new KafkaProducerMessageHandler<>(kafkaTemplate);
        handler.setTopicExpressionString("'" + binding.destination() + "'");
        handler.afterPropertiesSet();

        outboundChannel.subscribe((Message<?> msg) -> {
            try {
                handler.handleMessage(msg);
            } catch (Exception ex) {
                // 注意：这里不吞异常，让上层（DefaultBusMessageProducer）继续抛出并发布到 bus error 通道
                log.error("Kafka outbound 发送失败: binding='{}', destination='{}'", binding.name(), binding.destination(), ex);
                throw ex;
            }
        });

        log.info("Kafka outbound 已绑定: binding='{}', destination='{}'", binding.name(), binding.destination());
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        // 使用官方工厂创建容器（继承 spring.kafka.* 以及用户对工厂的自定义）
        ConcurrentMessageListenerContainer<Object, Object> container =
                containerFactory.createContainer(binding.destination());

        // 如果用户在 framework.bus.bindings.* 指定 group，则覆盖 group.id
        if (StringUtils.hasText(binding.group())) {
            container.getContainerProperties().setGroupId(binding.group());
        }

        // 装饰（不替换）已有的 CommonErrorHandler：发布到 bus error 通道 + 继续走官方处理逻辑
        CommonErrorHandler existing = container.getCommonErrorHandler();
        container.setCommonErrorHandler(new KafkaBusPublishingCommonErrorHandler(existing, channels, binding));

        // 用 Spring Integration 官方 adapter，把消费到的消息投递到 inboundChannel
        KafkaMessageDrivenChannelAdapter<Object, Object> adapter = new KafkaMessageDrivenChannelAdapter<>(container);
        adapter.setOutputChannel(inboundChannel);

        adapter.afterPropertiesSet();
        adapter.start();

        inboundAdapters.add(adapter);

        log.info("Kafka inbound 已绑定: binding='{}', destination='{}', group='{}'",
                binding.name(), binding.destination(), binding.group());
    }

    @Override
    public void destroy() {
        for (KafkaMessageDrivenChannelAdapter<Object, Object> adapter : inboundAdapters) {
            try {
                adapter.stop();
            } catch (Exception ex) {
                log.warn("Kafka inbound adapter stop 失败（忽略）", ex);
            }
        }
        inboundAdapters.clear();
    }
}