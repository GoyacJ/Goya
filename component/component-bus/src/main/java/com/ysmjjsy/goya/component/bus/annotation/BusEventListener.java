package com.ysmjjsy.goya.component.bus.annotation;

import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.enums.AckMode;
import org.springframework.context.event.EventListener;

import java.lang.annotation.*;

/**
 * <p>事件监听器注解</p>
 * <p>用于声明式订阅事件，支持本地和远程事件监听</p>
 * <p>继承 Spring 的 @EventListener，自动注册为事件监听器</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class OrderEventListener {
 *
 *     @BusEventListener(scope = {EventScope.LOCAL, EventScope.REMOTE})
 *     public void handleOrderCreated(OrderCreatedEvent event) {
 *         // 处理订单创建事件
 *     }
 *
 *     @BusEventListener(
 *         scope = {EventScope.REMOTE},
 *         condition = "#event.amount > 1000",
 *         async = true,
 *         ackMode = AckMode.MANUAL,
 *         maxRetries = 3
 *     )
 *     public void handleLargeOrder(OrderCreatedEvent event, Acknowledgment ack) {
 *         // 处理大额订单，异步执行，手动确认
 *         try {
 *             processLargeOrder(event);
 *             ack.acknowledge();
 *         } catch (Exception e) {
 *             // 不确认，触发重试
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see EventListener
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener
public @interface BusEventListener {

    /**
     * 事件作用域
     * <p>指定监听哪些作用域的事件（LOCAL、REMOTE、ALL）</p>
     * <p>默认只监听本地事件</p>
     *
     * @return 事件作用域数组
     */
    EventScope[] scope() default {EventScope.LOCAL};

    /**
     * 事件名称列表（用于事件匹配）
     * <p>eventName 默认是事件的 getClass().getSimpleName()</p>
     * <p>匹配优先级：</p>
     * <ol>
     *   <li>如果 eventNames 不为空，则必须匹配其中一个（eventName 相等）</li>
     *   <li>如果监听器参数是具体类型（非 String），则通过类型匹配（参数的 getClass().getSimpleName() 和事件的 getClass().getSimpleName() 相等）</li>
     *   <li>如果监听器参数是 String，则必须匹配 eventNames，如果为空或不相等则认为不匹配</li>
     * </ol>
     * <p>使用示例：</p>
     * <pre>{@code
     * // 监听特定 eventName 的事件（String 类型监听器）
     * @BusEventListener(eventNames = {"OrderCreatedEvent"})
     * public void handleOrderCreated(String jsonEvent) {
     *     // 处理订单创建事件
     * }
     *
     * // 监听特定 eventName 的事件（具体类型监听器）
     * @BusEventListener(eventNames = {"OrderCreatedEvent"})
     * public void handleOrderCreated(OrderCreatedEvent event) {
     *     // 处理订单创建事件
     * }
     * }</pre>
     *
     * @return 事件名称数组
     */
    String[] eventNames() default {};

    /**
     * SpEL 条件表达式
     * <p>只有当表达式计算结果为 true 时才会处理事件</p>
     * <p>示例：condition = "#event.amount > 1000"</p>
     *
     * @return SpEL 条件表达式
     */
    String condition() default "";

    /**
     * 是否异步执行
     * <p>true：异步执行，使用 @Async</p>
     * <p>false：同步执行</p>
     * <p>仅对本地事件有效</p>
     *
     * @return 是否异步
     */
    boolean async() default false;

    /**
     * 消息确认模式
     * <p>AUTO：自动确认</p>
     * <p>MANUAL：手动确认，需要在方法参数中接收 Acknowledgment</p>
     * <p>仅对远程事件有效</p>
     *
     * @return 确认模式
     */
    AckMode ackMode() default AckMode.AUTO;

    /**
     * 最大重试次数
     * <p>用于覆盖全局重试配置</p>
     * <p>注意：Spring Cloud Stream 的配置是全局的，此属性主要用于文档和提示</p>
     *
     * @return 最大重试次数
     */
    int maxRetries() default 3;
}

