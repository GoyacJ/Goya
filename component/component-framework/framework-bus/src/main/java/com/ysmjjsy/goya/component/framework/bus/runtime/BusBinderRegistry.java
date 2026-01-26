package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;

import java.util.*;

/**
 * <p>Registry of available binders.</p>
 *
 * @author goya
 * @since 2026/1/26 23:48
 */
public final class BusBinderRegistry {

    private final Map<String, BusBinder> byName;

    public BusBinderRegistry(List<BusBinder> binders) {
        Map<String, BusBinder> m = new LinkedHashMap<>();
        for (BusBinder b : binders) {
            m.put(b.name(), b);
        }
        this.byName = Collections.unmodifiableMap(m);
    }

    public Optional<BusBinder> get(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public Collection<BusBinder> all() {
        return byName.values();
    }
}

