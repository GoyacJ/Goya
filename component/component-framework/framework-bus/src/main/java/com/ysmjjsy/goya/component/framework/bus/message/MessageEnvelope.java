package com.ysmjjsy.goya.component.framework.bus.message;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>统一消息封装（元数据 + payload）</p>
 * 说明：bus 不强制序列化策略，序列化交给官方组件或上层约定
 *
 * @author goya
 * @since 2026/1/26 23:47
 */
public record MessageEnvelope<T>(
        String id,
        String type,
        Instant timestamp,
        String key,
        Map<String, Object> headers,
        T payload
) {
    public MessageEnvelope {
        if (headers == null) headers = new LinkedHashMap<>();
        if (timestamp == null) timestamp = Instant.now();
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
    }

    public static <T> MessageEnvelope<T> of(T payload) {
        String type = (payload == null) ? "null" : payload.getClass().getName();
        return new MessageEnvelope<>(null, type, Instant.now(), null, new LinkedHashMap<>(), payload);
    }
}