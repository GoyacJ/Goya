package com.ysmjjsy.goya.component.framework.security.spi;

import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeEvent;

/**
 * <p>权限变更发布器</p>
 *
 * @author goya
 * @since 2026/1/31
 */
public interface PermissionChangePublisher {

    /**
     * 发布权限变更事件。
     *
     * @param event 权限变更事件
     */
    void publish(PermissionChangeEvent event);
}
