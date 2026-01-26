package com.ysmjjsy.goya.component.framework.bus.stream;

import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/**
 * <p>Helper for Spring Cloud Stream function-style consumers.</p>
 * Usage (in application):
 * <pre>
 * {@code
 *   @Bean
 *   public Consumer<Message<byte[]>> orderCreated(BusStreamInboundAdapter adapter) {
 *       return msg -> adapter.accept("orderCreated", msg);
 *   }
 * }
 * @author goya
 * @since 2026/1/26 23:36
 */
public final class BusStreamInboundAdapter {

    private final BusChannels channels;

    public BusStreamInboundAdapter(BusChannels channels) {
        this.channels = Objects.requireNonNull(channels);
    }

    public void accept(String bindingName, Message<?> msg) {
        Message<?> enriched = MessageBuilder.fromMessage(msg)
                .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                .build();
        channels.inbound(bindingName).send(enriched);
    }
}