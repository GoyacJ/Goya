package com.ysmjjsy.goya.component.bus.core.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:59
 */
@Slf4j
@RequiredArgsConstructor
public class LocalEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public <T extends IEvent> void publish(T event) {
        applicationEventPublisher.publishEvent(event);
    }
}
