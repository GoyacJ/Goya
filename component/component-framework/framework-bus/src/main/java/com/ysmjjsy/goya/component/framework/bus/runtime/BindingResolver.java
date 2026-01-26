package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;

import java.util.Map;
import java.util.Objects;

/**
 * <p>Resolve configured bindings into runtime {@link BusBinding}.</p>
 *
 * @author goya
 * @since 2026/1/26 23:48
 */
public final class BindingResolver {

    private final BusProperties props;

    public BindingResolver(BusProperties props) {
        this.props = Objects.requireNonNull(props);
    }

    public BusBinding resolve(String bindingName) {
        BusProperties.BindingProperties bp = props.bindings().get(bindingName);
        if (bp == null) {
            throw new IllegalArgumentException("No framework.bus.bindings." + bindingName + " configured.");
        }
        return new BusBinding(bindingName, bp.destination(), bp.group(), bp.binder());
    }

    public Map<String, BusProperties.BindingProperties> all() {
        return props.bindings();
    }
}
