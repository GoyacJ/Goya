package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.SendOptions;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Objects;

/**
 * <p>BinderSelector</p>
 * Selects binder for a binding using deterministic precedence:
 * 1) per-send SendOptions.binder
 * 2) binding.binder
 * 3) global defaultBinder
 * 4) if only one non-local binder exists use it, else local
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

    public BusBinder select(BusBinding binding, SendOptions options) {
        String binderName = determineBinderName(binding, options);

        if (binderName != null) {
            return registry.get(binderName).orElseThrow(() ->
                    new IllegalStateException("No BusBinder named '" + binderName + "' found on classpath."));
        }

        // Auto: pick single non-local binder if exists; else local.
        return registry.all().stream()
                .filter(b -> !"local".equalsIgnoreCase(b.name()))
                // deterministic if multiple
                .min(Comparator.comparing(BusBinder::name))
                .orElseGet(() -> registry.get("local")
                        .orElseThrow(() -> new IllegalStateException("LocalBusBinder is missing.")));
    }

    private String determineBinderName(BusBinding binding, SendOptions options) {
        String override = options != null ? options.binder() : null;

        if (override != null && !override.isBlank()) {
            return override;
        }

        if (StringUtils.isNotBlank(binding.binder())) {
            return binding.binder();
        }

        if (StringUtils.isNotBlank(props.defaultBinder())) {
            return props.defaultBinder();
        }

        return null;
    }
}

