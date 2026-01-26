package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.stream.BusStreamInboundAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;

import java.util.Objects;

/**
 * <p>StreamBridge binder: keeps Integration skeleton but uses Spring Cloud Stream for the actual transport.</p>
 * - Outbound: subscribes outbound channel and forwards to StreamBridge.send(streamBindingName, message)
 * - Inbound: expects application to provide function-style Consumers that delegate to {@link BusStreamInboundAdapter}
 *
 * @author goya
 * @since 2026/1/26 23:44
 */
@Slf4j
public final class StreamBridgeBusBinder implements BusBinder {

    private final StreamBridge streamBridge;
    private final BusProperties props;

    public StreamBridgeBusBinder(StreamBridge streamBridge, BusProperties props) {
        this.streamBridge = Objects.requireNonNull(streamBridge);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public String name() {
        return "stream";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        // Forward each message to StreamBridge using binding.destination as the stream binding name.
        outboundChannel.subscribe((Message<?> msg) -> {
            String busBinding = Objects.toString(msg.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING), binding.name());
            String streamBindingName = resolveStreamBindingName(busBinding, null);
            boolean ok = streamBridge.send(streamBindingName, msg);
            if (!ok) {
                throw new IllegalStateException("StreamBridge.send failed for streamBindingName=" + streamBindingName);
            }
        });
        log.info("StreamBridge outbound bound for '{}'", binding.name());
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        // Inbound is handled via BusStreamInboundAdapter (function bean in user app).
        log.info("StreamBridge inbound for '{}' is handled by BusStreamInboundAdapter (declare Consumer bean).", binding.name());
    }

    /**
     * Resolve the Spring Cloud Stream binding name used by StreamBridge.
     * Default behavior: use framework.bus.bindings.<name>.destination as the stream binding name.
     * Optional: per-send binder override mapping via framework.bus.bindings.<name>.binderBindingNames.<binder>.
     */
    public String resolveStreamBindingName(String bindingName, String overrideBinder) {
        BusProperties.BindingProperties bp = props.bindings().get(bindingName);
        if (bp == null) {
            return bindingName;
        }
        if (overrideBinder != null && !overrideBinder.isBlank()) {
            String mapped = bp.binderBindingNames().get(overrideBinder);
            if (mapped != null && !mapped.isBlank()) {
                return mapped;
            }
            // Convention fallback (documented): <binding>__<binder>
            return bindingName + "__" + overrideBinder;
        }
        return bp.destination();
    }
}