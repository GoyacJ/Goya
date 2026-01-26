package com.ysmjjsy.goya.component.rabbitmq;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;

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
public final class RabbitIntegrationBusBinder implements BusBinder, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(RabbitIntegrationBusBinder.class);

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final BusChannels channels;

    private final java.util.List<AmqpInboundChannelAdapter> inboundAdapters =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    public RabbitIntegrationBusBinder(RabbitTemplate rabbitTemplate,
                                      ConnectionFactory connectionFactory,
                                      BusChannels channels) {
        this.rabbitTemplate = java.util.Objects.requireNonNull(rabbitTemplate);
        this.connectionFactory = java.util.Objects.requireNonNull(connectionFactory);
        this.channels = java.util.Objects.requireNonNull(channels);
    }

    @Override
    public String name() {
        return "rabbit";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        AmqpOutboundEndpoint endpoint = new AmqpOutboundEndpoint(rabbitTemplate);

        // 约定：destination 默认作为 routingKey（即 queue 名），发送到默认 exchange。
        // 贴近 RabbitTemplate 默认行为，不额外引入新的配置概念。
        endpoint.setExchangeName("");
        endpoint.setRoutingKeyExpression(new LiteralExpression(binding.destination()));
        endpoint.afterPropertiesSet();

        outboundChannel.subscribe((Message<?> msg) -> {
            try {
                endpoint.handleMessage(msg);
            } catch (Exception ex) {
                // 不吞异常，让上层继续抛出并发布到 bus error 通道
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

        // 监听容器层异常处理：只做“发布到 bus error 通道”，不改变 Spring AMQP 原有处理语义。
        container.setErrorHandler(t -> {
            try {
                var headers = new HashMap<String, Object>();
                headers.put("stage", "rabbit-inbound-container");
                headers.put("binder", "rabbit");
                headers.put(DefaultBusMessageProducer.HDR_BINDING, binding.name());
                headers.put("destination", binding.destination());
                channels.error().send(new ErrorMessage(t, headers));
            } catch (Exception ex) {
                // 防御：错误处理回调里不要再抛出异常导致更混乱
                log.warn("发布到 bus error 通道失败（忽略），原始异常: {}", t.toString(), ex);
            }
        });

        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
        adapter.setOutputChannel(inboundChannel);

        adapter.afterPropertiesSet();
        adapter.start();
        inboundAdapters.add(adapter);

        log.info("RabbitMQ inbound 已绑定: binding='{}', destination='{}', group='{}'",
                binding.name(), binding.destination(), binding.group());
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