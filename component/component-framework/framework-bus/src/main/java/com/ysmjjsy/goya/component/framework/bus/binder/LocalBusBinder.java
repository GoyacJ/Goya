package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/**
 * <p>Local in-memory binder: outbound -> inbound within same JVM (no persistence).</p>
 *
 * @author goya
 * @since 2026/1/26 23:44
 */
@Slf4j
public final class LocalBusBinder implements BusBinder {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        // nothing to do; the bridge is created at inbound bind time
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        // Bridge by subscribing outbound channel and forwarding to inbound.
        // In this minimal skeleton, we assume channels were created by runtime and accessible by bus infrastructure.
        // The actual bridge is established in BusBindingLifecycle by using the same channels; here we don't have them.
        // So LocalBusBinder relies on a header-only contract and does not create additional resources.
        log.debug("Local binder bound inbound for '{}'", binding.name());
    }

    /**
     * Utility to wire local bridging when both channels are available.
     */
    public static void bridge(SubscribableChannel outbound, SubscribableChannel inbound, String bindingName) {
        outbound.subscribe((Message<?> msg) -> {
            Message<?> enriched = msg;
            if (!Objects.equals(msg.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING), bindingName)) {
                enriched = MessageBuilder.fromMessage(msg)
                        .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                        .build();
            }
            inbound.send(enriched);
        });
    }
}
