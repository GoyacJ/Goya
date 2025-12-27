package com.ysmjjsy.goya.component.bus.processor.interceptor;

import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.deserializer.DeserializationResult;
import com.ysmjjsy.goya.component.bus.processor.BusEventListenerScanner;
import com.ysmjjsy.goya.component.bus.processor.EventContext;
import com.ysmjjsy.goya.component.bus.processor.IEventInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>路由拦截器</p>
 * <p>负责根据事件匹配监听器，支持事件名称匹配、类型匹配、SpEL 条件表达式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class RouteInterceptor implements IEventInterceptor {

    private final BusEventListenerScanner scanner;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final java.util.Map<String, Expression> expressionCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void intercept(EventContext context) {
        if (context.isAborted()) {
            return;
        }

        EventScope scope = context.getScope();
        DeserializationResult result = context.getDeserializationResult();

        if (result == null || !result.isUsable()) {
            log.warn("[Goya] |- component [bus] RouteInterceptor |- deserialization result is not usable, abort");
            context.abort("Deserialization result is not usable");
            return;
        }

        List<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new ArrayList<>();

        if (result.isDeserialized() && result.getEvent() != null) {
            // 反序列化成功，同时匹配事件对象监听器和 String 监听器
            IEvent event = result.getEvent();
            List<BusEventListenerScanner.EventListenerMetadata> eventListeners = findMatchingListeners(event, scope);
            List<BusEventListenerScanner.EventListenerMetadata> stringListeners = findMatchingListenersForString(result.getEventName(), scope);
            
            // 合并两种类型的监听器（使用 Set 去重）
            Set<BusEventListenerScanner.EventListenerMetadata> allMatched = new HashSet<>(eventListeners);
            allMatched.addAll(stringListeners);
            matchedListeners = new ArrayList<>(allMatched);
        } else {
            // 反序列化失败，使用 String 监听器匹配
            String eventName = result.getEventName();
            matchedListeners = findMatchingListenersForString(eventName, scope);
        }

        context.setMatchedListeners(matchedListeners);

        if (matchedListeners.isEmpty()) {
            log.debug("[Goya] |- component [bus] RouteInterceptor |- no matching listeners found for scope: [{}]",
                    scope);
            // 不中止，只是记录日志（可能其他拦截器会处理）
        } else {
            log.debug("[Goya] |- component [bus] RouteInterceptor |- found [{}] matching listeners",
                    matchedListeners.size());
        }
    }

    /**
     * 查找匹配的监听器
     * <p>只通过 eventName 匹配，不再使用 getSimpleName() 匹配</p>
     * <p>如果事件有命名空间，监听器的 eventNames 必须包含完整的事件名称（包含命名空间）</p>
     *
     * @param event 事件
     * @param scope 事件作用域
     * @return 匹配的监听器列表
     */
    private List<BusEventListenerScanner.EventListenerMetadata> findMatchingListeners(IEvent event, EventScope scope) {
        String eventName = event.eventName();
        String eventNamespace = event.eventNamespace();
        Set<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new HashSet<>();

        // 通过 eventName 查找（必须完全匹配）
        List<BusEventListenerScanner.EventListenerMetadata> eventNameListeners = scanner.findByEventName(eventName);
        
        for (BusEventListenerScanner.EventListenerMetadata metadata : eventNameListeners) {
            if (matchesScopeAndCondition(event, scope, metadata)) {
                // 检查参数类型是否匹配（排除 String 类型，String 类型由 findMatchingListenersForString 处理）
                Method method = metadata.method();
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0) {
                    Class<?> firstParamType = parameterTypes[0];
                    // 如果第一个参数是 String，跳过（由 findMatchingListenersForString 处理）
                    if (firstParamType == String.class) {
                        continue;
                    }
                    // 检查参数类型是否与事件类型兼容
                    if (!firstParamType.isAssignableFrom(event.getClass())) {
                        log.trace("[Goya] |- component [bus] RouteInterceptor |- parameter type mismatch for listener [{}], " +
                                "expected [{}], got [{}], skip",
                                method.getName(), firstParamType.getName(), event.getClass().getName());
                        continue;
                    }
                }
                
                matchedListeners.add(metadata);
            }
        }

        // 如果事件有命名空间，验证命名空间匹配
        if (!"default".equals(eventNamespace) && !matchedListeners.isEmpty()) {
            // 检查监听器的 eventNames 是否包含命名空间
            // 这里假设 eventName 的格式是 {namespace}.{domain}.{action} 或 {domain}.{action}
            // 如果事件有命名空间，监听器应该使用完整的事件名称
            log.trace("[Goya] |- component [bus] RouteInterceptor |- event [{}] has namespace [{}], matched {} listeners",
                    eventName, eventNamespace, matchedListeners.size());
        }

        return new ArrayList<>(matchedListeners);
    }

    /**
     * 查找匹配的 String 监听器
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     * @return 匹配的监听器列表
     */
    private List<BusEventListenerScanner.EventListenerMetadata> findMatchingListenersForString(String eventName,
                                                                                                EventScope scope) {
        if (eventName == null || eventName.isBlank()) {
            return new ArrayList<>();
        }

        List<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new ArrayList<>();
        List<BusEventListenerScanner.EventListenerMetadata> eventNameListeners = scanner.findByEventName(eventName);

        for (BusEventListenerScanner.EventListenerMetadata metadata : eventNameListeners) {
            Method method = metadata.method();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0 && parameterTypes[0] == String.class) {
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
    private boolean matchesScopeAndCondition(IEvent event, EventScope scope,
                                             BusEventListenerScanner.EventListenerMetadata metadata) {
        if (!matchesScope(scope, metadata)) {
            return false;
        }

        com.ysmjjsy.goya.component.bus.annotation.BusEventListener annotation = metadata.annotation();
        String condition = annotation.condition();
        if (condition != null && !condition.isBlank()) {
            try {
                Expression expression = expressionCache.computeIfAbsent(condition,
                        expressionParser::parseExpression);
                EvaluationContext evalContext = new StandardEvaluationContext();
                evalContext.setVariable("event", event);
                Boolean result = expression.getValue(evalContext, Boolean.class);
                return result != null && result;
            } catch (Exception e) {
                log.warn("[Goya] |- component [bus] RouteInterceptor |- failed to evaluate condition [{}]: {}",
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
        com.ysmjjsy.goya.component.bus.annotation.BusEventListener annotation = metadata.annotation();
        EventScope[] scopes = annotation.scope();
        for (EventScope s : scopes) {
            if (s == scope || s == EventScope.ALL) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 300;
    }
}

