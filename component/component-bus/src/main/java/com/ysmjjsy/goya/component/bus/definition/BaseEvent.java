package com.ysmjjsy.goya.component.bus.definition;

import lombok.Getter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>基础事件实现</p>
 * <p>提供事件的基础属性，包括事件名称、时间戳、事件ID、关联ID、版本等</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * public class OrderCreatedEvent extends BaseEvent {
 *     private final String orderId;
 *     private final BigDecimal amount;
 *
 *     public OrderCreatedEvent(String orderId, BigDecimal amount) {
 *         this.orderId = orderId;
 *         this.amount = amount;
 *     }
 *
 *     @Override
 *     public String eventName() {
 *         return "order.created";
 *     }
 *
 *     @Override
 *     public String eventVersion() {
 *         return "1.0";
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

    /**
     * 事件 ID
     * <p>用于事件追踪和回放，每个事件实例都有唯一的 ID</p>
     */
    private final String eventId = UUID.randomUUID().toString();

    /**
     * 关联 ID
     * <p>用于关联相关事件，可通过构造函数或 setter 设置</p>
     */
    private String correlationId;

    /**
     * 事件版本
     * <p>用于事件演进和版本管理，默认版本为 "1.0"</p>
     * <p>子类可以重写 eventVersion() 方法返回不同的版本</p>
     */
    private final String eventVersion = "1.0";

    /**
     * 事件命名空间
     * <p>用于区分不同团队或系统的事件，避免事件名称冲突</p>
     * <p>默认命名空间为 "default"</p>
     * <p>子类可以通过构造函数或 setter 设置命名空间</p>
     */
    private String namespace = "default";

    /**
     * 设置关联 ID
     * <p>用于关联相关事件</p>
     *
     * @param correlationId 关联 ID
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * 设置命名空间
     * <p>用于区分不同团队或系统的事件</p>
     *
     * @param namespace 命名空间
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public String correlationId() {
        return correlationId;
    }

    @Override
    public String eventVersion() {
        return eventVersion;
    }

    @Override
    public LocalDateTime timestamp() {
        return timestamp;
    }

    @Override
    public String eventNamespace() {
        return namespace;
    }
}

