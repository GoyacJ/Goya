package com.ysmjjsy.goya.component.framework.bus.event;

/**
 * <p>In-process event bus abstraction</p>
 *
 * @author goya
 * @since 2026/1/26 21:56
 */
public interface BusEventPublisher {

    <T> void publish(T event);

    /**
     * Publish after current transaction commits (if a transaction is active),
     * otherwise publish immediately.
     */
    <T> void publishAfterCommit(T event);
}
