package com.ysmjjsy.goya.component.bus.stream.processor.interceptor;

import com.ysmjjsy.goya.component.bus.stream.ack.AcknowledgmentAdapter;
import com.ysmjjsy.goya.component.bus.stream.ack.IEventAcknowledgment;
import com.ysmjjsy.goya.component.bus.stream.annotation.BusEventListener;
import com.ysmjjsy.goya.component.bus.stream.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import com.ysmjjsy.goya.component.bus.stream.deserializer.DeserializationResult;
import com.ysmjjsy.goya.component.bus.stream.enums.AckMode;
import com.ysmjjsy.goya.component.bus.stream.handler.IIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.stream.processor.BusEventListenerScanner;
import com.ysmjjsy.goya.component.bus.stream.processor.EventContext;
import com.ysmjjsy.goya.component.bus.stream.processor.IEventInterceptor;
import com.ysmjjsy.goya.component.bus.stream.publish.MetadataAccessor;
import com.ysmjjsy.goya.component.core.exception.AbstractRuntimeException;
import com.ysmjjsy.goya.component.core.exception.AbstractSystemException;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>监听器调用拦截器</p>
 * <p>负责调用匹配的监听器方法，支持监听器级别的幂等性检查、手动 ACK 注入等</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class InvokeInterceptor implements IEventInterceptor {

    private final ObjectProvider<IIdempotencyHandler> idempotencyHandlerProvider;
    private final BusProperties busProperties;

    @Override
    public void intercept(EventContext context) {
        if (context.isAborted()) {
            return;
        }

        if (context.getMatchedListeners() == null || context.getMatchedListeners().isEmpty()) {
            log.debug("[Goya] |- component [bus] InvokeInterceptor |- no matched listeners, skip");
            return;
        }

        DeserializationResult result = context.getDeserializationResult();
        String globalIdempotencyKey = MetadataAccessor.getIdempotencyKey(context.getMessage().getHeaders());
        IIdempotencyHandler idempotencyHandler = idempotencyHandlerProvider.getIfAvailable();

        // 遍历所有匹配的监听器
        for (BusEventListenerScanner.EventListenerMetadata metadata : context.getMatchedListeners()) {
            // 监听器级别的幂等性检查
            String listenerIdempotencyKey = buildListenerIdempotencyKey(globalIdempotencyKey, metadata);
            if (listenerIdempotencyKey != null && idempotencyHandler != null
                    && !idempotencyHandler.checkAndSetAtomic(listenerIdempotencyKey)) {
                log.debug("[Goya] |- component [bus] InvokeInterceptor |- event already processed by listener [{}], skip: [{}]",
                        metadata.method().getName(), listenerIdempotencyKey);
                continue;
            }

            try {
                if (result.isDeserialized() && result.getEvent() != null) {
                    // 调用事件对象监听器
                    invokeListener(metadata, result.getEvent(), context.getMessage());
                } else {
                    // 调用 String 监听器
                    invokeListenerForString(metadata, result.getJsonString(), context.getMessage().getHeaders());
                }
            } catch (Exception e) {
                if (isSystemException(e)) {
                    log.error("[Goya] |- component [bus] InvokeInterceptor |- system exception in listener [{}]: {}",
                            metadata.method().getName(), e.getMessage(), e);
                    // 系统异常总是中断执行
                    throw e;
                } else {
                    // 业务异常
                    log.warn("[Goya] |- component [bus] InvokeInterceptor |- business exception in listener [{}]: {}",
                            metadata.method().getName(), e.getMessage());
                    
                    // 根据配置决定是否继续执行其他监听器
                    boolean continueOnBusinessException = busProperties.listener().continueOnBusinessException();
                    if (!continueOnBusinessException) {
                        // 如果配置为 false（默认值），继续执行其他监听器
                        // 这里不抛出异常，继续循环
                        log.debug("[Goya] |- component [bus] InvokeInterceptor |- continue processing other listeners after business exception");
                    } else {
                        // 如果配置为 true，中断执行
                        log.warn("[Goya] |- component [bus] InvokeInterceptor |- interrupt processing other listeners due to business exception (continueOnBusinessException=true)");
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * 调用监听器方法
     *
     * @param metadata 监听器元数据
     * @param event    事件
     * @param message  消息（本地事件为 null）
     */
    private void invokeListener(BusEventListenerScanner.EventListenerMetadata metadata, IBusEvent event,
                                @Nullable Message<?> message) {
        Method method = metadata.method();
        Object bean = metadata.bean();
        BusEventListener annotation = metadata.annotation();

        if (bean == null) {
            log.warn("[Goya] |- component [bus] InvokeInterceptor |- bean is null for method [{}]", method.getName());
            return;
        }

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                log.warn("[Goya] |- component [bus] InvokeInterceptor |- method [{}] has no parameters", method.getName());
                return;
            }

            Object[] args = new Object[parameterTypes.length];
            Class<?> targetParamType = parameterTypes[0];

            // 根据参数类型决定传递方式
            if (targetParamType == String.class) {
                args[0] = GoyaJson.toJson(event);
            } else {
                Object convertedEvent = event;
                if (!targetParamType.isAssignableFrom(event.getClass())) {
                    try {
                        String json = GoyaJson.toJson(event);
                        convertedEvent = GoyaJson.fromJson(json, targetParamType);
                        if (convertedEvent == null) {
                            log.debug("[Goya] |- component [bus] InvokeInterceptor |- failed to deserialize event to [{}], skip",
                                    targetParamType.getName());
                            return;
                        }
                    } catch (Exception e) {
                        log.debug("[Goya] |- component [bus] InvokeInterceptor |- failed to convert event type from [{}] to [{}] for listener [{}]: {}, skip",
                                event.getClass().getName(), targetParamType.getName(), method.getName(), e.getMessage());
                        return;
                    }
                }
                args[0] = convertedEvent;
            }

            // 如果指定了手动 ACK，第二个参数应该是 IEventAcknowledgment（仅远程事件）
            if (annotation.ackMode() == AckMode.MANUAL && parameterTypes.length == 2 && message != null) {
                IEventAcknowledgment acknowledgment = AcknowledgmentAdapter.adapt(message);
                if (acknowledgment != null) {
                    Class<?> ackType = parameterTypes[1];
                    if (ackType == IEventAcknowledgment.class || ackType.isAssignableFrom(acknowledgment.getClass())) {
                        args[1] = acknowledgment;
                    } else {
                        log.warn("[Goya] |- component [bus] InvokeInterceptor |- Acknowledgment type mismatch for method [{}], " +
                                "expected IEventAcknowledgment, got [{}]",
                                method.getName(), ackType.getName());
                    }
                }
            }

            // 调用方法
            ReflectionUtils.invokeMethod(method, bean, args);
        } catch (Exception e) {
            log.error("[Goya] |- component [bus] InvokeInterceptor |- failed to invoke listener method [{}]: {}",
                    method.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 调用 String 类型的监听器方法
     *
     * @param metadata   监听器元数据
     * @param jsonString JSON 字符串
     * @param headers    消息头
     */
    private void invokeListenerForString(BusEventListenerScanner.EventListenerMetadata metadata, String jsonString,
                                         Map<String, Object> headers) {
        Method method = metadata.method();
        Object bean = metadata.bean();

        if (bean == null) {
            log.warn("[Goya] |- component [bus] InvokeInterceptor |- bean is null for method [{}]", method.getName());
            return;
        }

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            args[0] = jsonString;

            // 调用方法
            ReflectionUtils.invokeMethod(method, bean, args);
        } catch (Exception e) {
            log.error("[Goya] |- component [bus] InvokeInterceptor |- failed to invoke String listener method [{}]: {}",
                    method.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 构建监听器级别的幂等键
     *
     * @param globalIdempotencyKey 全局幂等键
     * @param metadata             监听器元数据
     * @return 监听器级别的幂等键
     */
    private String buildListenerIdempotencyKey(String globalIdempotencyKey,
                                                BusEventListenerScanner.EventListenerMetadata metadata) {
        if (globalIdempotencyKey == null || globalIdempotencyKey.isBlank()) {
            return null;
        }
        String listenerIdentifier = metadata.bean().getClass().getName() + "#" + metadata.method().getName();
        return globalIdempotencyKey + ":" + listenerIdentifier;
    }

    /**
     * 判断是否为系统异常
     *
     * @param e 异常
     * @return 是否为系统异常
     */
    private boolean isSystemException(Exception e) {
        if (e instanceof AbstractSystemException) {
            return true;
        }
        if (e instanceof RuntimeException) {
            return !(e instanceof AbstractRuntimeException);
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 400;
    }
}

