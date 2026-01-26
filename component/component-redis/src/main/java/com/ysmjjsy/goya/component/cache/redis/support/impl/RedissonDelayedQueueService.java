package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisDelayedQueueService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RReliableQueue;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>基于 Redisson 的延迟队列实现</p>
 * <p>对象名：{@code dq:{queue}}</p>
 * <p>内部使用：</p>
 * <ul>
 *   <li>{@link RBlockingQueue} 作为“真实队列”供消费者拉取</li>
 *   <li>{@link RDelayedQueue} 承担延迟投递，到期后自动转移到真实队列</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 23:41
 */
public class RedissonDelayedQueueService implements RedisDelayedQueueService {

    /**
     * Redis 命名空间：dq（delayed queue）。
     */
    private static final String NS = "dq";

    private final RedissonClient redisson;
    private final RedisKeySupport keys;

    /**
     * 构造函数。
     *
     * @param redisson RedissonClient
     * @param keys     key 支持
     */
    public RedissonDelayedQueueService(RedissonClient redisson, RedisKeySupport keys) {
        this.redisson = Objects.requireNonNull(redisson, "redisson 不能为空");
        this.keys = Objects.requireNonNull(keys, "keys 不能为空");
    }

    @Override
    public void enqueue(String queue, Object message, Duration delay) {
        Objects.requireNonNull(message, "message 不能为空");
        String qName = keys.name(NS, queue);

        RBlockingQueue<Object> blockingQueue = redisson.getBlockingQueue(qName);
        RDelayedQueue<Object> delayedQueue = redisson.getDelayedQueue(blockingQueue);

        RReliableQueue rReliableQueue = redisson.getReliableQueue(qName);

        long ms = delay == null ? 0L : delay.toMillis();
        if (ms <= 0L) {
            // 立即投递：直接进入真实队列
            blockingQueue.add(message);
            return;
        }
        delayedQueue.offer(message, ms, TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<Object> poll(String queue) {
        String qName = keys.name(NS, queue);
        RBlockingQueue<Object> blockingQueue = redisson.getBlockingQueue(qName);
        return Optional.ofNullable(blockingQueue.poll());
    }

    @Override
    public Optional<Object> take(String queue, Duration timeout) {
        String qName = keys.name(NS, queue);
        RBlockingQueue<Object> blockingQueue = redisson.getBlockingQueue(qName);

        long ms = timeout == null ? 0L : timeout.toMillis();
        if (ms <= 0L) {
            return Optional.ofNullable(blockingQueue.poll());
        }
        try {
            return Optional.ofNullable(blockingQueue.poll(ms, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @Override
    public void destroy(String queue) {
        String qName = keys.name(NS, queue);
        RBlockingQueue<Object> blockingQueue = redisson.getBlockingQueue(qName);

        // destroy 释放延迟队列调度资源；真实队列的数据是否保留由业务决定
        RDelayedQueue<Object> delayedQueue = redisson.getDelayedQueue(blockingQueue);
        delayedQueue.destroy();
    }
}