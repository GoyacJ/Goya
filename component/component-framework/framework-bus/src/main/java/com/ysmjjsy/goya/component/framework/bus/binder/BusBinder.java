package com.ysmjjsy.goya.component.framework.bus.binder;

import org.springframework.messaging.SubscribableChannel;

/**
 * <p>Binder plugs transport implementations into the Integration skeleton</p>
 *
 * @author goya
 * @since 2026/1/26 23:43
 */
public interface BusBinder {

    /**
     * Binder name (e.g. local, kafka, rabbit, stream).
     */
    String name();

    /**
     * Bind outbound channel for a logical binding.
     */
    void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel);

    /**
     * Bind inbound channel for a logical binding.
     */
    void bindInbound(BusBinding binding, SubscribableChannel inboundChannel);
}