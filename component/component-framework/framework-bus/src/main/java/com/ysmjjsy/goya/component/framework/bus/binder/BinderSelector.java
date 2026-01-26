package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusBinderRegistry;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.Objects;

/**
 * <p>Binder 选择器：决定一条消息应走哪个 binder</p>
 * 优先级：
 * 1) message header 中的 bus.binder（SendOptions 覆盖）
 * 2) binding 配置中的 binder
 * 3) 全局 defaultBinder
 * 4) preferStreamBridge=true 且存在 stream binder
 * 5) 若只有一个非 local binder，选它
 * 6) fallback local
 *
 * @author goya
 * @since 2026/1/26 23:37
 */
public final class BinderSelector {

    private final BusProperties props;
    private final BusBinderRegistry registry;

    public BinderSelector(BusProperties props, BusBinderRegistry registry) {
        this.props = Objects.requireNonNull(props);
        this.registry = Objects.requireNonNull(registry);
    }

    public String selectForOutbound(BusBinding binding, Message<?> msg) {
        String hdr = (String) msg.getHeaders().get(DefaultBusMessageProducer.HDR_BINDER);
        if (hdr != null && !hdr.isBlank()) return hdr.trim();

        if (binding.binder() != null && !binding.binder().isBlank()) return binding.binder().trim();

        if (props.defaultBinder() != null && !props.defaultBinder().isBlank()) return props.defaultBinder().trim();

        if (props.preferStreamBridge() && registry.get("stream").isPresent()) return "stream";

        // 自动推断：只有一个非 local binder 时选它
        Map<String, BusBinder> all = registry.all();
        long nonLocal = all.keySet().stream().filter(n -> !"local".equalsIgnoreCase(n)).count();
        if (nonLocal == 1) {
            return all.keySet().stream().filter(n -> !"local".equalsIgnoreCase(n)).findFirst().orElse("local");
        }

        return "local";
    }

    public String selectForInbound(BusBinding binding) {
        if (binding.binder() != null && !binding.binder().isBlank()) return binding.binder().trim();
        if (props.defaultBinder() != null && !props.defaultBinder().isBlank()) return props.defaultBinder().trim();
        if (props.preferStreamBridge() && registry.get("stream").isPresent()) return "stream";
        return "local";
    }
}

