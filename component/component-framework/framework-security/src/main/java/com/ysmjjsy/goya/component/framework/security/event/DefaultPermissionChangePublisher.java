package com.ysmjjsy.goya.component.framework.security.event;

import com.ysmjjsy.goya.component.framework.security.spi.PermissionChangePublisher;
import com.ysmjjsy.goya.component.framework.bus.event.BusEventPublisher;

import java.time.LocalDateTime;

/**
 * <p>默认权限变更发布器。</p>
 *
 * <p>基于 framework-bus 事件发布。</p>
 *
 * @author goya
 * @since 2026/1/31
 */
public class DefaultPermissionChangePublisher implements PermissionChangePublisher {

    private final BusEventPublisher busEventPublisher;

    public DefaultPermissionChangePublisher(BusEventPublisher busEventPublisher) {
        this.busEventPublisher = busEventPublisher;
    }

    @Override
    public void publish(PermissionChangeEvent event) {
        if (event == null) {
            return;
        }
        if (event.getChangedAt() == null) {
            event.setChangedAt(LocalDateTime.now());
        }
        busEventPublisher.publishAfterCommit(event);
    }
}
