package com.ysmjjsy.goya.component.bus.stream.publish;

import com.ysmjjsy.goya.component.bus.stream.capabilities.Capabilities;
import com.ysmjjsy.goya.component.bus.stream.constants.BusStreamConst;
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
 *
 *     @Override
 *     public Capabilities getCapabilities() {
 *         return Capabilities.BASIC; // Kafka 支持顺序消息，但不支持原生延迟消息
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see org.springframework.cloud.stream.function.StreamBridge
 */
public interface IRemoteEventPublisher {

    /**
     * 获取发布器标记
     * <p>用于标识不同的发布器实现，如 "REMOTE"、"DISTRIBUTED"、"KAFKA" 等</p>
     * <p>在 DefaultBusService 中用于选择对应的发布器</p>
     *
     * @return 发布器标记
     */
    default String mark() {
        return BusStreamConst.MARK_REMOTE;
    }

    /**
     * 发布远程事件
     *
     * @param destination 目标 destination（对应 Spring Cloud Stream 的 destination）
     * @param message     消息对象，包含 payload 和 headers
     */
    void publish(String destination, Message<?> message);

    /**
     * 获取 MQ 能力声明
     * <p>用于声明该发布器支持的能力，避免"能力错觉"</p>
     * <p>不同 MQ 的能力差异：</p>
     * <ul>
     *   <li>Kafka: 支持分区、顺序消息，不支持原生延迟消息</li>
     *   <li>RabbitMQ: 支持延迟消息、顺序消息，不支持分区</li>
     *   <li>RocketMQ: 支持延迟消息、顺序消息、分区</li>
     * </ul>
     * <p>默认返回 NONE（不支持任何高级特性）</p>
     *
     * @return 能力声明
     */
    default Capabilities getCapabilities() {
        return Capabilities.NONE;
    }
}

