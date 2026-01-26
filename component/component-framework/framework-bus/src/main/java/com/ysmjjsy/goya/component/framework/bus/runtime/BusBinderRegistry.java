package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Registry of available binders.</p>
 *
 * @author goya
 * @since 2026/1/26 23:48
 */
public final class BusBinderRegistry {

    private final Map<String, BusBinder> binders = new ConcurrentHashMap<>();

    public BusBinderRegistry(List<BusBinder> allBinders) {
        for (BusBinder b : allBinders) {
            if (b == null || b.name() == null) {
                continue;
            }
            binders.put(b.name().toLowerCase(), b);
        }
    }

    public Optional<BusBinder> get(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(binders.get(name.toLowerCase()));
    }

    public Map<String, BusBinder> all() {
        return Map.copyOf(binders);
    }
}

