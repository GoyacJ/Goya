package com.ysmjjsy.goya.component.bus.stream.processor;

import com.ysmjjsy.goya.component.bus.stream.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.bus.stream.definition.EventScope;
import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import com.ysmjjsy.goya.component.bus.stream.deserializer.DeserializationResult;
import com.ysmjjsy.goya.component.bus.stream.deserializer.EventDeserializer;
import com.ysmjjsy.goya.component.bus.stream.handler.IIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.stream.metrics.EventMetrics;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.DeserializeInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.IdempotencyInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.InvokeInterceptor;
import com.ysmjjsy.goya.component.bus.stream.processor.interceptor.RouteInterceptor;
import com.ysmjjsy.goya.component.bus.stream.publish.MetadataAccessor;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Comparator;
import java.util.List;

/**
 * <p>事件监听器处理器</p>
 * <p>负责事件路由、幂等性检查等业务逻辑</p>
 * <p>使用 Pipeline 模式处理事件，支持拦截器扩展</p>
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
    private final ObjectProvider<List<IEventInterceptor>> interceptorsProvider;
    private final BusProperties busProperties;

    /**
     * 处理本地事件
     * <p>LocalEventPublisher 会调用此方法，路由到对应的 @BusEventListener 方法</p>
     * <p>本地事件使用简化版 Pipeline，直接调用监听器</p>
     *
     * @param message 消息
     */
    public void handleLocalEvent(Message<IBusEvent> message) {
        if (message == null) {
            log.warn("[Goya] |- component [bus] BusEventListenerHandler |- received null message");
            return;
        }

        IBusEvent event = message.getPayload();

        log.debug("[Goya] |- component [bus] BusEventListenerHandler |- handle local event [{}]", event.eventName());

        long startTime = System.currentTimeMillis();
        try {
            EventMetrics.recordConsume(event.eventName(), EventScope.LOCAL);

            // 为本地事件创建 DeserializationResult（本地事件不需要反序列化，事件对象已存在）
            DeserializationResult deserializationResult = DeserializationResult.builder()
                    .event(event)
                    .eventName(event.eventName())
                    .jsonString(GoyaJson.toJson(event))
                    .isDeserialized(true)
                    .build();

            // 构建 EventContext（简化版，仅用于本地事件）
            EventContext context = EventContext.builder()
                    .message(message)
                    .scope(EventScope.LOCAL)
                    .event(event)
                    .deserializationResult(deserializationResult)
                    .build();

            // 使用 RouteInterceptor 查找匹配的监听器
            RouteInterceptor routeInterceptor =
                    new RouteInterceptor(scanner);
            routeInterceptor.intercept(context);

            if (context.isAborted() || context.getMatchedListeners() == null || context.getMatchedListeners().isEmpty()) {
                log.debug("[Goya] |- component [bus] BusEventListenerHandler |- no matching listeners for local event [{}]",
                        event.eventName());
                return;
            }

            // 使用 InvokeInterceptor 调用监听器
            InvokeInterceptor invokeInterceptor =
                    new InvokeInterceptor(idempotencyHandlerProvider, busProperties);
            invokeInterceptor.intercept(context);

            long duration = System.currentTimeMillis() - startTime;
            EventMetrics.recordSuccess(event.eventName(), duration);
        } catch (Exception e) {
            EventMetrics.recordFailure(event.eventName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 处理远程消息
     * <p>统一处理远程事件，使用 Pipeline 模式</p>
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

        long startTime = System.currentTimeMillis();
        String eventName = null;
        try {
            // 构建 EventContext
            EventContext context = EventContext.builder()
                    .message(message)
                    .scope(EventScope.REMOTE)
                    .build();

            // 获取事件名称（用于指标记录）
            eventName = MetadataAccessor.getEventName(message.getHeaders());
            if (eventName == null) {
                eventName = "unknown";
            }

            EventMetrics.recordConsume(eventName, EventScope.REMOTE);

            // 获取拦截器列表并排序
            List<IEventInterceptor> interceptors = interceptorsProvider.getIfAvailable();
            if (interceptors == null || interceptors.isEmpty()) {
                log.warn("[Goya] |- component [bus] BusEventListenerHandler |- no interceptors available, " +
                        "using default interceptors");
                // 使用默认拦截器
                interceptors = createDefaultInterceptors();
            }

            // 按顺序执行拦截器
            interceptors.sort(Comparator.comparingInt(IEventInterceptor::getOrder));
            for (IEventInterceptor interceptor : interceptors) {
                if (context.isAborted()) {
                    log.debug("[Goya] |- component [bus] BusEventListenerHandler |- processing aborted: [{}]",
                            context.getAbortReason());
                    return;
                }
                interceptor.intercept(context);
            }

            if (context.isAborted()) {
                log.debug("[Goya] |- component [bus] BusEventListenerHandler |- processing aborted after interceptors: [{}]",
                        context.getAbortReason());
            } else {
                // 更新事件名称（如果反序列化成功）
                if (context.getEvent() != null) {
                    eventName = context.getEvent().eventName();
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            EventMetrics.recordSuccess(eventName, duration);
        } catch (Exception e) {
            EventMetrics.recordFailure(eventName != null ? eventName : "unknown", e.getMessage());
            throw e;
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
     * 创建默认拦截器列表
     * <p>如果未注册自定义拦截器，使用默认拦截器</p>
     * <p>注意：这里无法注入 versionCheckerProvider，因为 BusEventListenerHandler 的构造函数中没有这个参数</p>
     * <p>实际使用时，应该通过 BusAutoConfiguration 注册的拦截器列表</p>
     *
     * @return 默认拦截器列表
     */
    private List<IEventInterceptor> createDefaultInterceptors() {
        // 实际使用时，应该通过 BusAutoConfiguration 注册的拦截器列表
        return List.of(
                new DeserializeInterceptor(eventDeserializerProvider),
                new IdempotencyInterceptor(idempotencyHandlerProvider),
                new RouteInterceptor(scanner),
                new InvokeInterceptor(idempotencyHandlerProvider, busProperties)
        );
    }

}

