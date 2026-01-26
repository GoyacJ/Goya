package com.ysmjjsy.goya.component.bus.stream.definition;

/**
 * <p>事件作用域枚举</p>
 * <p>用于控制事件的发布和订阅范围</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public enum EventScope {

    /**
     * 本地事件：仅在当前 JVM 内发布和订阅
     * <p>基于 Spring ApplicationEventPublisher 实现</p>
     */
    LOCAL,

    /**
     * 远程事件：跨服务发布和订阅
     * <p>基于 Spring Cloud Stream 实现，需要引入对应的 starter（如 kafka-boot-starter）</p>
     */
    REMOTE,

    /**
     * 全部：同时发布本地和远程事件
     * <p>先发布本地事件，再发布远程事件</p>
     */
    ALL
}

