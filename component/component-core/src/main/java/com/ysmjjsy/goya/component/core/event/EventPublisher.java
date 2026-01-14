package com.ysmjjsy.goya.component.core.event;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/7 23:15
 */
public interface EventPublisher {

    /**
     * publish event
     * @param event  event
     * @param <T> event type
     */
    <T extends IEvent> void publish(T event);
}
