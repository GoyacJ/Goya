package com.ysmjjsy.goya.component.bus.definition;

import java.io.Serializable;

/**
 * <p>事件接口</p>
 * <p>所有事件必须实现此接口</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * public record OrderCreatedEvent(String orderId, BigDecimal amount) implements IEvent {
 *     @Override
 *     public String eventName() {
 *         return "order.created";
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
public interface IEvent extends Serializable {

    /**
     * 获取事件名称
     * <p>事件名称用于路由和订阅，建议使用点分隔的命名方式，如：order.created、user.updated</p>
     *
     * @return 事件名称
     */
    default String eventName() {
        return getClass().getSimpleName();
    }
}

