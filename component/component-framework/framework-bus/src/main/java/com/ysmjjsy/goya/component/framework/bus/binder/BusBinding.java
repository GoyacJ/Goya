package com.ysmjjsy.goya.component.framework.bus.binder;

import java.util.Objects;

/**
 * <p>Resolved binding configuration</p>
 *
 * @param binder optional preferred binder
 * @author goya
 * @since 2026/1/26 23:43
 */
public record BusBinding(String name, String destination, String group, String binder) {

    public BusBinding(String name, String destination, String group, String binder) {
        this.name = Objects.requireNonNull(name);
        this.destination = destination;
        this.group = group;
        this.binder = binder;
    }
}