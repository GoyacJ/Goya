package com.ysmjjsy.goya.component.bus.ack;

/**
 * <p>事件确认接口</p>
 * <p>统一抽象不同 MQ 的 ACK 机制，避免泄漏底层 MQ 语义</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @BusEventListener(scope = {EventScope.REMOTE}, ackMode = AckMode.MANUAL)
 * public void handleEvent(OrderCreatedEvent event, IEventAcknowledgment ack) {
 *     try {
 *         // 处理事件
 *         processEvent(event);
 *         // 确认消息
 *         ack.acknowledge();
 *     } catch (Exception e) {
 *         // 不确认，触发重试
 *         log.error("Failed to process event", e);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
public interface IEventAcknowledgment {

    /**
     * 确认消息
     * <p>表示消息已成功处理，MQ 可以删除该消息</p>
     * <p>如果消息处理失败，不应调用此方法，让 MQ 重试或进入 DLQ</p>
     *
     * @throws Exception 如果确认失败
     */
    void acknowledge() throws Exception;

    /**
     * 拒绝消息（不重新入队）
     * <p>表示消息处理失败，且不应重试</p>
     * <p>消息会被丢弃或进入 DLQ（取决于 MQ 配置）</p>
     *
     * @throws Exception 如果拒绝失败
     */
    default void reject() throws Exception {
        // 默认实现：调用 acknowledge（兼容不支持 reject 的 MQ）
        acknowledge();
    }

    /**
     * 拒绝消息（重新入队）
     * <p>表示消息处理失败，但应重试</p>
     * <p>消息会被重新投递（取决于 MQ 配置）</p>
     * <p>注意：当 requeue=true 时，不调用任何确认方法，让 MQ 自动重试</p>
     * <p>如果 MQ 不支持 requeue 操作，会抛出 UnsupportedOperationException</p>
     *
     * @param requeue 是否重新入队
     * @throws Exception 如果拒绝失败
     */
    default void reject(boolean requeue) throws Exception {
        if (requeue) {
            // 重新入队：不调用任何方法，让 MQ 重试
            // 注意：某些 MQ 可能不支持此操作，需要特殊处理
            throw new UnsupportedOperationException(
                    "Requeue is not supported. The MQ will handle retry automatically if configured.");
        } else {
            reject();  // 不重新入队，拒绝消息
        }
    }
}

