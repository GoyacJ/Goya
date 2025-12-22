package com.ysmjjsy.goya.starter.redis.publisher;

import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * <p>基于 Redis Pub/Sub 的缓存失效消息发布器</p>
 * <p>通过 Redisson 的 RTopic 实现消息发布</p>
 * <p>特点：</p>
 * <ul>
 *     <li>基于 Redis 发布订阅机制</li>
 *     <li>支持跨节点消息广播</li>
 *     <li>自动序列化消息（使用 Redisson 配置的编解码器）</li>
 *     <li>异步非阻塞发布</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 * @see ICacheInvalidatePublisher
 * @see RedissonClient
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheInvalidatePublisher implements ICacheInvalidatePublisher {

    private final RedissonClient redissonClient;
    private final String topicName;

    /**
     * 获取 Redis Topic
     *
     * @return RTopic 实例
     */
    private RTopic getTopic() {
        return redissonClient.getTopic(topicName);
    }

    @Override
    public void publish(CacheInvalidateMessage message) {
        if (message == null) {
            log.warn("[Goya] |- starter [redis] |- publish invalidate message failed: message is null");
            return;
        }

        try {
            RTopic topic = getTopic();
            long receivers = topic.publish(message);
            log.trace("[Goya] |- starter [redis] |- published invalidate message to topic [{}], " +
                            "type: {}, cacheName: {}, key: {}, receivers: {}",
                    topicName, message.type(), message.cacheName(), message.key(), receivers);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- failed to publish invalidate message to topic [{}]: {}",
                    topicName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void publishBatch(Iterable<CacheInvalidateMessage> messages) {
        if (messages == null) {
            return;
        }

        RTopic topic = getTopic();
        int count = 0;

        for (CacheInvalidateMessage message : messages) {
            try {
                topic.publish(message);
                count++;
            } catch (Exception e) {
                log.error("[Goya] |- starter [redis] |- failed to publish batch message: {}", e.getMessage());
            }
        }

        log.debug("[Goya] |- starter [redis] |- published batch invalidate messages, count: {}", count);
    }
}

