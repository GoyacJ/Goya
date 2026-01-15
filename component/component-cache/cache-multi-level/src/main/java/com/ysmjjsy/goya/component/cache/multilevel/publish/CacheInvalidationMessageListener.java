package com.ysmjjsy.goya.component.cache.multilevel.publish;

/**
 * <p>缓存失效消息监听器</p>
 * <p>用于处理接收到的缓存失效消息</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * CacheInvalidationMessageListener listener = message -> {
 *     // 处理失效消息
 *     localCache.delete(message.key());
 * };
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15
 */
@FunctionalInterface
public interface CacheInvalidationMessageListener {

    /**
     * 处理失效消息
     *
     * @param message 失效消息
     */
    void onMessage(CacheInvalidationMessage message);
}
