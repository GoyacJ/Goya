package com.ysmjjsy.goya.component.framework.bus.message;

import org.springframework.messaging.Message;

/**
 * <p>监听器注册表：负责将 inbound 消息按 binding 分发给对应的业务监听器</p>
 *
 * @author goya
 * @since 2026/1/27 00:41
 */
public interface BusListenerRegistry {

    /**
     * 分发消息到指定 binding 的监听器。
     *
     * @param binding binding 名称（可能为 null）
     * @param message Spring Message（payload 通常是 MessageEnvelope）
     */
    void dispatch(String binding, Message<?> message);
}