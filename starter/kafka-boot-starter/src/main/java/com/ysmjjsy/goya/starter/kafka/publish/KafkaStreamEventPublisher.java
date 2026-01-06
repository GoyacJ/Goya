package com.ysmjjsy.goya.starter.kafka.publish;

import com.ysmjjsy.goya.component.bus.capabilities.Capabilities;
import com.ysmjjsy.goya.component.bus.constants.IBusConstants;
import com.ysmjjsy.goya.component.bus.definition.BusHeaders;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.publish.IRemoteEventPublisher;
import com.ysmjjsy.goya.component.bus.publish.MetadataAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>Kafka 流事件发布器</p>
 * <p>实现 IRemoteEventPublisher，提供 Kafka 特定的事件发布能力</p>
 * <p>Kafka 不支持原生延迟消息，通过 ScheduledExecutorService 实现延迟发送</p>
 * <p><strong>重要说明：</strong></p>
 * <ul>
 *   <li>延迟消息使用内存调度（ScheduledExecutorService），应用重启会丢失未发送的延迟消息</li>
 *   <li>这不是 Kafka 的原生能力，而是"模拟实现"，适用于对可靠性要求不高的场景</li>
 *   <li>对于生产环境，建议使用 Kafka 延迟插件（如 kafka-delayed-message-plugin）或业务层实现</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 通过 IBusService 使用
 * busService.publishDelayed(event, Duration.ofSeconds(10));
 * }</pre>
 *
 * @author goya
 * @see StreamBridge
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaStreamEventPublisher implements IRemoteEventPublisher {

    private final StreamBridge streamBridge;
    private final ScheduledExecutorService delayedMessageScheduler;

    @Override
    public String mark() {
        return IBusConstants.MARK_KAFKA;
    }

    @Override
    public void publish(String destination, Message<?> message) {
        if (StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Destination cannot be null or blank");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        Object payload = message.getPayload();
        if (!(payload instanceof IEvent event)) {
            throw new IllegalArgumentException("Payload must be an instance of IEvent");
        }

        MessageHeaders headers = message.getHeaders();

        // 确保 Header 中包含事件类型信息（如果还没有）
        Message<?> finalMessage = message;
        if (!headers.containsKey(BusHeaders.EVENT_TYPE)) {
            finalMessage = MessageBuilder.fromMessage(message)
                    .setHeader(BusHeaders.EVENT_TYPE, event.getClass().getName())
                    .build();
        }

        // 检查是否有延迟时间
        Long delayMillis = MetadataAccessor.getDelay(finalMessage.getHeaders());

        if (delayMillis != null && delayMillis > 0) {
            // 有延迟，使用 ScheduledExecutorService 延迟发送
            log.debug("[Goya] |- starter [kafka] KafkaStreamEventPublisher |- scheduling delayed message [{}] to destination [{}] with delay [{}ms]",
                    event.eventName(), destination, delayMillis);

            // 移除延迟 header，避免重复处理
            Message<?> messageWithoutDelay = MessageBuilder.fromMessage(finalMessage)
                    .removeHeader(BusHeaders.DELAY)
                    .build();

            // 延迟发送
            delayedMessageScheduler.schedule(() -> {
                boolean sent = streamBridge.send(destination, messageWithoutDelay);
                if (sent) {
                    log.debug("[Goya] |- starter [kafka] KafkaStreamEventPublisher |- delayed message [{}] sent to destination [{}]",
                            event.eventName(), destination);
                } else {
                    log.warn("[Goya] |- starter [kafka] KafkaStreamEventPublisher |- failed to send delayed message [{}] to destination [{}]",
                            event.eventName(), destination);
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        } else {
            // 无延迟，直接发送
            log.debug("[Goya] |- starter [kafka] KafkaStreamEventPublisher |- publish event [{}] (type: {}) to destination [{}]",
                    event.eventName(), event.getClass().getName(), destination);

            boolean sent = streamBridge.send(destination, finalMessage);
            if (!sent) {
                log.warn("[Goya] |- starter [kafka] KafkaStreamEventPublisher |- failed to send message to destination [{}]", destination);
            }
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return new Capabilities(
                // 不支持原生延迟消息（通过 ScheduledExecutorService 模拟）
                false,
                // 支持顺序消息（通过分区键）
                true,
                // 支持分区
                true,
                // 延迟消息无限制（因为是模拟实现）
                -1,
                "Kafka capabilities: ordered messages and partitioning supported, delayed messages via ScheduledExecutorService (not reliable across restarts)",
                // 允许降级（延迟消息可以降级为立即发送）
                true
        );
    }
}

