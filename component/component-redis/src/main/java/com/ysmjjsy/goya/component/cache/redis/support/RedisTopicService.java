package com.ysmjjsy.goya.component.cache.redis.support;

import java.util.function.Consumer;

/**
 * <p>Redis 发布订阅服务</p>
 *
 * @author goya
 * @since 2026/1/25 23:37
 */
public interface RedisTopicService {

    /**
     * 发布消息。
     *
     * @param topic   主题名（业务语义名，例如 "cache-invalidate"）
     * @param message 消息对象（需可被全局 Codec 序列化）
     */
    void publish(String topic, Object message);

    /**
     * 订阅消息（以 Object 接收）。
     *
     * <p>全局 TypedJsonMapperCodec 会根据 typeId 尝试恢复真实类型；
     * 如果恢复失败会退化为 Object/Map 等结构，业务自行处理。</p>
     *
     * @param topic    主题名
     * @param consumer 消费者
     * @return listenerId（用于取消订阅）
     */
    int subscribe(String topic, Consumer<Object> consumer);

    /**
     * 订阅消息（强类型接收）。
     *
     * <p>当你明确消息类型时使用，减少 instanceof/转换逻辑。</p>
     *
     * @param topic    主题名
     * @param type     目标类型
     * @param consumer 消费者
     * @param <T>      类型
     * @return listenerId
     */
    <T> int subscribe(String topic, Class<T> type, Consumer<T> consumer);

    /**
     * 取消订阅。
     *
     * @param topic      主题名
     * @param listenerId 订阅 ID
     */
    void unsubscribe(String topic, int listenerId);
}