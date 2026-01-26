package com.ysmjjsy.goya.component.framework.bus.stream;

import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/**
 * <p>Cloud Stream 入站桥接器</p>
 * 用户在函数式 Consumer 中调用 accept(bindingName, message)，将消息投递到 bus inbound(binding)
 *
 * @author goya
 * @since 2026/1/26 23:36
 */
public final class BusStreamInboundAdapter {

    private final BusChannels channels;

    public BusStreamInboundAdapter(BusChannels channels) {
        this.channels = Objects.requireNonNull(channels);
    }

    public void accept(String bindingName, Message<?> message) {
        Message<?> m = message;
        if (m.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING) == null) {
            m = MessageBuilder.fromMessage(m)
                    .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                    .build();
        }
        channels.inbound(bindingName).send(m);
    }
}