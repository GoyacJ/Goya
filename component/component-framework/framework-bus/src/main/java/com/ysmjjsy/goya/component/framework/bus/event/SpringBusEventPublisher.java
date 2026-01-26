package com.ysmjjsy.goya.component.framework.bus.event;

import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>Default event implementation using Spring's {@link ApplicationEventPublisher}.</p>
 *
 * @author goya
 * @since 2026/1/26 23:45
 */
public class SpringBusEventPublisher implements BusEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringBusEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public <T> void publish(@NonNull T event) {
        publisher.publishEvent(event);
    }

    @Override
    public <T> void publishAfterCommit(@NonNull T event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }
}