package com.ysmjjsy.goya.component.bus.core.configuration;

import com.ysmjjsy.goya.component.bus.core.event.LocalEventPublisher;
import com.ysmjjsy.goya.component.bus.core.event.EventPublisher;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 16:15
 */
@Slf4j
@AutoConfiguration
public class BusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [bus-core] BusAutoConfiguration auto configure.");
    }

    @Bean
    @Lazy(false)
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher localEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        LocalEventPublisher localEventPublisher = new LocalEventPublisher(applicationEventPublisher);
        log.trace("[Goya] |- component [bus-core] FrameWorkAutoConfiguration |- bean [localEventPublisher] register.");
        return localEventPublisher;
    }
}
