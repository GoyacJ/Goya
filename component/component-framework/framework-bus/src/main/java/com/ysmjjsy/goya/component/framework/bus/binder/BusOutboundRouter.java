package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

/**
 * <p>出站路由器：订阅 outbound(binding)，按选择的 binder 转发到对应 binder 专用 outbound 通道</p>
 * 这样即可支持：
 * - 默认 binder 为 kafka
 * - 单次发送强制 rabbit（SendOptions.binder("rabbit")）
 *
 * @author goya
 * @since 2026/1/27 01:01
 */
@RequiredArgsConstructor
public final class BusOutboundRouter implements MessageHandler {

    private final String bindingName;
    private final BusBinding binding;
    private final BinderSelector selector;
    private final BusChannels channels;
    private final Map<String, SubscribableChannel> binderOutboundChannels;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String binderName = selector.selectForOutbound(binding, message);

        // 确保 header 中有 binding，便于下游观测/dispatcher
        Message<?> msg = message;
        if (msg.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING) == null) {
            msg = MessageBuilder.fromMessage(msg)
                    .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                    .build();
        }

        SubscribableChannel target = binderOutboundChannels.get(binderName.toLowerCase());
        if (target == null) {
            // 如果指定 binder 不存在，回退到 local
            target = binderOutboundChannels.get("local");
        }
        if (target == null) {
            throw new MessagingException(msg, "未找到可用 binder outbound 通道: " + binderName);
        }
        target.send(msg);
    }
}