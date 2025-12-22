package com.ysmjjsy.goya.component.cache.listener;

import com.ysmjjsy.goya.component.cache.message.CacheInvalidateMessage;

/**
 * <p>缓存失效消息监听器接口</p>
 * <p>用于接收和处理来自其他节点的缓存失效消息</p>
 * <p>实现类应该：</p>
 * <ul>
 *     <li>订阅指定的消息主题或队列</li>
 *     <li>接收到消息后调用 {@link #onMessage(CacheInvalidateMessage)} 处理</li>
 *     <li>根据消息类型执行相应的缓存失效操作</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 * @see CacheInvalidateMessage
 */
public interface ICacheInvalidateListener {

    /**
     * 处理缓存失效消息
     *
     * @param message 失效消息
     */
    void onMessage(CacheInvalidateMessage message);

    /**
     * 启动监听器
     * <p>开始订阅消息主题或队列</p>
     */
    void start();

    /**
     * 停止监听器
     * <p>取消订阅并释放资源</p>
     */
    void stop();
}

