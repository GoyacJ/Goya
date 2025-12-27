package com.ysmjjsy.goya.component.bus.deserializer;

import com.ysmjjsy.goya.component.bus.definition.BusHeaders;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * <p>事件反序列化器</p>
 * <p>从 Message<?> 中提取事件信息，尝试反序列化为 IEvent 对象</p>
 * <p>供所有 starter（kafka、rabbitmq 等）复用</p>
 * <p>支持类加载白名单，防止恶意类加载攻击</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * DeserializationResult result = eventDeserializer.deserialize(message);
 * if (result.isDeserialized()) {
 *     // 使用 result.getEvent()
 * } else {
 *     // 使用 result.getJsonString() 和 result.getEventName()
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class EventDeserializer {

    private final ObjectProvider<EventClassWhitelist> whitelistProvider;

    /**
     * 反序列化消息
     * <p>从 Message<?> 中提取事件信息，尝试反序列化为 IEvent 对象</p>
     * <p>如果失败，返回 JSON 字符串和 eventName</p>
     *
     * @param message 消息
     * @return 反序列化结果
     */
    public DeserializationResult deserialize(Message<?> message) {
        if (message == null) {
            log.warn("[Goya] |- component [bus] EventDeserializer |- received null message");
            return DeserializationResult.builder()
                    .isDeserialized(false)
                    .build();
        }

        Object payload = message.getPayload();
        Map<String, Object> headers = message.getHeaders();

        // 1. 从 Header 读取事件类型和事件名称
        String eventTypeName = (String) headers.get(BusHeaders.EVENT_TYPE);
        String eventName = (String) headers.get(BusHeaders.EVENT_NAME);

        // 2. 如果 payload 已经是 IEvent，直接使用
        if (payload instanceof IEvent event) {
            String actualEventName = event.eventName();
            log.debug("[Goya] |- component [bus] EventDeserializer |- payload is already IEvent: {}", actualEventName);
            return DeserializationResult.builder()
                    .event(event)
                    .eventName(actualEventName)
                    .jsonString(JsonUtils.toJson(event))
                    .isDeserialized(true)
                    .build();
        }

        // 3. 尝试反序列化为 IEvent
        IEvent event;
        if (eventTypeName != null && !eventTypeName.isBlank()) {
            // 检查类加载白名单
            EventClassWhitelist whitelist = whitelistProvider.getIfAvailable();
            if (whitelist != null && !whitelist.isAllowed(eventTypeName)) {
                log.warn("[Goya] |- component [bus] EventDeserializer |- event type [{}] is not in whitelist, denied",
                        eventTypeName);
                // 不在白名单中，返回反序列化失败
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends IEvent> eventType = (Class<? extends IEvent>) Class.forName(eventTypeName);
                    
                    // 验证加载的类是否实现 IEvent 接口
                    if (!IEvent.class.isAssignableFrom(eventType)) {
                        log.warn("[Goya] |- component [bus] EventDeserializer |- class [{}] does not implement IEvent interface, denied",
                                eventTypeName);
                    } else {
                        event = deserializeToEventType(payload, eventType);
                        if (event != null) {
                            String actualEventName = event.eventName();
                            log.debug("[Goya] |- component [bus] EventDeserializer |- deserialized event [{}] from payload",
                                    actualEventName);
                            return DeserializationResult.builder()
                                    .event(event)
                                    .eventName(actualEventName)
                                    .jsonString(JsonUtils.toJson(event))
                                    .isDeserialized(true)
                                    .build();
                        }
                    }
                } catch (ClassNotFoundException _) {
                    log.debug("[Goya] |- component [bus] EventDeserializer |- event type class not found: {}",
                            eventTypeName);
                } catch (Exception e) {
                    log.debug("[Goya] |- component [bus] EventDeserializer |- failed to deserialize to {}: {}",
                            eventTypeName, e.getMessage());
                }
            }
        }

        // 4. 如果反序列化失败，返回 JSON 字符串和 eventName
        String jsonString = convertPayloadToString(payload);
        
        // 确保 eventName 存在（从 eventType 推断）
        if (StringUtils.isNoneBlank(eventName,eventTypeName)) {
            try {
                Class<?> eventType = Class.forName(eventTypeName);
                eventName = eventType.getSimpleName();
            } catch (ClassNotFoundException _) {
                log.debug("[Goya] |- component [bus] EventDeserializer |- failed to get eventName from eventType: {}",
                        eventTypeName);
            }
        }

        log.debug("[Goya] |- component [bus] EventDeserializer |- failed to deserialize, return JSON string with eventName: {}",
                eventName);

        return DeserializationResult.builder()
                .event(null)
                .eventName(eventName)
                .jsonString(jsonString)
                .isDeserialized(false)
                .build();
    }

    /**
     * 将 payload 反序列化为指定的事件类型
     *
     * @param payload    payload
     * @param targetType 目标类型
     * @return IEvent 对象，如果失败返回 null
     */
    private IEvent deserializeToEventType(Object payload, Class<? extends IEvent> targetType) {
        try {
            String json = convertPayloadToString(payload);
            if (json == null || json.isBlank()) {
                return null;
            }
            return JsonUtils.fromJson(json, targetType);
        } catch (Exception e) {
            log.debug("[Goya] |- component [bus] EventDeserializer |- failed to deserialize payload: {}",
                    e.getMessage());
            return null;
        }
    }

    /**
     * 将 payload 转换为 JSON 字符串
     *
     * @param payload payload
     * @return JSON 字符串
     */
    private String convertPayloadToString(Object payload) {
        return switch (payload) {
            case null -> "{}";
            case String str -> str;
            case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
            default -> JsonUtils.toJson(payload);
        };

    }
}

