package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/**
 * <p>本地 binder：JVM 内投递，不跨进程，不持久化</p>
 * 主要用于：单体、本地开发、测试、无 MQ 依赖的最小可运行模式
 *
 * @author goya
 * @since 2026/1/26 23:44
 */
@Slf4j
public final class LocalBusBinder implements BusBinder {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        // 本地 binder 的 outbound 实际通过 bridge 实现：outbound -> inbound
        // 这里不额外处理
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        // 本地 binder 无外部输入
    }

    /**
     * 建立 JVM 内桥接：从 outbound 复制消息到 inbound，并确保带上 bus.binding header。
     */
    public static void bridge(SubscribableChannel outbound, SubscribableChannel inbound, String bindingName) {
        Objects.requireNonNull(outbound);
        Objects.requireNonNull(inbound);
        Objects.requireNonNull(bindingName);

        outbound.subscribe(msg -> {
            Message<?> m = msg;
            if (m.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING) == null) {
                m = MessageBuilder.fromMessage(m)
                        .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                        .build();
            }
            inbound.send(m);
        });
    }
}
