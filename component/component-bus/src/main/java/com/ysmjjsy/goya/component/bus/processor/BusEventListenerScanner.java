package com.ysmjjsy.goya.component.bus.processor;

import com.ysmjjsy.goya.component.bus.enums.AckMode;
import com.ysmjjsy.goya.component.bus.annotation.BusEventListener;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>事件监听器扫描器</p>
 * <p>扫描 @BusEventListener 注解的方法，建立事件名称索引和参数类型索引</p>
 * <p>不依赖任何业务 Bean，避免 BeanPostProcessor 警告</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 通过 BusEventListenerHandler 使用扫描结果
 * BusEventListenerScanner scanner;
 * BusEventListenerHandler handler = new BusEventListenerHandler(scanner, ...);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class BusEventListenerScanner implements BeanPostProcessor {

    /**
     * 所有监听器元数据列表
     */
    @Getter
    private final List<EventListenerMetadata> allListeners = new ArrayList<>();

    /**
     * 事件名称索引：按 eventName 查找监听器
     * <p>key: eventName（@BusEventListener.eventNames 中的值）</p>
     * <p>value: 监听器列表</p>
     */
    private final Map<String, List<EventListenerMetadata>> eventNameIndex = new ConcurrentHashMap<>();

    /**
     * 参数类型索引：按参数类型的 getSimpleName() 查找监听器
     * <p>key: 参数类型的 getSimpleName()</p>
     * <p>value: 监听器列表</p>
     */
    private final Map<String, List<EventListenerMetadata>> paramTypeIndex = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            BusEventListener annotation = AnnotatedElementUtils.findMergedAnnotation(method, BusEventListener.class);
            if (annotation != null) {
                processBusEventListener(bean, method, annotation);
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        return bean;
    }

    /**
     * 处理 @BusEventListener 注解的方法
     *
     * @param bean       Bean 实例
     * @param method     方法
     * @param annotation 注解
     */
    private void processBusEventListener(Object bean, Method method, BusEventListener annotation) {
        // 检查方法参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0 || parameterTypes.length > 2) {
            log.warn("[Goya] |- component [bus] BusEventListenerScanner |- method [{}] must have 1 or 2 parameters",
                    method.getName());
            return;
        }

        // 第一个参数可以是 IEvent 类型或 String 类型
        Class<?> firstParamType = parameterTypes[0];
        boolean isStringListener = firstParamType == String.class;
        boolean isEventListener = IEvent.class.isAssignableFrom(firstParamType);

        if (!isStringListener && !isEventListener) {
            log.warn("[Goya] |- component [bus] BusEventListenerScanner |- first parameter of method [{}] must be IEvent or String",
                    method.getName());
            return;
        }

        // 如果指定了手动 ACK，检查第二个参数类型
        if (annotation.ackMode() == AckMode.MANUAL && parameterTypes.length == 2) {
            Class<?> ackType = parameterTypes[1];
            // 检查是否为 Acknowledgment 类型（Spring Cloud Stream 或 Spring Integration）
            boolean isAckType = ackType.getName().contains("Acknowledgment") ||
                    ackType.getName().contains("AcknowledgmentCallback");
            if (!isAckType) {
                log.warn("[Goya] |- component [bus] BusEventListenerScanner |- method [{}] with MANUAL ackMode should have Acknowledgment as second parameter",
                        method.getName());
            }
        }

        // 记录监听器元数据
        EventListenerMetadata metadata = new EventListenerMetadata(bean, method, annotation);

        // 添加到所有监听器列表
        allListeners.add(metadata);

        // 建立 eventName 索引（基于 @BusEventListener.eventNames）
        String[] eventNames = annotation.eventNames();
        if (eventNames.length > 0) {
            for (String eventName : eventNames) {
                if (eventName != null && !eventName.isBlank()) {
                    eventNameIndex.computeIfAbsent(eventName, k -> new ArrayList<>()).add(metadata);
                }
            }
        }

        // 建立参数类型索引（基于参数类型的 getSimpleName()）
        String paramTypeSimpleName = firstParamType.getSimpleName();
        paramTypeIndex.computeIfAbsent(paramTypeSimpleName, k -> new ArrayList<>()).add(metadata);

        log.debug("[Goya] |- component [bus] BusEventListenerScanner |- registered listener for method [{}] with scope {}, eventNames: {}, paramType: {}",
                method.getName(), java.util.Arrays.toString(annotation.scope()),
                java.util.Arrays.toString(eventNames), paramTypeSimpleName);
    }

    /**
     * 根据 eventName 查找监听器
     * <p>用于匹配 @BusEventListener.eventNames 中包含指定 eventName 的监听器</p>
     *
     * @param eventName 事件名称
     * @return 监听器列表，如果未找到返回空列表
     */
    public List<EventListenerMetadata> findByEventName(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            return Collections.emptyList();
        }
        return eventNameIndex.getOrDefault(eventName, Collections.emptyList());
    }

    /**
     * 根据参数类型 SimpleName 查找监听器
     * <p>用于匹配参数类型的 getSimpleName() 等于指定值的监听器</p>
     *
     * @param paramTypeSimpleName 参数类型的 getSimpleName()
     * @return 监听器列表，如果未找到返回空列表
     */
    public List<EventListenerMetadata> findByParamType(String paramTypeSimpleName) {
        if (paramTypeSimpleName == null || paramTypeSimpleName.isBlank()) {
            return Collections.emptyList();
        }
        return paramTypeIndex.getOrDefault(paramTypeSimpleName, Collections.emptyList());
    }

    /**
     * 事件监听器元数据
     */
    public record EventListenerMetadata(Object bean, Method method, BusEventListener annotation) {
    }
}

