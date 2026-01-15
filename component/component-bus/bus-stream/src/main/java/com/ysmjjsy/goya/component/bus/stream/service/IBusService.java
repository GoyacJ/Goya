package com.ysmjjsy.goya.component.bus.stream.service;

import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;

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
    <E extends IBusEvent> void publishLocal(E event);

    /**
     * 发布远程事件
     * <p>跨服务发布，基于 Spring Cloud Stream 实现</p>
     * <p>需要引入对应的 starter（如 kafka-boot-starter）</p>
     *
     * @param event 事件对象
     * @param <E>   事件类型
     * @throws IllegalStateException 如果未引入对应的 starter
     */
    <E extends IBusEvent> void publishRemote(E event);

    /**
     * 延迟发布事件
     * <p>通过 Header 传递延迟时间，某些 binder（如 RabbitMQ）原生支持延迟消息</p>
     *
     * @param event 事件对象
     * @param delay 延迟时间
     * @param <E>   事件类型
     */
    <E extends IBusEvent> void publishDelayed(E event, Duration delay);

    /**
     * 有序发布事件
     * <p>通过分区键保证同一分区键的消息有序处理</p>
     * <p>分区键会被设置到 Header 中，通过 Spring Cloud Stream 的 partition-key-expression 使用</p>
     *
     * @param event       事件对象
     * @param partitionKey 分区键
     * @param <E>         事件类型
     */
    <E extends IBusEvent> void publishOrdered(E event, String partitionKey);

    /**
     * 在事务内发布事件（事务性发件箱模式）
     * <p>先执行事务回调，然后在事务提交后发布远程事件</p>
     * <p>本地事件在事务内同步执行，远程事件在事务提交后异步执行</p>
     * <p>使用示例：</p>
     * <pre>{@code
     * @Transactional
     * public void createOrder(Order order) {
     *     // 保存订单
     *     orderRepository.save(order);
     *     
     *     // 在事务内发布事件（事务性发件箱模式）
     *     busService.publishInTransaction(
     *         new OrderCreatedEvent(order.getId()),
     *         () -> {
     *             // 事务内的其他操作
     *             // 如果事务回滚，远程事件不会发布
     *         }
     *     );
     * }
     * }</pre>
     * <p><strong>事务语义说明：</strong></p>
     * <ul>
     *   <li>事务回调在事务内执行</li>
     *   <li>本地事件在事务内同步执行</li>
     *   <li>远程事件在事务提交后异步执行（如果事务回滚，远程事件不会发布）</li>
     * </ul>
     *
     * @param event              事件对象
     * @param transactionCallback 事务回调（在事务内执行）
     * @param <E>                事件类型
     */
    <E extends IBusEvent> void publishInTransaction(E event, Runnable transactionCallback);
}

