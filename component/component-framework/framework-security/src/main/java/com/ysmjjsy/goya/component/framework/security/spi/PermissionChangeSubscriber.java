package com.ysmjjsy.goya.component.framework.security.spi;

import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeEvent;

/**
 * <p>权限变更订阅器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface PermissionChangeSubscriber {

    /**
     * 处理权限变更事件。
     *
     * @param event 权限变更事件
     */
    void onChange(PermissionChangeEvent event);
}
