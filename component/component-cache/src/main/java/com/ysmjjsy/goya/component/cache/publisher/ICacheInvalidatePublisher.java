package com.ysmjjsy.goya.component.cache.publisher;

import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;

/**
 * <p>缓存失效消息发布器接口</p>
 * <p>用于向分布式系统中的其他节点发布缓存失效消息</p>
 * <p>实现方式：</p>
 * <ul>
 *     <li>Redis Pub/Sub：基于 Redis 的发布订阅机制</li>
 *     <li>MQ：基于消息队列（如 RabbitMQ、Kafka）</li>
 *     <li>自定义：基于其他通信机制</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 * @see CacheInvalidateMessage
 */
public interface ICacheInvalidatePublisher {

    /**
     * 发布缓存失效消息
     *
     * @param message 失效消息
     */
    void publish(CacheInvalidateMessage message);

    /**
     * 批量发布缓存失效消息
     *
     * @param messages 失效消息列表
     */
    default void publishBatch(Iterable<CacheInvalidateMessage> messages) {
        messages.forEach(this::publish);
    }
}

