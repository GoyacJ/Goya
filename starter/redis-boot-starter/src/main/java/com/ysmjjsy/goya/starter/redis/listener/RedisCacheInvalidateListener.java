package com.ysmjjsy.goya.starter.redis.listener;

import com.ysmjjsy.goya.component.cache.listener.ICacheInvalidateListener;
import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;
import com.ysmjjsy.goya.component.cache.model.CacheValue;
import com.ysmjjsy.goya.component.cache.service.LocalCacheService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * <p>基于 Redis Pub/Sub 的缓存失效消息监听器</p>
 * <p>通过 Redisson 的 RTopic 监听消息，并处理 L1 缓存失效</p>
 * <p>特点：</p>
 * <ul>
 *     <li>自动订阅 Redis 主题</li>
 *     <li>接收消息后立即处理 L1 缓存</li>
 *     <li>过滤自己发送的消息（通过 nodeId）</li>
 *     <li>支持优雅停机</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 * @see ICacheInvalidateListener
 * @see RedissonClient
 */
@Slf4j
public class RedisCacheInvalidateListener implements ICacheInvalidateListener {

    private final RedissonClient redissonClient;
    private final String topicName;
    private final LocalCacheService l1Cache;
    private final String localNodeId;
    
    private RTopic topic;
    private Integer listenerId;
    private volatile boolean started = false;

    public RedisCacheInvalidateListener(
            RedissonClient redissonClient,
            String topicName,
            LocalCacheService l1Cache,
            String localNodeId) {
        this.redissonClient = redissonClient;
        this.topicName = topicName;
        this.l1Cache = l1Cache;
        this.localNodeId = localNodeId;
    }

    @Override
    public void onMessage(CacheInvalidateMessage message) {
        if (message == null) {
            return;
        }

        // 忽略自己发送的消息
        if (localNodeId.equals(message.nodeId())) {
            log.trace("[Goya] |- starter [redis] |- ignore self message, nodeId: {}", localNodeId);
            return;
        }

        try {
            switch (message.type()) {
                case KEY -> {
                    if (message.cacheName() != null && message.key() != null) {
                        if (message.version() != null) {
                            // 带版本号（更新操作）：比较版本，只有更新时才失效
                            CacheValue<?> localValue = l1Cache.get(message.cacheName(), message.key());
                            
                            if (localValue == null || message.version() > localValue.version()) {
                                l1Cache.remove(message.cacheName(), message.key());
                                log.trace("[Goya] |- starter [redis] |- L1 invalidated key [{}] in cache [{}], " +
                                                "message version: {}, local version: {}",
                                        message.key(), message.cacheName(), message.version(),
                                        localValue != null ? localValue.version() : "null");
                            } else {
                                log.trace("[Goya] |- starter [redis] |- Skip invalidate key [{}] in cache [{}], " +
                                                "message version {} <= local version {}",
                                        message.key(), message.cacheName(), message.version(), localValue.version());
                            }
                        } else {
                            // 无版本号（删除操作）：直接失效
                            l1Cache.remove(message.cacheName(), message.key());
                            log.trace("[Goya] |- starter [redis] |- L1 invalidated key [{}] in cache [{}] (delete operation)",
                                    message.key(), message.cacheName());
                        }
                    }
                }
                case CACHE -> {
                    if (message.cacheName() != null) {
                        l1Cache.clear(message.cacheName());
                        log.trace("[Goya] |- starter [redis] |- L1 cache [{}] cleared", message.cacheName());
                    }
                }
                case ALL -> {
                    l1Cache.clearAll();
                    log.trace("[Goya] |- starter [redis] |- all L1 caches cleared");
                }
            }
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- failed to process invalidate message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void start() {
        if (started) {
            log.warn("[Goya] |- starter [redis] |- invalidate listener already started");
            return;
        }

        try {
            topic = redissonClient.getTopic(topicName);
            listenerId = topic.addListener(CacheInvalidateMessage.class,
                    (_, msg) -> onMessage(msg));
            
            started = true;
            log.info("[Goya] |- starter [redis] |- invalidate listener started, " +
                    "topic: {}, nodeId: {}, listenerId: {}", topicName, localNodeId, listenerId);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- failed to start invalidate listener: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        if (!started) {
            return;
        }

        try {
            if (topic != null && listenerId != null) {
                topic.removeListener(listenerId);
                log.info("[Goya] |- starter [redis] |- invalidate listener stopped, " +
                        "topic: {}, listenerId: {}", topicName, listenerId);
            }
            started = false;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- failed to stop invalidate listener: {}", e.getMessage(), e);
        }
    }
}

