package com.ysmjjsy.goya.component.bus.stream.processor;

import com.ysmjjsy.goya.component.bus.stream.annotation.BusEventListener;
import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import com.ysmjjsy.goya.component.bus.stream.enums.AckMode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
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
 * <p>扫描 @BusEventListener 注解的方法，建立事件名称索引</p>
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
public class BusEventListenerScanner implements BeanPostProcessor, SmartInitializingSingleton {

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
     * 事件名称重复检查：用于启动时检查重复的事件名称
     * <p>key: eventName</p>
     * <p>value: 监听器数量</p>
     */
    private final Map<String, Integer> eventNameCount = new ConcurrentHashMap<>();


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
        boolean isEventListener = IBusEvent.class.isAssignableFrom(firstParamType);

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
                    // 记录事件名称出现次数，用于重复检查
                    eventNameCount.merge(eventName, 1, Integer::sum);
                }
            }
        } else {
            // 如果没有指定 eventNames，记录警告
            log.warn("[Goya] |- component [bus] BusEventListenerScanner |- listener method [{}] does not specify eventNames. " +
                            "Event matching will fail. Please specify eventNames in @BusEventListener annotation.",
                    method.getName());
        }

        log.debug("[Goya] |- component [bus] BusEventListenerScanner |- registered listener for method [{}] with scope {}, eventNames: {}",
                method.getName(), java.util.Arrays.toString(annotation.scope()),
                java.util.Arrays.toString(eventNames));
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
     * 检查重复的事件名称
     * <p>在启动时调用，检查是否有多个监听器监听同一个事件名称</p>
     * <p>如果有重复，输出警告日志</p>
     */
    public void checkDuplicateEventNames() {
        for (Map.Entry<String, Integer> entry : eventNameCount.entrySet()) {
            if (entry.getValue() > 1) {
                log.warn("[Goya] |- component [bus] BusEventListenerScanner |- event name [{}] has {} listeners. " +
                                "This may cause duplicate event processing. Consider using namespaces to avoid conflicts.",
                        entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 在所有单例Bean初始化完成后，检查重复的事件名称
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.debug("[Goya] |- component [bus] BusEventListenerScanner |- checking duplicate event names after all beans initialized");
        checkDuplicateEventNames();
        
        // 检查监听器数量，超过50个时输出警告
        int listenerCount = allListeners.size();
        if (listenerCount > 50) {
            log.warn("[Goya] |- component [bus] BusEventListenerScanner |- total listener count [{}] exceeds recommended limit (50). " +
                            "This may impact routing performance. Consider optimizing listener distribution.",
                    listenerCount);
        }
    }

    /**
     * 事件监听器元数据
     */
    public record EventListenerMetadata(Object bean, Method method, BusEventListener annotation) {
    }
}

