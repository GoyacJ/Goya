package com.ysmjjsy.goya.component.bus.ack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

/**
 * <p>确认适配器</p>
 * <p>将不同 MQ 的 Acknowledgment 对象适配为统一的 IEventAcknowledgment 接口</p>
 * <p>支持 Kafka、RabbitMQ 等不同 MQ 的 ACK 机制</p>
 * <p><strong>设计说明：</strong></p>
 * <ul>
 *   <li>为什么使用反射：避免直接依赖 MQ 特定的 Acknowledgment 类型（如 Kafka 的 Acknowledgment、
 *       RabbitMQ 的 AcknowledgmentCallback），保持 component-bus 与具体 MQ 实现的解耦。</li>
 *   <li>性能影响：反射调用会有一定的性能开销，但 ACK 操作不是高频操作，性能影响可接受。
 *       如果未来需要优化，可以考虑使用策略模式或动态代理。</li>
 *   <li>错误处理：如果 MQ 的 Acknowledgment 对象不支持某个方法（如 reject），会降级为支持的方法（如 acknowledge）。</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class AcknowledgmentAdapter {

    private AcknowledgmentAdapter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 从 Message 中提取 Acknowledgment 并适配为 IEventAcknowledgment
     *
     * @param message 消息
     * @return IEventAcknowledgment 实例，如果不存在则返回 null
     */
    public static IEventAcknowledgment adapt(Message<?> message) {
        if (message == null) {
            return null;
        }

        MessageHeaders headers = message.getHeaders();
        Object acknowledgment = getAcknowledgment(headers);

        if (acknowledgment == null) {
            return null;
        }

        return wrap(acknowledgment);
    }

    /**
     * 从 MessageHeaders 中提取 Acknowledgment 对象
     *
     * @param headers 消息头
     * @return Acknowledgment 对象，如果不存在则返回 null
     */
    private static Object getAcknowledgment(MessageHeaders headers) {
        // 1. 尝试 Kafka Acknowledgment
        Object acknowledgment = headers.get("kafka_acknowledgment");
        if (acknowledgment != null) {
            return acknowledgment;
        }

        // 2. 尝试 Spring Integration Acknowledgment
        acknowledgment = headers.get(IntegrationMessageHeaderAccessor.ACKNOWLEDGMENT_CALLBACK);
        if (acknowledgment != null) {
            return acknowledgment;
        }

        // 3. 遍历所有 headers，查找 Acknowledgment 类型的对象
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            Object value = entry.getValue();
            if (value != null && value.getClass().getName().contains("Acknowledgment")) {
                return value;
            }
        }

        return null;
    }

    /**
     * 将原始 Acknowledgment 对象包装为 IEventAcknowledgment
     *
     * @param acknowledgment 原始 Acknowledgment 对象
     * @return IEventAcknowledgment 实例
     */
    private static IEventAcknowledgment wrap(Object acknowledgment) {
        if (acknowledgment == null) {
            return null;
        }

        // 使用反射调用 acknowledge() 方法，避免直接依赖 MQ 特定类型
        return new IEventAcknowledgment() {
            @Override
            public void acknowledge() throws Exception {
                try {
                    java.lang.reflect.Method acknowledgeMethod = acknowledgment.getClass().getMethod("acknowledge");
                    acknowledgeMethod.invoke(acknowledgment);
                } catch (NoSuchMethodException e) {
                    log.warn("[Goya] |- component [bus] AcknowledgmentAdapter |- Acknowledgment class [{}] does not have acknowledge() method",
                            acknowledgment.getClass().getName());
                    throw new UnsupportedOperationException("Acknowledgment does not support acknowledge()", e);
                } catch (Exception e) {
                    log.error("[Goya] |- component [bus] AcknowledgmentAdapter |- failed to acknowledge: {}",
                            e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            public void reject() throws Exception {
                try {
                    // 尝试调用 reject() 方法
                    java.lang.reflect.Method rejectMethod = acknowledgment.getClass().getMethod("reject");
                    rejectMethod.invoke(acknowledgment);
                } catch (NoSuchMethodException e) {
                    // 如果不支持 reject，降级为 acknowledge
                    log.debug("[Goya] |- component [bus] AcknowledgmentAdapter |- Acknowledgment does not support reject(), " +
                            "fallback to acknowledge()");
                    acknowledge();
                } catch (Exception e) {
                    log.error("[Goya] |- component [bus] AcknowledgmentAdapter |- failed to reject: {}",
                            e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            public void reject(boolean requeue) throws Exception {
                try {
                    // 尝试调用 reject(boolean) 方法
                    java.lang.reflect.Method rejectMethod = acknowledgment.getClass().getMethod("reject", boolean.class);
                    rejectMethod.invoke(acknowledgment, requeue);
                } catch (NoSuchMethodException e) {
                    // 如果不支持 reject(boolean)，降级为 reject()
                    log.debug("[Goya] |- component [bus] AcknowledgmentAdapter |- Acknowledgment does not support reject(boolean), " +
                            "fallback to reject()");
                    reject();
                } catch (Exception e) {
                    log.error("[Goya] |- component [bus] AcknowledgmentAdapter |- failed to reject(requeue={}): {}",
                            requeue, e.getMessage(), e);
                    throw e;
                }
            }
        };
    }
}

