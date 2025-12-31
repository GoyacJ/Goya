package com.ysmjjsy.goya.component.common.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serial;
import java.time.Clock;

/**
 * <p>应用初始化事件</p>
 *
 * @author goya
 * @since 2025/12/19 23:40
 */
@Slf4j
@Getter
public class ApplicationInitializingEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 2012503459546299809L;

    private final ConfigurableApplicationContext context;

    public ApplicationInitializingEvent(Object source, ConfigurableApplicationContext context) {
        super(source);
        this.context = context;
    }

    public ApplicationInitializingEvent(Object source, Clock clock, ConfigurableApplicationContext context) {
        super(source, clock);
        this.context = context;
    }
}
