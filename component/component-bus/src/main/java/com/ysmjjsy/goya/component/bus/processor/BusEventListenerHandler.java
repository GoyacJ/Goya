package com.ysmjjsy.goya.component.bus.processor;

import com.ysmjjsy.goya.component.bus.annotation.BusEventListener;
import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.deserializer.EventDeserializer;
import com.ysmjjsy.goya.component.bus.enums.AckMode;
import com.ysmjjsy.goya.component.bus.exception.BusException;
import com.ysmjjsy.goya.component.bus.handler.IIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.publish.MetadataAccessor;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;
import com.ysmjjsy.goya.component.common.definition.exception.SystemException;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>事件监听器处理器</p>
 * <p>负责事件路由、幂等性检查等业务逻辑</p>
 * <p>统一处理本地和远程事件，根据参数类型自动序列化</p>
 * <p>作为普通 Bean，可以正常依赖业务组件</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 处理本地事件
 * handler.handleLocalEvent(message);
 *
 * // 处理远程事件
 * handler.handleRemoteMessage(message);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class BusEventListenerHandler {

    private final BusEventListenerScanner scanner;
    private final ObjectProvider<IIdempotencyHandler> idempotencyHandlerProvider;
    private final ObjectProvider<EventDeserializer> eventDeserializerProvider;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    // SpEL 表达式缓存：避免重复编译
    private final Map<String, Expression> expressionCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 处理本地事件
     * <p>LocalEventPublisher 会调用此方法，路由到对应的 @BusEventListener 方法</p>
     *
     * @param message 消息
     */
    public void handleLocalEvent(Message<IEvent> message) {
        if (message == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- received null message");
            return;
        }

        IEvent event = message.getPayload();
        if (event == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- received message with null payload");
            return;
        }

        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- handle local event [{}]", event.eventName());

        // 查找匹配的监听器
        List<BusEventListenerScanner.EventListenerMetadata> matchingListeners = findMatchingListeners(event, EventScope.LOCAL);

        for (BusEventListenerScanner.EventListenerMetadata metadata : matchingListeners) {
            try {
                // 调用监听器方法（本地事件直接调用，不传递 message）
                invokeListener(metadata, event, null);
            } catch (Exception e) {
                log.error("[Goya] |- component [bus] BusEventListenerHandler |- failed to invoke listener [{}]: {}",
                        metadata.method().getName(), e.getMessage(), e);
                // 本地事件异常直接抛出，不重试
                throw new BusException("Failed to handle local event: " + event.eventName(), e);
            }
        }
    }

    /**
     * 处理远程消息
     * <p>统一处理远程事件，支持反序列化和 String 降级方案</p>
     * <p>SCS Consumer 会调用此方法，路由到对应的 @BusEventListener 方法</p>
     *
     * @param message 消息
     */
    public void handleRemoteMessage(Message<?> message) {
        if (message == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- received null message");
            return;
        }

        MessageHeaders headers = message.getHeaders();

        // 设置 MDC（链路追踪）
        String traceId = MetadataAccessor.getTraceId(headers);
        String previousTraceId = null;
        if (traceId != null) {
            previousTraceId = org.slf4j.MDC.get("traceId");
            org.slf4j.MDC.put("traceId", traceId);
        }

        try {
            // 全局幂等性检查（入口处）
            String globalIdempotencyKey = MetadataAccessor.getIdempotencyKey(headers);
            IIdempotencyHandler idempotencyHandler = idempotencyHandlerProvider.getIfAvailable();
            if (globalIdempotencyKey != null && idempotencyHandler != null) {
                if (!idempotencyHandler.checkAndSet(globalIdempotencyKey)) {
                    log.debug("[Goya] |- component [bus] BusEventListenerHandler |- event already processed globally, skip: [{}]",
                            globalIdempotencyKey);
                    return;
                }
            }

            // 使用 EventDeserializer 反序列化
            EventDeserializer eventDeserializer = eventDeserializerProvider.getIfAvailable();
            if (eventDeserializer == null) {
                log.warn("[Goya] |- component [bus] BusEventListenerHandler |- EventDeserializer not available");
                return;
            }

            com.ysmjjsy.goya.component.bus.deserializer.DeserializationResult result = eventDeserializer.deserialize(message);

            if (!result.isUsable()) {
                log.warn("[Goya] |- component [bus] BusEventListenerHandler |- deserialization result is not usable");
                return;
            }

            if (result.isDeserialized() && result.getEvent() != null) {
                // 反序列化成功，走本地事件流程
                IEvent event = result.getEvent();
                log.debug("[Goya] |- component [bus] BusEventListenerHandler |- handle remote event [{}] (deserialized)",
                        event.eventName());

                // 查找匹配的监听器
                List<BusEventListenerScanner.EventListenerMetadata> matchingListeners = findMatchingListeners(event, EventScope.REMOTE);

                for (BusEventListenerScanner.EventListenerMetadata metadata : matchingListeners) {
                    // 监听器级别的幂等性检查
                    String listenerIdempotencyKey = buildListenerIdempotencyKey(globalIdempotencyKey, metadata);
                    if (listenerIdempotencyKey != null && idempotencyHandler != null
                            && !idempotencyHandler.checkAndSet(listenerIdempotencyKey)) {
                        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- event already processed by listener [{}], skip: [{}]",
                                metadata.method().getName(), listenerIdempotencyKey);
                        continue;
                    }

                    try {
                        // 构建 Message<IEvent> 以支持手动 ACK
                        Message<IEvent> eventMessage = MessageBuilder
                                .withPayload(event)
                                .copyHeaders(headers)
                                .build();
                        invokeListener(metadata, event, eventMessage);
                    } catch (Exception e) {
                        if (isSystemException(e)) {
                            log.error("[Goya] |- component [bus] BusEventListenerHandler |- system exception in listener [{}]: {}",
                                    metadata.method().getName(), e.getMessage(), e);
                            throw e;
                        } else {
                            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- business exception in listener [{}]: {}",
                                    metadata.method().getName(), e.getMessage());
                        }
                    }
                }
            } else {
                // 反序列化失败，走 String 监听器流程
                String eventName = result.getEventName();
                String jsonString = result.getJsonString();
                log.debug("[Goya] |- component [bus] BusEventListenerHandler |- handle remote event as String [{}] (fallback)",
                        eventName);

                // 查找匹配的 String 监听器（通过 eventName）
                List<BusEventListenerScanner.EventListenerMetadata> matchingListeners = findMatchingListenersForString(eventName, EventScope.REMOTE);

                for (BusEventListenerScanner.EventListenerMetadata metadata : matchingListeners) {
                    // 监听器级别的幂等性检查
                    String listenerIdempotencyKey = buildListenerIdempotencyKey(globalIdempotencyKey, metadata);
                    if (listenerIdempotencyKey != null && idempotencyHandler != null
                            && !idempotencyHandler.checkAndSet(listenerIdempotencyKey)) {
                        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- event already processed by String listener [{}], skip: [{}]",
                                metadata.method().getName(), listenerIdempotencyKey);
                        continue;
                    }

                    try {
                        // 调用 String 监听器
                        invokeListenerForString(metadata, jsonString, headers);
                    } catch (Exception e) {
                        if (isSystemException(e)) {
                            log.error("[Goya] |- component [bus] BusEventListenerHandler |- system exception in String listener [{}]: {}",
                                    metadata.method().getName(), e.getMessage(), e);
                            throw e;
                        } else {
                            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- business exception in String listener [{}]: {}",
                                    metadata.method().getName(), e.getMessage());
                        }
                    }
                }
            }
        } finally {
            // 清理 MDC
            if (traceId != null) {
                if (previousTraceId != null) {
                    org.slf4j.MDC.put("traceId", previousTraceId);
                } else {
                    org.slf4j.MDC.remove("traceId");
                }
            }
        }
    }

    /**
     * 调用监听器方法
     * <p>根据参数类型自动序列化：String 参数传递 JSON 字符串，具体类型参数传递反序列化后的对象</p>
     *
     * @param metadata 监听器元数据
     * @param event    事件
     * @param message  消息（本地事件为 null）
     */
    private void invokeListener(BusEventListenerScanner.EventListenerMetadata metadata, IEvent event, @Nullable Message<IEvent> message) {
        Method method = metadata.method();
        Object bean = metadata.bean();
        BusEventListener annotation = metadata.annotation();

        if (bean == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- bean is null for method [{}]", method.getName());
            return;
        }

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                log.warn("[Goya] |- component [bus] BusEventListenerHandler |- method [{}] has no parameters", method.getName());
                return;
            }

            Object[] args = new Object[parameterTypes.length];
            Class<?> targetParamType = parameterTypes[0];

            // 根据参数类型决定传递方式
            if (targetParamType == String.class) {
                // String 参数：传递 JSON 字符串
                args[0] = JsonUtils.toJson(event);
            } else {
                // 具体类型参数：尝试反序列化为该类型
                Object convertedEvent = event;
                if (!targetParamType.isAssignableFrom(event.getClass())) {
                    // 类型不匹配，尝试通过 JSON 转换
                    try {
                        String json = JsonUtils.toJson(event);
                        convertedEvent = JsonUtils.fromJson(json, targetParamType);
                        if (convertedEvent == null) {
                            log.debug("[Goya] |- component [bus] BusEventListenerHandler |- failed to deserialize event to [{}], skip",
                                    targetParamType.getName());
                            return;
                        }
                        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- converted event from [{}] to [{}] for listener [{}]",
                                event.getClass().getName(), targetParamType.getName(), method.getName());
                    } catch (Exception e) {
                        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- failed to convert event type from [{}] to [{}] for listener [{}]: {}, skip",
                                event.getClass().getName(), targetParamType.getName(), method.getName(), e.getMessage());
                        return;
                    }
                }
                args[0] = convertedEvent;
            }

            // 如果指定了手动 ACK，第二个参数应该是 Acknowledgment（仅远程事件）
            if (annotation.ackMode() == AckMode.MANUAL && parameterTypes.length == 2 && message != null) {
                // 从 Message headers 中获取 Acknowledgment
                Object acknowledgment = getAcknowledgment(message);

                if (acknowledgment != null) {
                    // 检查参数类型是否匹配
                    Class<?> ackType = parameterTypes[1];
                    if (ackType.isAssignableFrom(acknowledgment.getClass())) {
                        args[1] = acknowledgment;
                        log.trace("[Goya] |- component [bus] BusEventListenerHandler |- injected Acknowledgment for method [{}]",
                                method.getName());
                    } else {
                        log.warn("[Goya] |- component [bus] BusEventListenerHandler |- Acknowledgment type mismatch for method [{}], expected [{}], got [{}]",
                                method.getName(), ackType.getName(), acknowledgment.getClass().getName());
                    }
                } else {
                    log.debug("[Goya] |- component [bus] BusEventListenerHandler |- Acknowledgment not found in message headers for method [{}], " +
                            "make sure ackMode is set to MANUAL in Spring Cloud Stream configuration",
                            method.getName());
                }
            }

            // 调用方法
            ReflectionUtils.invokeMethod(method, bean, args);
        } catch (Exception e) {
            log.error("[Goya] |- component [bus] BusEventListenerHandler |- failed to invoke listener method [{}]: {}",
                    method.getName(), e.getMessage(), e);
            throw e;
        }
    }

    private static @Nullable Object getAcknowledgment(Message<IEvent> message) {
        Object acknowledgment = null;
        MessageHeaders messageHeaders = message.getHeaders();

        // 尝试多种方式获取 Acknowledgment
        acknowledgment = messageHeaders.get("kafka_acknowledgment");
        if (acknowledgment == null) {
            acknowledgment = messageHeaders.get(IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK);
        }
        if (acknowledgment == null) {
            // 遍历所有 headers，查找 Acknowledgment 类型的对象
            for (Map.Entry<String, Object> entry : messageHeaders.entrySet()) {
                Object value = entry.getValue();
                if (value != null && value.getClass().getName().contains("Acknowledgment")) {
                    acknowledgment = value;
                    break;
                }
            }
        }
        return acknowledgment;
    }

    /**
     * 查找匹配的监听器
     * <p>匹配规则：</p>
     * <ol>
     *   <li>作用域匹配（必须）</li>
     *   <li>事件匹配（满足任一即可）：@BusEventListener.eventNames 包含 event.eventName() OR 参数类型 getSimpleName() 等于 event.eventName()</li>
     *   <li>SpEL 条件匹配（如果指定，必须满足）</li>
     * </ol>
     *
     * @param event 事件
     * @param scope 事件作用域
     * @return 匹配的监听器列表
     */
    private List<BusEventListenerScanner.EventListenerMetadata> findMatchingListeners(IEvent event, EventScope scope) {
        String eventName = event.eventName();
        Set<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new HashSet<>();

        // 1. 通过 eventName 查找（@BusEventListener.eventNames 匹配）
        List<BusEventListenerScanner.EventListenerMetadata> eventNameListeners = scanner.findByEventName(eventName);
        for (BusEventListenerScanner.EventListenerMetadata metadata : eventNameListeners) {
            if (matchesScopeAndCondition(event, scope, metadata)) {
                matchedListeners.add(metadata);
            }
        }

        // 2. 通过参数类型 SimpleName 查找（参数类型 getSimpleName() 等于 event.eventName()）
        // 遍历所有监听器，检查参数类型
        for (BusEventListenerScanner.EventListenerMetadata metadata : scanner.getAllListeners()) {
            Method method = metadata.method();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0) {
                Class<?> paramType = parameterTypes[0];
                String paramTypeSimpleName = paramType.getSimpleName();
                // 如果参数类型的 SimpleName 等于 eventName，且作用域和条件匹配
                if (paramTypeSimpleName.equals(eventName) && matchesScopeAndCondition(event, scope, metadata)) {
                    matchedListeners.add(metadata);
                }
            }
        }

        return new ArrayList<>(matchedListeners);
    }

    /**
     * 查找匹配的 String 监听器（用于反序列化失败的情况）
     * <p>匹配规则：</p>
     * <ol>
     *   <li>作用域匹配（必须）</li>
     *   <li>@BusEventListener.eventNames 包含 eventName（必须）</li>
     * </ol>
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     * @return 匹配的监听器列表
     */
    private List<BusEventListenerScanner.EventListenerMetadata> findMatchingListenersForString(String eventName, EventScope scope) {
        if (eventName == null || eventName.isBlank()) {
            return new ArrayList<>();
        }

        List<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new ArrayList<>();
        List<BusEventListenerScanner.EventListenerMetadata> eventNameListeners = scanner.findByEventName(eventName);

        for (BusEventListenerScanner.EventListenerMetadata metadata : eventNameListeners) {
            // 检查参数类型必须是 String
            Method method = metadata.method();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0 && parameterTypes[0] == String.class) {
                // 检查作用域
                if (matchesScope(scope, metadata)) {
                    matchedListeners.add(metadata);
                }
            }
        }

        return matchedListeners;
    }

    /**
     * 检查作用域和条件是否匹配
     *
     * @param event    事件
     * @param scope    事件作用域
     * @param metadata 监听器元数据
     * @return 是否匹配
     */
    private boolean matchesScopeAndCondition(IEvent event, EventScope scope, BusEventListenerScanner.EventListenerMetadata metadata) {
        // 1. 检查作用域
        if (!matchesScope(scope, metadata)) {
            return false;
        }

        // 2. 检查 SpEL 条件
        BusEventListener annotation = metadata.annotation();
        String condition = annotation.condition();
        if (condition != null && !condition.isBlank()) {
            try {
                Expression expression = expressionCache.computeIfAbsent(condition,
                        expressionParser::parseExpression);
                EvaluationContext context = new StandardEvaluationContext();
                context.setVariable("event", event);
                Boolean result = expression.getValue(context, Boolean.class);
                return result != null && result;
            } catch (Exception e) {
                log.warn("[Goya] |- component [bus] BusEventListenerHandler |- failed to evaluate condition [{}]: {}",
                        condition, e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * 检查作用域是否匹配
     *
     * @param scope    事件作用域
     * @param metadata 监听器元数据
     * @return 是否匹配
     */
    private boolean matchesScope(EventScope scope, BusEventListenerScanner.EventListenerMetadata metadata) {
        BusEventListener annotation = metadata.annotation();
        EventScope[] scopes = annotation.scope();
        for (EventScope s : scopes) {
            if (s == scope || s == EventScope.ALL) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建监听器级别的幂等键
     * <p>格式：{globalIdempotencyKey}:{listenerIdentifier}</p>
     *
     * @param globalIdempotencyKey 全局幂等键
     * @param metadata             监听器元数据
     * @return 监听器级别的幂等键
     */
    private String buildListenerIdempotencyKey(String globalIdempotencyKey, BusEventListenerScanner.EventListenerMetadata metadata) {
        if (globalIdempotencyKey == null || globalIdempotencyKey.isBlank()) {
            return null;
        }

        // 使用监听器方法的完整签名作为标识符
        String listenerIdentifier = metadata.bean().getClass().getName() + "#" + metadata.method().getName();
        return globalIdempotencyKey + ":" + listenerIdentifier;
    }

    /**
     * 调用 String 类型的监听器方法（用于反序列化失败的情况）
     *
     * @param metadata   监听器元数据
     * @param jsonString JSON 字符串
     * @param headers    消息头
     */
    private void invokeListenerForString(BusEventListenerScanner.EventListenerMetadata metadata, String jsonString,
                                        Map<String, Object> headers) {
        Method method = metadata.method();
        Object bean = metadata.bean();
        BusEventListener annotation = metadata.annotation();

        if (bean == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- bean is null for method [{}]", method.getName());
            return;
        }

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            args[0] = jsonString; // 第一个参数是 String

            // 如果指定了手动 ACK，第二个参数应该是 Acknowledgment
            // 注意：String 监听器的手动 ACK 需要在 KafkaAutoConfiguration 中传递完整的 Message
            if (annotation.ackMode() == AckMode.MANUAL && parameterTypes.length == 2) {
                log.debug("[Goya] |- component [bus] BusEventListenerHandler |- String listener with MANUAL ackMode, " +
                        "but Acknowledgment is not available");
            }

            // 调用方法
            ReflectionUtils.invokeMethod(method, bean, args);
        } catch (Exception e) {
            log.error("[Goya] |- component [bus] BusEventListenerHandler |- failed to invoke String listener method [{}]: {}",
                    method.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 判断是否为系统异常
     * <p>系统异常应该重试，业务异常不应该重试</p>
     *
     * @param e 异常
     * @return 是否为系统异常
     */
    private boolean isSystemException(Exception e) {
        // 系统异常类型：SystemException、网络异常等
        if (e instanceof SystemException) {
            return true;
        }

        // RuntimeException 中，CommonException 是业务异常，其他可能是系统异常
        if (e instanceof RuntimeException) {
            return !(e instanceof AbstractRuntimeException);
        }

        // 其他异常类型（如 IOException、SQLException 等）视为系统异常
        return true;
    }
}

