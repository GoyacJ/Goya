package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.message.BusMessageListener;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.message.MessageEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>消息分发器：把 inbound 通道上的 Spring Message 分发到 {@link BusMessageListener} 标注的方法</p>
 * <p>
 * 设计要点：
 * <ul>
 *   <li>绑定名（binding）通过 header {@link DefaultBusMessageProducer#HDR_BINDING} 传递</li>
 *   <li>监听方法参数支持：MessageEnvelope 或 payload（自动解包）</li>
 *   <li>发生异常时：发布到全局 error 通道，并再次抛出，以便上层/官方机制触发重试或回滚</li>
 * </ul>
 * @author goya
 * @since 2026/1/26 23:51
 */
@Slf4j
public final class BusMessageDispatcher {

    private final Map<String, List<Invocation>> invocationsByBinding = new ConcurrentHashMap<>();

    private final BusChannels channels;

    public BusMessageDispatcher(BusChannels channels) {
        this.channels = Objects.requireNonNull(channels);
    }

    public void register(String binding, Object bean, Method method) {
        invocationsByBinding.computeIfAbsent(binding, k -> new ArrayList<>())
                .add(new Invocation(bean, method));
        log.info("Registered @BusMessageListener: binding='{}' -> {}#{}", binding, bean.getClass().getName(), method.getName());
    }

    public void subscribeAll() {
        for (String binding : invocationsByBinding.keySet()) {
            SubscribableChannel in = channels.inbound(binding);
            in.subscribe(this::handleMessage);
        }
    }

    private void handleMessage(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        String binding = (String) headers.get(DefaultBusMessageProducer.HDR_BINDING);
        if (binding == null) {
            log.warn("Inbound message missing header {}", DefaultBusMessageProducer.HDR_BINDING);
            return;
        }
        List<Invocation> invs = invocationsByBinding.get(binding);
        if (invs == null || invs.isEmpty()) {
            log.debug("No listeners for binding '{}'", binding);
            return;
        }

        // Build envelope from headers + payload (simple, zero-serialization).
        MessageEnvelope env = MessageEnvelope.builder(message.getPayload())
                .id(Objects.toString(headers.get(DefaultBusMessageProducer.HDR_ENVELOPE_ID), UUID.randomUUID().toString()))
                .type(Objects.toString(headers.get(DefaultBusMessageProducer.HDR_ENVELOPE_TYPE), message.getPayload().getClass().getName()))
                .key((String) headers.get(DefaultBusMessageProducer.HDR_ENVELOPE_KEY))
                .headers(headers)
                .build();

        for (Invocation inv : invs) {
            try {
                inv.invoke(env);
            } catch (Exception ex) {
                // 发布到统一错误通道，便于统一日志、告警、指标
                channels.error().send(new org.springframework.messaging.support.ErrorMessage(ex, java.util.Map.of(
                        "stage", "inbound",
                        DefaultBusMessageProducer.HDR_BINDING, binding,
                        DefaultBusMessageProducer.HDR_ENVELOPE_ID, env.id(),
                        DefaultBusMessageProducer.HDR_ENVELOPE_TYPE, env.type()
                )));
                throw ex;
            }
        }
    }

    private static final class Invocation {
        private final Object bean;
        private final Method method;

        private Invocation(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
            ReflectionUtils.makeAccessible(this.method);
        }

        private void invoke(MessageEnvelope<Object> env) {
            Class<?>[] params = method.getParameterTypes();
            try {
                if (params.length == 1) {
                    if (MessageEnvelope.class.isAssignableFrom(params[0])) {
                        method.invoke(bean, env);
                    } else {
                        method.invoke(bean, env.payload());
                    }
                } else {
                    throw new IllegalStateException("@BusMessageListener method must have exactly 1 parameter.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Bus listener invocation failed: " + bean.getClass().getName() + "#" + method.getName(), e);
            }
        }
    }
}
