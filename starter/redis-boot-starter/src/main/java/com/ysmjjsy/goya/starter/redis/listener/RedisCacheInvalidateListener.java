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
            log.trace("[Goya] |- starter [redis] |- ignore self invalidate message, nodeId={}", localNodeId);
            return;
        }

        try {
            switch (message.type()) {
                case KEY -> handleKeyInvalidate(message);
                case CACHE -> handleCacheInvalidate(message);
                case ALL -> handleAllInvalidate();
                default -> log.warn("[Goya] |- starter [redis] |- unknown invalidate message type: {}", message.type());
            }
        } catch (Exception ex) {
            log.error("[Goya] |- starter [redis] |- failed to process invalidate message: {}", message, ex);
        }
    }

    private void handleKeyInvalidate(CacheInvalidateMessage message) {
        String cacheName = message.cacheName();
        Object key = message.key();

        if (cacheName == null || key == null) {
            log.debug("[Goya] |- starter [redis] |- skip KEY invalidate, cacheName or key is null: {}", message);
            return;
        }

        Long incomingVersion = message.version();
        CacheValue<?> localValue = l1Cache.get(cacheName, key);

        // 无版本号：直接删除（通常是 delete）
        if (incomingVersion == null) {
            l1Cache.remove(cacheName, key);
            log.trace("[Goya] |- starter [redis] |- L1 invalidated key [{}] in cache [{}] (no version)",
                    key, cacheName);
            return;
        }

        // 本地无缓存 or 远端版本更新
        if (localValue == null || incomingVersion > localValue.version()) {
            l1Cache.remove(cacheName, key);
            log.trace("[Goya] |- starter [redis] |- L1 invalidated key [{}] in cache [{}], " +
                            "incomingVersion={}, localVersion={}",
                    key, cacheName, incomingVersion,
                    localValue != null ? localValue.version() : "null");
        } else {
            log.trace("[Goya] |- starter [redis] |- skip invalidate key [{}] in cache [{}], " +
                            "incomingVersion={} <= localVersion={}",
                    key, cacheName, incomingVersion, localValue.version());
        }
    }

    private void handleCacheInvalidate(CacheInvalidateMessage message) {
        String cacheName = message.cacheName();
        if (cacheName == null) {
            log.debug("[Goya] |- starter [redis] |- skip CACHE invalidate, cacheName is null: {}", message);
            return;
        }

        l1Cache.clear(cacheName);
        log.trace("[Goya] |- starter [redis] |- L1 cache [{}] cleared", cacheName);
    }

    private void handleAllInvalidate() {
        l1Cache.clearAll();
        log.trace("[Goya] |- starter [redis] |- all L1 caches cleared");
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

