package com.ysmjjsy.goya.component.common.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.Clock;

/**
 * <p>应用初始化事件</p>
 *
 * @author goya
 * @since 2025/12/19 23:40
 */
@Slf4j
public class ApplicationInitializingEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 2012503459546299809L;

    public ApplicationInitializingEvent(Object source) {
        super(source);
    }

    public ApplicationInitializingEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
