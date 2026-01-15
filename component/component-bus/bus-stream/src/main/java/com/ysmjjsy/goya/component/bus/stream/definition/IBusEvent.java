package com.ysmjjsy.goya.component.bus.stream.definition;

import com.ysmjjsy.goya.component.bus.core.event.IEvent;

import java.time.LocalDateTime;
import java.util.UUID;

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
public interface IBusEvent extends IEvent {

    /**
     * 获取事件名称
     * <p>事件名称用于路由和订阅，建议使用点分隔的命名方式，格式：{namespace}.{domain}.{action}</p>
     * <p>示例：team-a.order.created、team-b.user.updated</p>
     * <p>如果未指定命名空间，则使用默认命名空间 "default"</p>
     *
     * @return 事件名称
     */
    default String eventName() {
        return getClass().getSimpleName();
    }

    /**
     * 获取事件命名空间
     * <p>命名空间用于区分不同团队或系统的事件，避免事件名称冲突</p>
     * <p>默认命名空间为 "default"</p>
     * <p>建议使用团队标识或系统标识作为命名空间，如 "team-a"、"team-b"、"system-order"</p>
     *
     * @return 事件命名空间
     */
    default String eventNamespace() {
        return "default";
    }

    /**
     * 获取事件版本
     * <p>用于事件演进和版本管理，默认版本为 "1.0"</p>
     * <p>当事件结构发生变更时，应更新版本号，如 "1.1"、"2.0" 等</p>
     *
     * @return 事件版本
     */
    default String eventVersion() {
        return "1.0";
    }

    /**
     * 获取事件 ID
     * <p>用于事件追踪和回放，每个事件实例都有唯一的 ID</p>
     * <p>默认自动生成 UUID，可在 BaseEvent 中重写</p>
     *
     * @return 事件 ID
     */
    default String eventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取关联 ID
     * <p>用于关联相关事件，如订单创建事件和订单支付事件可以使用相同的 correlationId</p>
     * <p>默认返回 null，可在 BaseEvent 中设置</p>
     *
     * @return 关联 ID，如果不存在则返回 null
     */
    default String correlationId() {
        return null;
    }

    /**
     * 获取事件时间戳
     * <p>事件发生的时间，用于审计和排序</p>
     * <p>默认返回当前时间，可在 BaseEvent 中重写</p>
     *
     * @return 事件时间戳
     */
    default LocalDateTime timestamp() {
        return LocalDateTime.now();
    }
}

