package com.ysmjjsy.goya.component.framework.bus.runtime;

import org.springframework.messaging.SubscribableChannel;

/**
 * <p>Bus 运行期通道集合</p>
 * <p>
 * 设计目的：
 * - 统一管理 Integration 骨架通道（出站、入站、错误）
 * - 让 binder / producer / dispatcher 都只依赖这一层抽象，不互相耦合
 *
 * @author goya
 * @since 2026/1/26 23:51
 */
public interface BusChannels {

    /**
     * 获取某个 binding 的出站通道。
     * 发送端会把消息投递到该通道，由 binder 输出到 MQ/本地。
     */
    SubscribableChannel outbound(String bindingName);

    /**
     * 获取某个 binding 的入站通道。
     * binder 从 MQ/本地接收到消息后投递到该通道，再由 dispatcher 分发给业务监听器。
     */
    SubscribableChannel inbound(String bindingName);

    /**
     * 全局错误通道：所有异常统一发布到这里。
     */
    SubscribableChannel error();
}