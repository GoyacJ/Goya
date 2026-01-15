package com.ysmjjsy.goya.component.cache.multilevel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 缓存清空事件
 *
 * <p>当整个缓存被清空时发布此事件。
 * redis 模块会订阅此事件并转发到 Redis Pub/Sub，通知其他节点清空 L1。
 *
 * @author goya
 * @since 2025/12/26 14:47
 */
@Getter
public class CacheClearEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -4177616094040684952L;
    
    /**
     * -- GETTER --
     *  获取缓存名称
     *
     */
    private final String cacheName;

    /**
     * 构造函数
     *
     * @param cacheName 缓存名称
     */
    public CacheClearEvent(String cacheName) {
        super(cacheName);
        this.cacheName = cacheName;
    }

    @Override
    public String toString() {
        return "CacheClearEvent{cacheName='" + cacheName + "'}";
    }
}