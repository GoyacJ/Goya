package com.ysmjjsy.goya.component.cache.multilevel.publish;

/**
 * <p>缓存失效消息订阅器</p>
 * <p>用于订阅缓存失效消息，接收失效通知</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * CacheInvalidationSubscriber subscriber = new RedisCacheInvalidationSubscriber(...);
 * subscriber.subscribe(message -> {
 *     // 处理失效消息
 * });
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15
 */
public interface CacheInvalidationSubscriber {

    /**
     * 订阅失效消息
     *
     * @param listener 消息监听器
     * @throws IllegalStateException 如果已经订阅
     */
    void subscribe(CacheInvalidationMessageListener listener);

    /**
     * 取消订阅
     */
    void unsubscribe();
}
