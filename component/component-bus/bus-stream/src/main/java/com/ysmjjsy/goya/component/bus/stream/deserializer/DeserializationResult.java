package com.ysmjjsy.goya.component.bus.stream.deserializer;

import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * <p>反序列化结果</p>
 * <p>包含事件对象、事件名称、JSON 字符串等信息</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Getter
@Builder
public class DeserializationResult {

    /**
     * 事件对象（如果反序列化成功）
     */
    private final IBusEvent event;

    /**
     * 事件名称（事件的 eventName()）
     */
    private final String eventName;

    /**
     * JSON 字符串
     */
    private final String jsonString;

    /**
     * 是否成功反序列化为 IEvent
     */
    private final boolean isDeserialized;

    /**
     * 是否可以使用（至少要有 eventName 或 jsonString）
     */
    public boolean isUsable() {
        return eventName != null && !eventName.isBlank() && jsonString != null && !jsonString.isBlank();
    }
}

