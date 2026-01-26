package com.ysmjjsy.goya.component.framework.bus.stream;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>StreamBridge binder：出站通过 StreamBridge.send 发送</p>
 * 注意：
 * - 入站不在这里自动绑定（Cloud Stream 入站由函数式 Consumer 负责），见 BusStreamInboundAdapter
 * - 本 binder 仅在 classpath 存在 StreamBridge 且配置 preferStreamBridge=true 时注册
 *
 * @author goya
 * @since 2026/1/27 01:00
 */
public final class StreamBridgeBusBinder implements BusBinder {

    private final Object streamBridge;
    private final BusChannels channels;

    public StreamBridgeBusBinder(Object streamBridge, BusChannels channels) {
        this.streamBridge = Objects.requireNonNull(streamBridge);
        this.channels = Objects.requireNonNull(channels);
    }

    @Override
    public String name() {
        return "stream";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        outboundChannel.subscribe(msg -> {
            try {
                // 反射调用：StreamBridge#send(String, Object)
                String target = binding.destination();
                boolean ok = (boolean) streamBridge.getClass()
                        .getMethod("send", String.class, Object.class)
                        .invoke(streamBridge, target, msg);

                if (!ok) {
                    IllegalStateException ex = new IllegalStateException("StreamBridge.send 返回 false: target=" + target);
                    publish(ex, "stream-outbound-send-false", binding, msg);
                    throw ex;
                }
            } catch (Exception ex) {
                publish(ex, "stream-outbound-exception", binding, msg);
                if (ex instanceof RuntimeException re) throw re;
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        // 入站由用户函数式 Consumer 收到消息后调用 BusStreamInboundAdapter.accept 投递到 inbound(binding)
    }

    private void publish(Throwable ex, String stage, BusBinding binding, Message<?> msg) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("stage", stage);
        headers.put("binder", "stream");
        headers.put(DefaultBusMessageProducer.HDR_BINDING, binding.name());
        headers.put("destination", binding.destination());
        headers.put("messageHeaders", msg.getHeaders());
        channels.error().send(new ErrorMessage(ex, headers));
    }
}