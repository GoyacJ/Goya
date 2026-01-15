package com.ysmjjsy.goya.component.bus.stream.processor.interceptor;

import com.ysmjjsy.goya.component.bus.stream.deserializer.DeserializationResult;
import com.ysmjjsy.goya.component.bus.stream.deserializer.EventDeserializer;
import com.ysmjjsy.goya.component.bus.stream.processor.EventContext;
import com.ysmjjsy.goya.component.bus.stream.processor.IEventInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>反序列化拦截器</p>
 * <p>负责从 Message 中反序列化事件</p>
 * <p>如果反序列化失败，设置 deserializationResult 但不会中止处理（支持 String 降级方案）</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class DeserializeInterceptor implements IEventInterceptor {

    private final ObjectProvider<EventDeserializer> eventDeserializerProvider;

    @Override
    public void intercept(EventContext context) {
        if (context.isAborted()) {
            return;
        }

        if (context.getMessage() == null) {
            log.warn("[Goya] |- component [bus] DeserializeInterceptor |- message is null, abort");
            context.abort("Message is null");
            return;
        }

        EventDeserializer eventDeserializer = eventDeserializerProvider.getIfAvailable();
        if (eventDeserializer == null) {
            log.warn("[Goya] |- component [bus] DeserializeInterceptor |- EventDeserializer not available, abort");
            context.abort("EventDeserializer not available");
            return;
        }

        // 反序列化消息
        DeserializationResult result = eventDeserializer.deserialize(context.getMessage());
        context.setDeserializationResult(result);

        if (!result.isUsable()) {
            log.warn("[Goya] |- component [bus] DeserializeInterceptor |- deserialization result is not usable, abort");
            context.abort("Deserialization result is not usable");
            return;
        }

        // 如果反序列化成功，设置事件对象
        if (result.isDeserialized() && result.getEvent() != null) {
            context.setEvent(result.getEvent());
            log.debug("[Goya] |- component [bus] DeserializeInterceptor |- event deserialized: [{}]",
                    result.getEvent().eventName());
        } else {
            // 反序列化失败，但不中止（支持 String 降级方案）
            log.debug("[Goya] |- component [bus] DeserializeInterceptor |- deserialization failed, " +
                    "will use String fallback: [{}]", result.getEventName());
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}

