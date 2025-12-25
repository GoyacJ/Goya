package com.ysmjjsy.goya.component.bus.service;

import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;

import java.time.Duration;

/**
 * <p>事件总线服务接口</p>
 * <p>提供统一的事件发布 API，支持本地、远程和全部发布</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class OrderService {
 *     private final IBusService busService;
 *
 *     public void createOrder(Order order) {
 *         // 发布本地事件
 *         busService.publishLocal(new OrderCreatedEvent(order.getId()));
 *
 *         // 发布远程事件
 *         busService.publishRemote(new OrderCreatedEvent(order.getId()));
 *
 *         // 发布到本地和远程
 *         busService.publishAll(new OrderCreatedEvent(order.getId()));
 *
 *         // 延迟发布
 *         busService.publishDelayed(new OrderCreatedEvent(order.getId()), Duration.ofSeconds(10));
 *
 *         // 有序发布（通过分区键保证顺序）
 *         busService.publishOrdered(new OrderCreatedEvent(order.getId()), order.getUserId());
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
public interface IBusService {

    /**
     * 发布本地事件
     * <p>仅在当前 JVM 内发布，基于 Spring ApplicationEventPublisher 实现</p>
     *
     * @param event 事件对象
     * @param <E>   事件类型
     */
    <E extends IEvent> void publishLocal(E event);

    /**
     * 发布远程事件
     * <p>跨服务发布，基于 Spring Cloud Stream 实现</p>
     * <p>需要引入对应的 starter（如 kafka-boot-starter）</p>
     *
     * @param event 事件对象
     * @param <E>   事件类型
     * @throws IllegalStateException 如果未引入对应的 starter
     */
    <E extends IEvent> void publishRemote(E event);

    /**
     * 发布到本地和远程
     * <p>先发布本地事件，再发布远程事件</p>
     *
     * @param event 事件对象
     * @param <E>   事件类型
     */
    <E extends IEvent> void publishAll(E event);

    /**
     * 延迟发布事件
     * <p>通过 Header 传递延迟时间，某些 binder（如 RabbitMQ）原生支持延迟消息</p>
     *
     * @param event 事件对象
     * @param delay 延迟时间
     * @param <E>   事件类型
     */
    <E extends IEvent> void publishDelayed(E event, Duration delay);

    /**
     * 有序发布事件
     * <p>通过分区键保证同一分区键的消息有序处理</p>
     * <p>分区键会被设置到 Header 中，通过 Spring Cloud Stream 的 partition-key-expression 使用</p>
     *
     * @param event       事件对象
     * @param partitionKey 分区键
     * @param <E>         事件类型
     */
    <E extends IEvent> void publishOrdered(E event, String partitionKey);
}

