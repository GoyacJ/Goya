package com.ysmjjsy.goya.component.framework.bus.message;

import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Bus 入站消息分发器：从 inbound 通道接收消息并分发给业务监听器</p>
 * 关键点：
 * - 业务监听器抛异常时：发布到 error 通道，且继续抛出（不吞异常）
 * - 不在这里实现重试/DLQ：交给官方容器错误处理 + broker 能力
 *
 * @author goya
 * @since 2026/1/27 00:35
 */
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