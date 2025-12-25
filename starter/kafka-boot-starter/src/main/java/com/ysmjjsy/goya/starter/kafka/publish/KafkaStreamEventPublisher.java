package com.ysmjjsy.goya.starter.kafka.publish;

import com.ysmjjsy.goya.component.bus.constants.IBusConstants;
import com.ysmjjsy.goya.component.bus.definition.BusHeaders;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.publish.IRemoteEventPublisher;
import com.ysmjjsy.goya.component.bus.publish.MetadataAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>Kafka 流事件发布器</p>
 * <p>扩展 StreamEventPublisher，支持延迟消息处理</p>
 * <p>Kafka 不支持原生延迟消息，通过 ScheduledExecutorService 实现延迟发送</p>
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
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be null or blank");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        Object payload = message.getPayload();
        if (!(payload instanceof IEvent)) {
            throw new IllegalArgumentException("Payload must be an instance of IEvent");
        }

        IEvent event = (IEvent) payload;
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
}

