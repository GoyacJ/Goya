package com.ysmjjsy.goya.component.framework.log.event;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.util.Objects;

/**
 * <p>@Loggable 方法调用事件</p>
 *
 * <p>监听该事件即可实现“调用记录落库/审计/分析”等功能，切面只负责发布。</p>
 *
 * @author goya
 * @since 2026/1/24 23:30
 */
public class LoggableMethodInvokedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private final MethodInvokeEventPayload payload;

    /**
     * 构造事件。
     *
     * @param source 事件源（通常为切面）
     * @param payload 载荷
     */
    public LoggableMethodInvokedEvent(Object source, MethodInvokeEventPayload payload) {
        super(source);
        this.payload = Objects.requireNonNull(payload, "payload 不能为空");
    }

    /**
     * 获取事件载荷。
     *
     * @return payload
     */
    public MethodInvokeEventPayload getPayload() {
        return payload;
    }
}