package com.ysmjjsy.goya.component.rabbitmq;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>RabbitMQ Binder：把 Bus 的 Integration 骨架“接线”到 RabbitMQ</p>
 *
 * <p>
 * 重要原则：
 * <ul>
 *   <li>尽量复用官方配置与官方组件：RabbitTemplate / ConnectionFactory（来源于 spring.rabbitmq.* 自动装配）</li>
 *   <li>本类只做接线，不在这里发明重试/DLQ/并发/ACK 等策略（交给 Spring AMQP / Broker 官方能力）</li>
 * </ul>
 * <p>
 * Outbound：SubscribableChannel -> AmqpOutboundEndpoint
 * Inbound：AmqpInboundChannelAdapter -> SubscribableChannel
 * <p>
 * 同时：把 inbound 端（监听容器层面）发生的异常发布到 Bus 全局 error 通道，便于统一观测/告警。
 *
 * @author goya
 * @since 2026/1/27 00:13
 */
@Slf4j
@RequiredArgsConstructor
public final class RabbitIntegrationBusBinder implements BusBinder, DisposableBean {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final BusChannels channels;
    private final BeanFactory beanFactory;

    private final CopyOnWriteArrayList<AmqpInboundChannelAdapter> inboundAdapters = new CopyOnWriteArrayList<>();

    @Override
    public String name() {
        return "rabbit";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        AmqpOutboundEndpoint endpoint = new AmqpOutboundEndpoint(rabbitTemplate);
        endpoint.setExchangeName("");
        endpoint.setRoutingKeyExpression(new LiteralExpression(binding.destination()));
        endpoint.setBeanFactory(beanFactory);
        endpoint.afterPropertiesSet();

        outboundChannel.subscribe((Message<?> msg) -> {
            try {
                endpoint.handleMessage(msg);
            } catch (Exception ex) {
                log.error("RabbitMQ outbound 发送失败: binding='{}', destination='{}'", binding.name(), binding.destination(), ex);
                throw ex;
            }
        });

        log.info("RabbitMQ outbound 已绑定: binding='{}', destination='{}'", binding.name(), binding.destination());
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(binding.destination());

        // 容器层异常发布到 bus error 通道（不吞异常，不替代官方策略）
        container.setErrorHandler(t -> {
            try {
                HashMap<String, Object> headers = new HashMap<>();
                headers.put("stage", "rabbit-inbound-container");
                headers.put("binder", "rabbit");
                headers.put(DefaultBusMessageProducer.HDR_BINDING, binding.name());
                headers.put("destination", binding.destination());
                channels.error().send(new ErrorMessage(t, headers));
            } catch (Exception ex) {
                log.warn("发布到 bus error 通道失败（忽略），原始异常: {}", t.toString(), ex);
            }
        });

        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
        adapter.setOutputChannel(inboundChannel);
        adapter.setBeanFactory(beanFactory);

        adapter.afterPropertiesSet();
        adapter.start();
        inboundAdapters.add(adapter);

        log.info("RabbitMQ inbound 已绑定: binding='{}', destination='{}'", binding.name(), binding.destination());
    }

    @Override
    public void destroy() {
        for (AmqpInboundChannelAdapter adapter : inboundAdapters) {
            try {
                adapter.stop();
            } catch (Exception ex) {
                log.warn("RabbitMQ inbound adapter stop 失败（忽略）", ex);
            }
        }
        inboundAdapters.clear();
    }
}