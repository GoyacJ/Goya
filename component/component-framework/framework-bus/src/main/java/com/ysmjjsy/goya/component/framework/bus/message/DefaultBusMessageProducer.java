package com.ysmjjsy.goya.component.framework.bus.message;

import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>默认的消息发送实现：把 envelope 转成 Spring Message 丢进 outbound 通道.</p>
 * <p>
 * 关键点：
 * - 发送异常时：发布 ErrorMessage 到 Bus error 通道（便于观测）
 * - 但仍然抛出异常：不吞异常，避免改变上层调用的语义
 *
 * @author goya
 * @since 2026/1/26 23:47
 */
public final class DefaultBusMessageProducer implements BusMessageProducer {

    /** header：binding 名（用于路由/观测） */
    public static final String HDR_BINDING = "bus.binding";

    /** header：本次发送强制 binder（用于 per-send override） */
    public static final String HDR_BINDER = "bus.binder";

    private final BusChannels channels;

    public DefaultBusMessageProducer(BusChannels channels) {
        this.channels = Objects.requireNonNull(channels);
    }

    @Override
    public <T> void send(String binding, MessageEnvelope<T> envelope) {
        send(binding, envelope, SendOptions.DEFAULT);
    }

    @Override
    public <T> void send(String binding, MessageEnvelope<T> envelope, SendOptions options) {
        Objects.requireNonNull(binding, "binding 不能为空");
        Objects.requireNonNull(envelope, "envelope 不能为空");
        if (options == null) options = SendOptions.DEFAULT;

        MessageBuilder<MessageEnvelope<T>> builder = MessageBuilder.withPayload(envelope)
                .setHeader(HDR_BINDING, binding);

        if (options.hasBinderOverride()) {
            builder.setHeader(HDR_BINDER, options.binder());
        }

        Message<MessageEnvelope<T>> msg = builder.build();

        try {
            boolean ok = channels.outbound(binding).send(msg);
            if (!ok) {
                IllegalStateException ex = new IllegalStateException("outbound channel send 返回 false");
                publishToErrorChannel(ex, "producer-outbound-send-false", binding, msg);
                throw ex;
            }
        } catch (Exception ex) {
            publishToErrorChannel(ex, "producer-outbound-exception", binding, msg);
            throw ex;
        }
    }

    private void publishToErrorChannel(Throwable ex, String stage, String binding, Object payload) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("stage", stage);
        headers.put(HDR_BINDING, binding);
        if (payload != null) headers.put("payload", payload);
        channels.error().send(new ErrorMessage(ex, headers));
    }
}