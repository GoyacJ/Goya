package com.ysmjjsy.goya.starter.redis.subscriber;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.event.CacheClearEvent;
import com.ysmjjsy.goya.component.cache.event.CacheEvictionEvent;
import com.ysmjjsy.goya.component.cache.event.LocalCacheEvictionEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.io.Serial;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存失效订阅器
 *
 * <p>订阅 Spring 事件（CacheEvictionEvent、CacheClearEvent），转发到 Redis Pub/Sub。
 * 同时订阅 Redis Pub/Sub，接收其他节点的失效消息，通知本地 L1 失效。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>订阅 Spring 事件并转发到 Redis Pub/Sub</li>
 *   <li>订阅 Redis Pub/Sub，接收其他节点的失效消息</li>
 *   <li>发布本地失效事件（通知本地 L1 失效）</li>
 *   <li>延迟检查机制（确保 L2 已失效）</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>使用 @EventListener 订阅 Spring 事件</li>
 *   <li>使用 ApplicationEventPublisher 发布本地失效事件</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li><b>订阅 Spring 事件：</b>
 *     <ol>
 *       <li>收到 CacheEvictionEvent 或 CacheClearEvent</li>
 *       <li>转发到 Redis Pub/Sub</li>
 *       <li>延迟检查 L2 是否已失效（兜底机制）</li>
 *     </ol>
 *   </li>
 *   <li><b>订阅 Redis Pub/Sub：</b>
 *     <ol>
 *       <li>收到其他节点的失效消息</li>
 *       <li>发布本地失效事件（LocalCacheEvictionEvent）</li>
 *       <li>GoyaCache 订阅此事件并失效本地 L1</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>事件监听在 Spring 事件线程中执行</li>
 *   <li>Redis Pub/Sub 监听在 Redisson 的线程中执行</li>
 *   <li>延迟检查使用 ScheduledExecutorService</li>
 * </ul>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>如果 Redis Pub/Sub 发布失败，记录错误日志但不抛出异常</li>
 *   <li>如果 Redis Pub/Sub 订阅失败，记录错误日志</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:58
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheEvictionSubscriber {

    /**
     * Redis Pub/Sub 频道名称
     *
     * <p>使用固定频道，所有失效消息都发送到这个频道。
     * Redisson 不支持通配符订阅，因此使用统一频道。
     */
    private static final String EVICTION_CHANNEL = "goya:cache:evict:all";

    /**
     * 延迟检查时间（毫秒）
     */
    private static final long EVICTION_CHECK_DELAY_MS = 100;

    /**
     * Redisson 客户端
     */
    private final RedissonClient redisson;

    /**
     * Spring 事件发布器（用于发布本地失效事件）
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * GoyaCacheManager（用于获取 GoyaCache 实例进行 L2 失效检查）
     */
    private final GoyaCacheManager cacheManager;

    /**
     * Redis Pub/Sub Topic
     */
    private RTopic topic;

    /**
     * 消息监听器 ID（用于取消订阅）
     */
    private Integer listenerId;

    /**
     * 延迟检查调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "goya-cache-eviction-check");
        t.setDaemon(true);
        return t;
    });

    /**
     * 初始化：订阅 Redis Pub/Sub
     *
     * <p>订阅固定频道，接收所有节点的缓存失效消息。
     * Redisson 不支持通配符订阅，因此使用统一频道 {@code goya:cache:evict:all}。
     */
    @PostConstruct
    public void subscribe() {
        try {
            // 订阅固定频道（所有失效消息都发送到这个频道）
            topic = redisson.getTopic(EVICTION_CHANNEL);

            // 订阅消息
            listenerId = topic.addListener(EvictionMessage.class, (channel, message) -> handleRemoteEviction(message));

            log.info("Subscribed to Redis Pub/Sub for cache eviction: channel={}", EVICTION_CHANNEL);
        } catch (Exception e) {
            log.error("Failed to subscribe to Redis Pub/Sub: channel={}", EVICTION_CHANNEL, e);
        }
    }

    /**
     * 销毁：取消订阅
     */
    @PreDestroy
    public void unsubscribe() {
        try {
            if (listenerId != null && topic != null) {
                topic.removeListener(listenerId);
                log.info("Unsubscribed from Redis Pub/Sub");
            }
            scheduler.shutdown();
        } catch (Exception e) {
            log.error("Failed to unsubscribe from Redis Pub/Sub", e);
        }
    }

    /**
     * 订阅 Spring 事件：缓存失效
     *
     * @param event 缓存失效事件
     */
    @EventListener
    public void onCacheEviction(CacheEvictionEvent event) {
        try {
            // 转发到 Redis Pub/Sub（使用统一频道）
            EvictionMessage message = new EvictionMessage(event.getCacheName(), event.getKey());

            RTopic evictionTopic = redisson.getTopic(EVICTION_CHANNEL);
            evictionTopic.publish(message);

            if (log.isTraceEnabled()) {
                log.trace("Published eviction message to Redis: cacheName={}, key={}, channel={}",
                        event.getCacheName(), event.getKey(), EVICTION_CHANNEL);
            }

            // 延迟检查机制：确保 L2 已失效
            scheduleEvictionCheck(event.getCacheName(), event.getKey());
        } catch (Exception e) {
            log.error("Failed to publish eviction message to Redis: cacheName={}, key={}, channel={}",
                    event.getCacheName(), event.getKey(), EVICTION_CHANNEL, e);
        }
    }

    /**
     * 订阅 Spring 事件：缓存清空
     *
     * @param event 缓存清空事件
     */
    @EventListener
    public void onCacheClear(CacheClearEvent event) {
        try {
            // 转发到 Redis Pub/Sub（使用统一频道）
            ClearMessage message = new ClearMessage(event.getCacheName());

            RTopic clearTopic = redisson.getTopic(EVICTION_CHANNEL);
            clearTopic.publish(message);

            log.info("Published clear message to Redis: cacheName={}, channel={}",
                    event.getCacheName(), EVICTION_CHANNEL);
        } catch (Exception e) {
            log.error("Failed to publish clear message to Redis: cacheName={}, channel={}",
                    event.getCacheName(), EVICTION_CHANNEL, e);
        }
    }

    /**
     * 处理远程失效消息
     *
     * <p>收到其他节点的失效消息后，发布本地失效事件，通知本地 L1 缓存失效。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>创建 LocalCacheEvictionEvent 事件</li>
     *   <li>发布事件到 Spring 事件机制</li>
     *   <li>GoyaCache 订阅事件并失效本地 L1</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果事件发布失败，记录错误日志但不抛出异常</li>
     * </ul>
     *
     * @param message 失效消息
     */
    private void handleRemoteEviction(EvictionMessage message) {
        try {
            // 发布本地失效事件（通知本地 L1 失效）
            LocalCacheEvictionEvent event = new LocalCacheEvictionEvent(
                    this, message.getCacheName(), message.getKey());
            eventPublisher.publishEvent(event);

            if (log.isTraceEnabled()) {
                log.trace("Published local cache eviction event: cacheName={}, key={}",
                        message.getCacheName(), message.getKey());
            }
        } catch (Exception e) {
            log.error("Failed to handle remote eviction message: cacheName={}, key={}",
                    message.getCacheName(), message.getKey(), e);
        }
    }

    /**
     * 延迟检查 L2 是否已失效
     *
     * <p>发布失效消息后，延迟检查 L2 是否已失效。
     * 如果未失效，主动失效（兜底机制）。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>延迟 EVICTION_CHECK_DELAY_MS 毫秒后执行检查</li>
     *   <li>从 GoyaCacheManager 获取 GoyaCache 实例</li>
     *   <li>调用 GoyaCache.checkAndEvictL2IfPresent() 检查并失效</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 cacheManager 为 null，跳过检查</li>
     *   <li>如果检查失败，记录警告日志但不抛出异常</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    private void scheduleEvictionCheck(String cacheName, Object key) {
        // 如果 cacheManager 为 null，跳过检查
        if (cacheManager == null) {
            if (log.isTraceEnabled()) {
                log.trace("Skipping L2 eviction check: cacheManager is null, cacheName={}, key={}", cacheName, key);
            }
            return;
        }

        scheduler.schedule(() -> {
            try {
                // 从 GoyaCacheManager 获取 GoyaCache 实例
                GoyaCache cache = (GoyaCache) cacheManager.getCache(cacheName);
                if (cache == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Cache not found for eviction check: cacheName={}, key={}", cacheName, key);
                    }
                    return;
                }

                // 检查并失效 L2（如果存在）
                boolean evicted = cache.checkAndEvictL2IfPresent(key);
                if (evicted) {
                    if (log.isDebugEnabled()) {
                        log.debug("L2 cache evicted during delayed check: cacheName={}, key={}", cacheName, key);
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("L2 cache already evicted or not present: cacheName={}, key={}", cacheName, key);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to check L2 eviction: cacheName={}, key={}", cacheName, key, e);
            }
        }, EVICTION_CHECK_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 失效消息
     */
    @Setter
    @Getter
    public static class EvictionMessage implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String cacheName;
        private Object key;

        public EvictionMessage() {
        }

        public EvictionMessage(String cacheName, Object key) {
            this.cacheName = cacheName;
            this.key = key;
        }

    }

    /**
     * 清空消息
     */
    @Setter
    @Getter
    public static class ClearMessage implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String cacheName;

        public ClearMessage() {
        }

        public ClearMessage(String cacheName) {
            this.cacheName = cacheName;
        }

    }
}