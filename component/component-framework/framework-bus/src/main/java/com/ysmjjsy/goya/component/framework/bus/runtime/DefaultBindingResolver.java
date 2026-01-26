package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;

import java.util.Map;
import java.util.Objects;

/**
 * <p>默认解析器：直接读取 framework.bus.bindings.*</p>
 *
 * @author goya
 * @since 2026/1/27 00:55
 */
public final class DefaultBindingResolver implements BindingResolver {

    private final BusProperties props;

    public DefaultBindingResolver(BusProperties props) {
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public Map<String, BusProperties.BindingProperties> all() {
        return props.bindings();
    }

    @Override
    public BusBinding resolve(String bindingName) {
        BusProperties.BindingProperties bp = props.bindings().get(bindingName);
        if (bp == null) {
            throw new IllegalArgumentException("未找到 binding 配置: " + bindingName);
        }
        return new BusBinding(bindingName, bp.destination(), bp.group(), bp.binder(), bp.binderBindingNames());
    }
}