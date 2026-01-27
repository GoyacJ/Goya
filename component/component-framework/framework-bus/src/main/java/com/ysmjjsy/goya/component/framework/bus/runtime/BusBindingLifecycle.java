package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.binder.LocalBusBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.messaging.SubscribableChannel;

import java.util.Map;

/**
 * <p>Initializes bindings at startup: creates per-binding channels and asks binders to bind them.</p>
 *
 * @author goya
 * @since 2026/1/26 23:50
 */
@Slf4j
@RequiredArgsConstructor
public final class BusBindingLifecycle implements SmartInitializingSingleton {

    private final BusProperties props;
    private final BindingResolver resolver;
    private final BusChannels channels;
    private final BusBinderRegistry registry;

    @Override
    public void afterSingletonsInstantiated() {
        for (Map.Entry<String, BusProperties.BindingProperties> e : resolver.all().entrySet()) {
            String name = e.getKey();
            BusBinding binding = resolver.resolve(name);
            SubscribableChannel outbound = channels.outbound(name);
            SubscribableChannel inbound = channels.inbound(name);

            // Always bind inbound/outbound with the selected binder at binding level.
            // Per-send overrides are handled at send-time by selecting binder and (optionally) mapping stream binding names.
            String binderName = (binding.binder() != null && !binding.binder().isBlank())
                    ? binding.binder()
                    : props.defaultBinder();

            BusBinder binder = (binderName != null && !binderName.isBlank())
                    ? registry.get(binderName).orElse(null)
                    : null;

            if (binder == null) {
                // If StreamBridge is preferred and available, you may not have kafka/rabbit binders registered.
                // Fallback to local unless user provided default binder.
                binder = registry.get("local").orElseThrow();
            }

            // Local binder needs an explicit in-JVM bridge from outbound -> inbound.
            if ("local".equalsIgnoreCase(binder.name())) {
                LocalBusBinder.bridge(outbound, inbound, name);
            }

            log.info("Binding '{}' using binder '{}', destination='{}', group='{}'", name, binder.name(), binding.destination(), binding.group());

            binder.bindOutbound(binding, outbound);
            binder.bindInbound(binding, inbound);
        }
    }
}
