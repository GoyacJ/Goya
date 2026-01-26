package com.ysmjjsy.goya.component.cache.redis.support;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Redis 延迟队列服务（RReliableQueue）</p>
 * <p>基于 Redisson PRO 的 {@code RReliableQueue} 实现：
 * <ul>
 *   <li>入队支持 delay（延迟投递）</li>
 *   <li>出队支持可见性超时 visibility</li>
 *   <li>支持 ACK / NACK（失败重投、拒绝丢弃/入 DLQ）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 23:58
 */
public interface RedisReliableDelayedQueueService {

    /**
     * 投递延迟消息
     *
     * @param queueName 队列名
     * @param payload   消息体
     * @param delay     延迟时间（null/0 表示不延迟）
     * @return messageId（队列生成的唯一 ID）
     */
    String offer(String queueName, Object payload, Duration delay);

    /**
     * 拉取一条消息（默认 MANUAL ack）
     *
     * @param queueName  队列名
     * @param visibility 可见性超时（null 表示使用队列默认值）
     * @param timeout    拉取等待时间（null 表示短轮询）
     * @return 可 ACK/NACK 的消息句柄
     */
    Optional<ReliableQueueMessage> poll(String queueName, Duration visibility, Duration timeout);

    /**
     * 可靠队列消息句柄
     *
     * <p>提供 payload + headers + ack/nack 能力。
     */
    interface ReliableQueueMessage {

        /**
         * 消息 ID
         *
         * @return messageId
         */
        String id();

        /**
         * 消息体（已反序列化）
         *
         * @return payload
         */
        Object payload();

        /**
         * 消息 headers（可能为空）
         *
         * @return headers
         */
        Map<String, Object> headers();

        /**
         * 确认消费成功（ACK）
         */
        void ack();

        /**
         * 标记失败并可选延迟重投（NACK-FAILED）
         *
         * @param delay 重投延迟（null/0 表示立即重投）
         */
        void nackFailed(Duration delay);

        /**
         * 标记拒绝（NACK-REJECTED）
         *
         * <p>拒绝语义一般表示“不可重试”，会被删除或进入 DLQ（如果配置了 DLQ）。
         */
        void nackRejected();
    }
}
