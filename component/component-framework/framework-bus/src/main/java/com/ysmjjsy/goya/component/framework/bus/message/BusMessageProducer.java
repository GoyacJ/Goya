package com.ysmjjsy.goya.component.framework.bus.message;

/**
 * <p>Message producer abstraction.</p>
 *
 * @author goya
 * @since 2026/1/26 23:46
 */
public interface BusMessageProducer {

    <T> void send(String bindingName, MessageEnvelope<T> envelope);

    <T> void send(String bindingName, MessageEnvelope<T> envelope, SendOptions options);
}
