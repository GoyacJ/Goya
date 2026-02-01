package com.ysmjjsy.goya.component.framework.security.event;

import com.ysmjjsy.goya.component.framework.security.spi.PermissionChangeSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * <p>权限变更事件分发器。</p>
 *
 * <p>接收 PermissionChangeEvent，并分发给 PermissionChangeSubscriber。</p>
 *
 * @author goya
 * @since 2026/2/01
 */
@Slf4j
public class PermissionChangeSubscriberDispatcher {

    private final ObjectProvider<PermissionChangeSubscriber> subscribersProvider;

    public PermissionChangeSubscriberDispatcher(ObjectProvider<PermissionChangeSubscriber> subscribersProvider) {
        this.subscribersProvider = subscribersProvider;
    }

    @TransactionalEventListener
    public void onApplicationEvent(PermissionChangeEvent event) {
        if (event == null) {
            return;
        }
        for (PermissionChangeSubscriber subscriber : subscribersProvider) {
            if (subscriber == null) {
                continue;
            }
            try {
                subscriber.onChange(event);
            } catch (Exception ex) {
                log.warn("[Goya] |- component [framework] |- permission change subscriber error: {}", ex.getMessage(), ex);
            }
        }
    }
}
