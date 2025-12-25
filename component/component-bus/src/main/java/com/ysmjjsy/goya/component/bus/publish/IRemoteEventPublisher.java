package com.ysmjjsy.goya.component.bus.publish;

import com.ysmjjsy.goya.component.common.strategy.IStrategyExecute;
import org.springframework.messaging.Message;

/**
 * <p>远程事件发布器接口</p>
 * <p>用于发布远程事件，基于 Spring Cloud Stream 的 StreamBridge 实现</p>
 * <p>实现类应该直接使用 StreamBridge，不重复封装</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @RequiredArgsConstructor
 * public class KafkaStreamEventPublisher implements IRemoteEventPublisher {
 *     private final StreamBridge streamBridge;
 *
 *     @Override
 *     public String mark() {
 *         return "DISTRIBUTED";
 *     }
 *
 *     @Override
 *     public void publish(String destination, Message<?> message) {
 *         streamBridge.send(destination, message);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see org.springframework.cloud.stream.function.StreamBridge
 */
public interface IRemoteEventPublisher extends IStrategyExecute<Message<?>, Void> {

    /**
     * 策略标记
     *
     * @return "DISTRIBUTED"
     */
    @Override
    default String mark() {
        return "DISTRIBUTED";
    }

    /**
     * 发布远程事件
     *
     * @param destination 目标 destination（对应 Spring Cloud Stream 的 destination）
     * @param message     消息对象，包含 payload 和 headers
     */
    void publish(String destination, Message<?> message);

    /**
     * 执行策略（实现 IStrategyExecute）
     *
     * @param request 消息对象
     */
    @Override
    default void execute(Message<?> request) {
        // 默认实现，子类可以覆盖
        // 注意：这里需要 destination，但 IStrategyExecute 接口不提供
        // 实际使用时应该通过 BusServiceImpl 调用 publish 方法
    }

    /**
     * 执行策略并返回结果（实现 IStrategyExecute）
     *
     * @param request 消息对象
     * @return null
     */
    @Override
    default Void executeResp(Message<?> request) {
        execute(request);
        return null;
    }
}

