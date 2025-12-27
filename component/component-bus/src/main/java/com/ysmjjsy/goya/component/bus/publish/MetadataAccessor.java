package com.ysmjjsy.goya.component.bus.publish;

import com.ysmjjsy.goya.component.bus.definition.BusHeaders;
import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>元数据访问器</p>
 * <p>将 BusHeaders 的通用 Header（x-goya-*）映射到 Spring Cloud Stream Message Headers</p>
 * <p>供实现层（kafka-boot-starter 等）使用</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 构建 Message Headers
 * Map<String, Object> headers = MetadataAccessor.buildHeaders(event, EventScope.REMOTE)
 *     .withDelay(Duration.ofSeconds(10))
 *     .withPartitionKey("user123")
 *     .withTraceId("trace-123")
 *     .withIdempotencyKey("idempotency-123")
 *     .toMap();
 *
 * // 创建 Message
 * Message<IEvent> message = MessageBuilder.withPayload(event)
 *     .copyHeaders(headers)
 *     .build();
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see BusHeaders
 * @see org.springframework.messaging.MessageHeaders
 */
@Slf4j
public final class MetadataAccessor {

    private MetadataAccessor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 构建 Message Headers 的 Builder
     *
     * @param event 事件对象
     * @param scope 事件作用域
     * @return HeaderBuilder
     */
    public static HeaderBuilder buildHeaders(IEvent event, EventScope scope) {
        return new HeaderBuilder(event, scope);
    }

    /**
     * Header Builder
     */
    public static class HeaderBuilder {
        private final IEvent event;
        private final EventScope scope;
        private final Map<String, Object> headers = new HashMap<>();

        private HeaderBuilder(IEvent event, EventScope scope) {
            this.event = event;
            this.scope = scope;
            // 设置基础 Header
            // eventName 使用 event.eventName()，支持事件重写
            headers.put(BusHeaders.EVENT_NAME, event.eventName());
            headers.put(BusHeaders.EVENT_SCOPE, scope.name());
            // 设置事件类型（用于跨服务反序列化）
            headers.put(BusHeaders.EVENT_TYPE, event.getClass().getName());
            // 自动设置事件元数据
            headers.put(BusHeaders.EVENT_ID, event.eventId());
            headers.put(BusHeaders.EVENT_VERSION, event.eventVersion());
            if (event.correlationId() != null) {
                headers.put(BusHeaders.CORRELATION_ID, event.correlationId());
            }
        }

        /**
         * 设置延迟时间
         *
         * @param delay 延迟时间
         * @return Builder
         */
        public HeaderBuilder withDelay(Duration delay) {
            if (delay != null && !delay.isNegative()) {
                headers.put(BusHeaders.DELAY, delay.toMillis());
            }
            return this;
        }

        /**
         * 设置分区键
         *
         * @param partitionKey 分区键
         * @return Builder
         */
        public HeaderBuilder withPartitionKey(String partitionKey) {
            if (partitionKey != null && !partitionKey.isBlank()) {
                headers.put(BusHeaders.PARTITION_KEY, partitionKey);
            }
            return this;
        }

        /**
         * 设置追踪 ID
         *
         * @param traceId 追踪 ID
         * @return Builder
         */
        public HeaderBuilder withTraceId(String traceId) {
            if (traceId != null && !traceId.isBlank()) {
                headers.put(BusHeaders.TRACE_ID, traceId);
            }
            return this;
        }

        /**
         * 设置幂等键
         *
         * @param idempotencyKey 幂等键
         * @return Builder
         */
        public HeaderBuilder withIdempotencyKey(String idempotencyKey) {
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                headers.put(BusHeaders.IDEMPOTENCY_KEY, idempotencyKey);
            }
            return this;
        }

        /**
         * 设置事件 ID
         *
         * @param eventId 事件 ID
         * @return Builder
         */
        public HeaderBuilder withEventId(String eventId) {
            if (eventId != null && !eventId.isBlank()) {
                headers.put(BusHeaders.EVENT_ID, eventId);
            }
            return this;
        }

        /**
         * 设置关联 ID
         *
         * @param correlationId 关联 ID
         * @return Builder
         */
        public HeaderBuilder withCorrelationId(String correlationId) {
            if (correlationId != null && !correlationId.isBlank()) {
                headers.put(BusHeaders.CORRELATION_ID, correlationId);
            }
            return this;
        }

        /**
         * 设置事件版本
         *
         * @param eventVersion 事件版本
         * @return Builder
         */
        public HeaderBuilder withEventVersion(String eventVersion) {
            if (eventVersion != null && !eventVersion.isBlank()) {
                headers.put(BusHeaders.EVENT_VERSION, eventVersion);
            }
            return this;
        }

        /**
         * 添加自定义 Header
         *
         * @param key   Header 键
         * @param value Header 值
         * @return Builder
         */
        public HeaderBuilder withHeader(String key, Object value) {
            if (key != null && value != null) {
                headers.put(key, value);
            }
            return this;
        }

        /**
         * 转换为 Map
         *
         * @return Header Map
         */
        public Map<String, Object> toMap() {
            return new HashMap<>(headers);
        }

        /**
         * 转换为 MessageHeaders
         *
         * @return MessageHeaders
         */
        public MessageHeaders toMessageHeaders() {
            return new MessageHeaders(headers);
        }
    }

    /**
     * 从 Message Headers 中提取延迟时间
     *
     * @param headers Message Headers
     * @return 延迟时间（毫秒），如果不存在则返回 null
     */
    public static Long getDelay(MessageHeaders headers) {
        Object delay = headers.get(BusHeaders.DELAY);
        if (delay instanceof Number de) {
            return de.longValue();
        }
        return null;
    }

    /**
     * 从 Message Headers 中提取分区键
     *
     * @param headers Message Headers
     * @return 分区键，如果不存在则返回 null
     */
    public static String getPartitionKey(MessageHeaders headers) {
        Object key = headers.get(BusHeaders.PARTITION_KEY);
        return key != null ? key.toString() : null;
    }

    /**
     * 从 Message Headers 中提取追踪 ID
     *
     * @param headers Message Headers
     * @return 追踪 ID，如果不存在则返回 null
     */
    public static String getTraceId(MessageHeaders headers) {
        Object traceId = headers.get(BusHeaders.TRACE_ID);
        return traceId != null ? traceId.toString() : null;
    }

    /**
     * 从 Message Headers 中提取幂等键
     *
     * @param headers Message Headers
     * @return 幂等键，如果不存在则返回 null
     */
    public static String getIdempotencyKey(MessageHeaders headers) {
        Object key = headers.get(BusHeaders.IDEMPOTENCY_KEY);
        return key != null ? key.toString() : null;
    }

    /**
     * 从 Message Headers 中提取事件名称
     *
     * @param headers Message Headers
     * @return 事件名称，如果不存在则返回 null
     */
    public static String getEventName(MessageHeaders headers) {
        Object eventName = headers.get(BusHeaders.EVENT_NAME);
        return eventName != null ? eventName.toString() : null;
    }

    /**
     * 从 Message Headers 中提取事件作用域
     *
     * @param headers Message Headers
     * @return 事件作用域，如果不存在则返回 null
     */
    public static EventScope getEventScope(MessageHeaders headers) {
        Object scope = headers.get(BusHeaders.EVENT_SCOPE);
        if (scope != null) {
            try {
                return EventScope.valueOf(scope.toString());
            } catch (IllegalArgumentException _) {
                log.warn("[Goya] |- component [bus] MetadataAccessor |- invalid event scope: {}", scope);
            }
        }
        return null;
    }

    /**
     * 从 Message Headers 中提取事件 ID
     *
     * @param headers Message Headers
     * @return 事件 ID，如果不存在则返回 null
     */
    public static String getEventId(MessageHeaders headers) {
        Object eventId = headers.get(BusHeaders.EVENT_ID);
        return eventId != null ? eventId.toString() : null;
    }

    /**
     * 从 Message Headers 中提取关联 ID
     *
     * @param headers Message Headers
     * @return 关联 ID，如果不存在则返回 null
     */
    public static String getCorrelationId(MessageHeaders headers) {
        Object correlationId = headers.get(BusHeaders.CORRELATION_ID);
        return correlationId != null ? correlationId.toString() : null;
    }

    /**
     * 从 Message Headers 中提取事件版本
     *
     * @param headers Message Headers
     * @return 事件版本，如果不存在则返回 null
     */
    public static String getEventVersion(MessageHeaders headers) {
        Object eventVersion = headers.get(BusHeaders.EVENT_VERSION);
        return eventVersion != null ? eventVersion.toString() : null;
    }
}

