package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;

import java.util.Map;

/**
 * <p>Resolve configured bindings into runtime {@link BusBinding}.</p>
 *
 * @author goya
 * @since 2026/1/26 23:48
 */
public interface BindingResolver {

    Map<String, BusProperties.BindingProperties> all();

    BusBinding resolve(String bindingName);
}
