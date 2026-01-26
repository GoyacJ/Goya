package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.support.RedisReliableDelayedQueueService;
import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import lombok.RequiredArgsConstructor;
import org.redisson.api.Message;
import org.redisson.api.MessageArgs;
import org.redisson.api.RReliableQueue;
import org.redisson.api.RedissonClient;
import org.redisson.api.queue.*;
import org.redisson.client.codec.Codec;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 23:58
 */
@RequiredArgsConstructor
public class RedissonReliableDelayedQueueService implements RedisReliableDelayedQueueService {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 统一 Codec
     */
    private final Codec codec;


    @Override
    public String offer(String queueName, Object payload, Duration delay) {
        Objects.requireNonNull(queueName, "queueName 不能为空");

        try {
            RReliableQueue<Object> rq = redissonClient.getReliableQueue(queueName, codec);

            MessageArgs<Object> ma = MessageArgs.payload(payload);
            if (delay != null && !delay.isZero() && !delay.isNegative()) {
                ma = ma.delay(delay);
            }

            Message<Object> msg = rq.add(QueueAddArgs.messages(ma));
            if (msg == null) {
                throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("投递消息失败（可能队列已满且超时）：queueName=" + queueName).build();
            }
            return msg.getId();
        } catch (Exception e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).userMessage("投递延迟消息失败：queueName=" + queueName).build();
        }
    }

    @Override
    public Optional<RedisReliableDelayedQueueService.ReliableQueueMessage> poll(String queueName, Duration visibility, Duration timeout) {
        Objects.requireNonNull(queueName, "queueName 不能为空");

        try {
            RReliableQueue<Object> rq = redissonClient.getReliableQueue(queueName, codec);

            QueuePollArgs args = QueuePollArgs.defaults()
                    .acknowledgeMode(AcknowledgeMode.MANUAL);

            if (visibility != null && !visibility.isZero() && !visibility.isNegative()) {
                args = args.visibility(visibility);
            }
            if (timeout != null && !timeout.isNegative()) {
                args = args.timeout(timeout);
            }

            Message<Object> msg = rq.poll(args);
            if (msg == null) {
                return Optional.empty();
            }

            return Optional.of(new DefaultReliableQueueMessage(rq, msg));
        } catch (Exception e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).userMessage("拉取消息失败：queueName=" + queueName).build();
        }
    }

    /**
     * 可靠消息句柄实现
     *
     * @param rq  队列实例
     * @param msg 消息
     */
    private record DefaultReliableQueueMessage(RReliableQueue<Object> rq,
                                               Message<Object> msg) implements ReliableQueueMessage {

        @Override
        public String id() {
            return msg.getId();
        }

        @Override
        public Object payload() {
            return msg.getPayload();
        }

        @Override
        public Map<String, Object> headers() {
            Map<String, Object> headers = msg.getHeaders();
            return headers == null ? Collections.emptyMap() : headers;
        }

        @Override
        public void ack() {
            rq.acknowledge(QueueAckArgs.ids(msg.getId()));
        }

        @Override
        public void nackFailed(Duration delay) {
            QueueNegativeAckArgs args = QueueNegativeAckArgs.failed(msg.getId());
            if (delay != null && !delay.isZero() && !delay.isNegative()) {
                args = args.syncTimeout(delay);
            }
            rq.negativeAcknowledge(args);
        }

        @Override
        public void nackRejected() {
            rq.negativeAcknowledge(QueueNegativeAckArgs.rejected(msg.getId()));
        }
    }
}