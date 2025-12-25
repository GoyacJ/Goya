package com.ysmjjsy.goya.component.bus.definition;

import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>基础事件实现</p>
 * <p>提供事件的基础属性，包括事件名称、时间戳等</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * public class OrderCreatedEvent extends BaseEvent {
 *     private final String orderId;
 *     private final BigDecimal amount;
 *
 *     public OrderCreatedEvent(String orderId, BigDecimal amount) {
 *         super("order.created");
 *         this.orderId = orderId;
 *         this.amount = amount;
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Getter
public abstract class BaseEvent implements IEvent {

    @Serial
    private static final long serialVersionUID = 293682199896164000L;

    /**
     * 事件时间戳
     */
    private final LocalDateTime timestamp = LocalDateTime.now();

}

