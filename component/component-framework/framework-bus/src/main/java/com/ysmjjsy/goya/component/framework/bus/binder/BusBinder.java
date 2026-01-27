package com.ysmjjsy.goya.component.framework.bus.binder;

import org.springframework.messaging.SubscribableChannel;

/**
 * <p>Binder SPI：不同实现（local/kafka/rabbit/stream）通过它接入 Bus</p>
 * <p>
 * 约定：
 * - bindOutbound：把 outboundChannel 的消息发送到外部系统（或本地）
 * - bindInbound：把外部系统的消息投递到 inboundChannel
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