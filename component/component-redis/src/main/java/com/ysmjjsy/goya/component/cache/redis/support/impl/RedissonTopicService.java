package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisTopicService;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>基于 Redisson {@link RTopic} 的发布订阅实现。</p>
 * <p>对象名：{@code topic:{topic}}</p>
 * <p>最终 name 会由 {@link RedisKeySupport} 拼接 tenant 前缀，实现租户隔离。</p>
 *
 * @author goya
 * @since 2026/1/25 23:40
 */
public class RedissonTopicService implements RedisTopicService {

    /**
     * Redis 命名空间：topic。
     */
    private static final String NS = "topic";

    private final RedissonClient redisson;
    private final RedisKeySupport keys;

    /**
     * 构造函数。
     *
     * @param redisson RedissonClient
     * @param keys     Redis key 支持
     */
    public RedissonTopicService(RedissonClient redisson, RedisKeySupport keys) {
        this.redisson = Objects.requireNonNull(redisson, "redisson 不能为空");
        this.keys = Objects.requireNonNull(keys, "keys 不能为空");
    }

    @Override
    public void publish(String topic, Object message) {
        Objects.requireNonNull(message, "message 不能为空");
        RTopic t = redisson.getTopic(keys.name(NS, topic));
        t.publish(message);
    }

    @Override
    public int subscribe(String topic, Consumer<Object> consumer) {
        Objects.requireNonNull(consumer, "consumer 不能为空");
        RTopic t = redisson.getTopic(keys.name(NS, topic));
        return t.addListener(Object.class, (channel, msg) -> consumer.accept(msg));
    }

    @Override
    public <T> int subscribe(String topic, Class<T> type, Consumer<T> consumer) {
        Objects.requireNonNull(type, "type 不能为空");
        Objects.requireNonNull(consumer, "consumer 不能为空");
        RTopic t = redisson.getTopic(keys.name(NS, topic));
        return t.addListener(type, (channel, msg) -> consumer.accept(msg));
    }

    @Override
    public void unsubscribe(String topic, int listenerId) {
        RTopic t = redisson.getTopic(keys.name(NS, topic));
        t.removeListener(listenerId);
    }
}