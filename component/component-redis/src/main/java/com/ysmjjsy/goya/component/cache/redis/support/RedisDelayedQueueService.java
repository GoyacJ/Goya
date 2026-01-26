package com.ysmjjsy.goya.component.cache.redis.support;

import java.time.Duration;
import java.util.Optional;

/**
 * <p>Redis 延迟队列服务</p>
 * <p>基于 Redisson {@code RDelayedQueue + (RBlockingQueue/RQueue)} 实现。</p>
 *
 * <p><b>设计目标：</b></p>
 * <ul>
 *   <li>支持延迟投递（delay）</li>
 *   <li>支持阻塞/非阻塞消费</li>
 *   <li>统一命名空间与租户隔离</li>
 * </ul>
 *
 * <p><b>语义说明：</b></p>
 * <ul>
 *   <li>延迟投递后，消息到期会被转移到“真实队列”中供消费</li>
 *   <li>消息序列化走 Redisson 全局 Codec</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 23:40
 */
public interface RedisDelayedQueueService {

    /**
     * 延迟投递消息。
     *
     * @param queue   队列名（业务语义名，例如 "order-timeout"）
     * @param message 消息对象
     * @param delay   延迟时长（null 或 <=0 则视为立即投递）
     */
    void enqueue(String queue, Object message, Duration delay);

    /**
     * 非阻塞拉取一条消息。
     *
     * @param queue 队列名
     * @return 消息（可能为空）
     */
    Optional<Object> poll(String queue);

    /**
     * 阻塞等待一条消息（带超时）。
     *
     * @param queue   队列名
     * @param timeout 超时（null 或 <=0 则立即返回）
     * @return 消息（可能为空）
     */
    Optional<Object> take(String queue, Duration timeout);

    /**
     * 销毁延迟队列结构。
     *
     * <p>用于应用关闭或确知不再需要该延迟队列时释放 Redisson 内部调度资源。</p>
     *
     * @param queue 队列名
     */
    void destroy(String queue);
}
