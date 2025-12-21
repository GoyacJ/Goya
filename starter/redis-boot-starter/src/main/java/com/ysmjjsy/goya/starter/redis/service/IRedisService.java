package com.ysmjjsy.goya.starter.redis.service;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * <p>Redis 特有功能服务接口</p>
 * <p>提供 Redis 独有的分布式功能，包括：</p>
 * <ul>
 *     <li>原子计数器：分布式环境下的原子递增/递减操作</li>
 *     <li>发布订阅：消息发布和订阅功能</li>
 *     <li>分布式信号量：控制分布式环境下的资源访问</li>
 *     <li>分布式倒计时门闩：协调多个分布式任务的执行</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22 00:05
 * @see org.redisson.api.RedissonClient
 */
public interface IRedisService {

    /* ---------- 原子计数器操作 ---------- */

    /**
     * 递增计数器
     *
     * @param key 计数器键
     * @return 递增后的值
     */
    Long increment(String key);

    /**
     * 递增计数器指定值
     *
     * @param key   计数器键
     * @param delta 增量值
     * @return 递增后的值
     */
    Long incrementBy(String key, long delta);

    /**
     * 递减计数器
     *
     * @param key 计数器键
     * @return 递减后的值
     */
    Long decrement(String key);

    /**
     * 递减计数器指定值
     *
     * @param key   计数器键
     * @param delta 减量值
     * @return 递减后的值
     */
    Long decrementBy(String key, long delta);

    /**
     * 获取计数器当前值
     *
     * @param key 计数器键
     * @return 当前值
     */
    Long getCounter(String key);

    /**
     * 设置计数器值
     *
     * @param key   计数器键
     * @param value 值
     */
    void setCounter(String key, long value);

    /**
     * 获取原子 Long 对象
     * 可用于更复杂的原子操作
     *
     * @param key 计数器键
     * @return RAtomicLong 对象
     */
    RAtomicLong getAtomicLong(String key);

    /* ---------- 发布订阅操作 ---------- */

    /**
     * 发布消息到指定主题
     *
     * @param topic   主题名称
     * @param message 消息内容
     * @param <T>     消息类型
     * @return 接收到消息的订阅者数量
     */
    <T> long publish(String topic, T message);

    /**
     * 订阅主题
     *
     * @param topic    主题名称
     * @param listener 消息监听器
     * @param <T>      消息类型
     * @return 监听器 ID，用于后续取消订阅
     */
    <T> int subscribe(String topic, Consumer<T> listener);

    /**
     * 取消订阅
     *
     * @param topic      主题名称
     * @param listenerId 监听器 ID
     */
    void unsubscribe(String topic, int listenerId);

    /* ---------- 分布式信号量操作 ---------- */

    /**
     * 获取信号量对象
     *
     * @param name 信号量名称
     * @return RSemaphore 对象
     */
    RSemaphore getSemaphore(String name);

    /**
     * 尝试设置信号量许可数量
     *
     * @param name    信号量名称
     * @param permits 许可数量
     * @return 是否设置成功
     */
    boolean trySetPermits(String name, int permits);

    /**
     * 获取可用许可数量
     *
     * @param name 信号量名称
     * @return 可用许可数量
     */
    int availablePermits(String name);

    /**
     * 获取许可
     * 如果没有可用许可，则阻塞等待
     *
     * @param name 信号量名称
     */
    void acquire(String name);

    /**
     * 尝试获取许可
     *
     * @param name 信号量名称
     * @return 是否获取成功
     */
    boolean tryAcquire(String name);

    /**
     * 尝试获取许可，等待指定时间
     *
     * @param name    信号量名称
     * @param timeout 等待时间
     * @return 是否获取成功
     */
    boolean tryAcquire(String name, Duration timeout);

    /**
     * 释放许可
     *
     * @param name 信号量名称
     */
    void release(String name);

    /* ---------- 分布式倒计时门闩操作 ---------- */

    /**
     * 获取倒计时门闩对象
     *
     * @param name 门闩名称
     * @return RCountDownLatch 对象
     */
    RCountDownLatch getCountDownLatch(String name);

    /**
     * 尝试设置倒计时数量
     *
     * @param name  门闩名称
     * @param count 倒计时数量
     * @return 是否设置成功
     */
    boolean trySetCount(String name, long count);

    /**
     * 倒计时减一
     *
     * @param name 门闩名称
     */
    void countDown(String name);

    /**
     * 获取当前倒计时数量
     *
     * @param name 门闩名称
     * @return 倒计时数量
     */
    long getCount(String name);

    /**
     * 等待倒计时到零
     *
     * @param name 门闩名称
     */
    void await(String name);

    /**
     * 等待倒计时到零，带超时时间
     *
     * @param name    门闩名称
     * @param timeout 超时时间
     * @return 是否在超时前完成等待
     */
    boolean await(String name, Duration timeout);;

    /**
     * 获取 Redis 信息
     *
     * @return Redis 服务器信息
     */
    String info();
}

