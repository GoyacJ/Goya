package com.ysmjjsy.goya.component.bus.stream.publish;

import com.ysmjjsy.goya.component.bus.stream.capabilities.Capabilities;
import com.ysmjjsy.goya.component.bus.stream.definition.BusHeaders;
import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * <p>流事件发布器</p>
 * <p>基于 Spring Cloud Stream 的 StreamBridge 实现远程事件发布</p>
 * <p>在 Header 中传递事件类型信息，payload 直接是事件的 JSON</p>
 * <p>使用条件：当存在 StreamBridge Bean 时自动注册（需要引入具体的 starter，如 kafka-boot-starter）</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 通过 IBusService 使用
 * busService.publishRemote(event);
 *
 * // 或直接使用
 * streamEventPublisher.publish("bus.events", message);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see StreamBridge
 * @see <a href="https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream.html#_sending_arbitrary_data_to_an_output_e_g_foreign_event_driven">Spring Cloud Stream StreamBridge</a>
 */
@Slf4j
@RequiredArgsConstructor
public class StreamBusEventPublisher implements IRemoteEventPublisher {

    private final StreamBridge streamBridge;

    @Override
    public void publish(String destination, Message<?> message) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be null or blank");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        Object payload = message.getPayload();
        if (!(payload instanceof IBusEvent event)) {
            throw new IllegalArgumentException("Payload must be an instance of IEvent");
        }

        // 确保 Header 中包含事件类型信息（如果还没有）
        Message<?> finalMessage = message;
        if (!message.getHeaders().containsKey(BusHeaders.EVENT_TYPE)) {
            // 添加事件类型到 Header
            finalMessage = MessageBuilder.fromMessage(message)
                    .setHeader(BusHeaders.EVENT_TYPE, event.getClass().getName())
                    .build();
        }

        log.debug("[Goya] |- component [bus] StreamEventPublisher |- publish event [{}] (type: {}) to destination [{}]",
                event.eventName(), event.getClass().getName(), destination);

        // Spring Cloud Stream 会自动将 IEvent 序列化为 JSON
        boolean sent = streamBridge.send(destination, finalMessage);

        if (!sent) {
            log.warn("[Goya] |- component [bus] StreamEventPublisher |- failed to send message to destination [{}]", destination);
        }
    }

    @Override
    public Capabilities getCapabilities() {
        // StreamEventPublisher 是基础实现，不声明具体能力
        // 具体能力由实现类（如 KafkaStreamEventPublisher）声明
        // 默认不允许降级
        return Capabilities.NONE;
    }
}

