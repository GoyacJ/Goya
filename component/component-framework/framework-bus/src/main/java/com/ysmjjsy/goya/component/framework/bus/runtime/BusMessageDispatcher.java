package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.message.BusListenerRegistry;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>inbound(binding) 通道的消息分发器：把消息分发给业务监听器</p>
 * 注意：
 * - 监听器异常发布到 error 通道
 * - 但继续抛出异常，让官方容器（kafka/amqp/stream）决定是否重试/DLQ/回滚
 *
 * @author goya
 * @since 2026/1/26 23:51
 */
@Slf4j
public final class BusMessageDispatcher implements MessageHandler {
    private final BusListenerRegistry registry;
    private final BusChannels channels;

    public BusMessageDispatcher(BusListenerRegistry registry, BusChannels channels) {
        this.registry = Objects.requireNonNull(registry);
        this.channels = Objects.requireNonNull(channels);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String binding = (String) message.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING);

        try {
            registry.dispatch(binding, message);
        } catch (Exception ex) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("stage", "dispatcher-listener");
            headers.put(DefaultBusMessageProducer.HDR_BINDING, binding);
            headers.put("messageHeaders", message.getHeaders());

            channels.error().send(new ErrorMessage(ex, headers));

            if (ex instanceof MessagingException me) throw me;
            throw new MessagingException(message, ex);
        }
    }
}
