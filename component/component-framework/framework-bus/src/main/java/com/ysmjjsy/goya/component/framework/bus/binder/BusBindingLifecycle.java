package com.ysmjjsy.goya.component.framework.bus.binder;

import com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties.BusProperties;
import com.ysmjjsy.goya.component.framework.bus.message.BusMessageDispatcher;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BindingResolver;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusBinderRegistry;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>binding 生命周期</p>
 * - 为每个 binding 建立 outbound 路由器（支持 per-send binder override）
 * - 为每个 binder 建立专用 outbound 通道并调用 binder.bindOutbound
 * - inbound：按 binding 的 binder/defaultBinder 选择一个 binder 进行 bindInbound
 * - inbound(binding) 订阅 dispatcher
 * - inbound(binding) 安装拦截器：补齐 bus.binding header（防止 adapter 输出缺 header）
 * @author goya
 * @since 2026/1/27 01:02
 */
@RequiredArgsConstructor
public final class BusBindingLifecycle implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(BusBindingLifecycle.class);

    private final BusProperties props;
    private final BindingResolver resolver;
    private final BusChannels channels;
    private final BusBinderRegistry registry;
    private final BusMessageDispatcher dispatcher;

    @Override
    public void afterSingletonsInstantiated() {
        BinderSelector selector = new BinderSelector(props, registry);

        for (Map.Entry<String, BusProperties.BindingProperties> e : resolver.all().entrySet()) {
            String name = e.getKey();
            BusBinding binding = resolver.resolve(name);

            // 1) inbound/outbound(binding)
            SubscribableChannel outbound = channels.outbound(name);
            SubscribableChannel inbound = channels.inbound(name);

            // 2) inbound：先订阅 dispatcher
            inbound.unsubscribe(dispatcher);
            inbound.subscribe(dispatcher);

            // 3) inbound：补齐 binding header，避免下游 dispatcher 无法路由
            //    说明：这里使用 ChannelInterceptor（Spring Integration 官方扩展点），不造轮子
            if (inbound instanceof org.springframework.messaging.support.AbstractSubscribableChannel asc) {
                asc.addInterceptor(new BindingHeaderInterceptor(name));
            }

            // 4) outbound：为每个已注册 binder 建立专用 outbound 通道（包括 local）
            Map<String, SubscribableChannel> binderOutbound = new HashMap<>();
            for (String binderName : registry.all().keySet()) {
                String key = name + "::__outbound__::" + binderName.toLowerCase();
                binderOutbound.put(binderName.toLowerCase(), channels.outbound(key));
            }

            // 5) outbound：安装路由器（订阅 binding 的 outbound 通道）
            BusOutboundRouter router = new BusOutboundRouter(name, binding, selector, channels, binderOutbound);
            outbound.unsubscribe(router);
            outbound.subscribe(router);

            // 6) 对每个 binder 绑定 outbound（这样 per-send 才能切换）
            for (Map.Entry<String, BusBinder> be : registry.all().entrySet()) {
                String binderName = be.getKey();
                BusBinder binder = be.getValue();

                SubscribableChannel binderOutboundCh = binderOutbound.get(binderName.toLowerCase());
                BusBinding outboundBinding = binding.forBinder(binderName);

                // local binder 需要显式建立 outbound->inbound 的 JVM bridge
                if ("local".equalsIgnoreCase(binder.name())) {
                    LocalBusBinder.bridge(binderOutboundCh, inbound, name);
                } else {
                    binder.bindOutbound(outboundBinding, binderOutboundCh);
                }

                log.info("Outbound 绑定: binding='{}' -> binder='{}', destination='{}'",
                        name, binder.name(), outboundBinding.destination());
            }

            // 7) inbound：只选择一个 binder 绑定（避免重复消费）
            String inboundBinderName = selector.selectForInbound(binding);
            BusBinder inboundBinder = registry.get(inboundBinderName).orElseGet(() -> registry.get("local").orElseThrow());

            if (!"local".equalsIgnoreCase(inboundBinder.name()) && !"stream".equalsIgnoreCase(inboundBinder.name())) {
                inboundBinder.bindInbound(binding, inbound);
                log.info("Inbound 绑定: binding='{}' -> binder='{}', destination='{}', group='{}'",
                        name, inboundBinder.name(), binding.destination(), binding.group());
            } else {
                // local：无外部输入；stream：入站由用户函数式 Consumer 通过 BusStreamInboundAdapter 投递
                log.info("Inbound 跳过自动绑定: binding='{}' binder='{}'", name, inboundBinder.name());
            }
        }
    }

    /**
     * inbound 通道拦截器：确保消息包含 bus.binding header。
     */
    private static final class BindingHeaderInterceptor implements ChannelInterceptor {
        private final String bindingName;

        private BindingHeaderInterceptor(String bindingName) {
            this.bindingName = bindingName;
        }

        @Override
        public Message<?> preSend(Message<?> message, org.springframework.messaging.MessageChannel channel) {
            if (message.getHeaders().get(DefaultBusMessageProducer.HDR_BINDING) != null) {
                return message;
            }
            return MessageBuilder.fromMessage(message)
                    .setHeader(DefaultBusMessageProducer.HDR_BINDING, bindingName)
                    .build();
        }
    }
}