package com.ysmjjsy.goya.component.framework.bus.runtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.messaging.SubscribableChannel;

import java.util.Map;
import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/27 00:49
 */
@Slf4j
@RequiredArgsConstructor
public final class BusInboundSubscriber implements SmartInitializingSingleton {

    private final BindingResolver resolver;
    private final BusChannels channels;
    private final BusMessageDispatcher dispatcher;

    @Override
    public void afterSingletonsInstantiated() {
        for (Map.Entry<String, ?> e : resolver.all().entrySet()) {
            String bindingName = e.getKey();
            SubscribableChannel inbound = channels.inbound(bindingName);

            // 防止重复订阅（一般不会发生，但加一道保险）
            inbound.unsubscribe(dispatcher);
            inbound.subscribe(dispatcher);

            log.info("Bus inbound 已订阅 dispatcher: binding='{}'", bindingName);
        }
    }
}