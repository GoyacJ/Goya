package com.ysmjjsy.goya.component.bus.stream.metrics;

import com.ysmjjsy.goya.component.bus.stream.definition.EventScope;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>事件指标工具类</p>
 * <p>用于记录事件处理的各项指标，包括发布、消费、成功、失败等</p>
 * <p>注意：这是一个简化实现，生产环境建议使用 Micrometer 等专业指标库</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 记录事件发布
 * EventMetrics.recordPublish(event.eventName(), EventScope.REMOTE);
 *
 * // 记录事件消费
 * EventMetrics.recordConsume(event.eventName(), EventScope.REMOTE);
 *
 * // 记录处理成功
 * EventMetrics.recordSuccess(event.eventName(), duration);
 *
 * // 记录处理失败
 * EventMetrics.recordFailure(event.eventName(), error);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public final class EventMetrics {

    private EventMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    // 发布指标
    private static final AtomicLong PUBLISH_COUNT = new AtomicLong(0);
    private static final AtomicLong PUBLISH_LOCAL_COUNT = new AtomicLong(0);
    private static final AtomicLong PUBLISH_REMOTE_COUNT = new AtomicLong(0);

    // 消费指标
    private static final AtomicLong CONSUME_COUNT = new AtomicLong(0);
    private static final AtomicLong CONSUME_LOCAL_COUNT = new AtomicLong(0);
    private static final AtomicLong CONSUME_REMOTE_COUNT = new AtomicLong(0);

    // 成功/失败指标
    private static final AtomicLong SUCCESS_COUNT = new AtomicLong(0);
    private static final AtomicLong FAILURE_COUNT = new AtomicLong(0);

    // 延迟指标（毫秒）
    private static final AtomicLong TOTAL_PROCESSING_TIME = new AtomicLong(0);
    private static final AtomicLong MAX_PROCESSING_TIME = new AtomicLong(0);
    private static final AtomicLong MIN_PROCESSING_TIME = new AtomicLong(Long.MAX_VALUE);

    /**
     * 记录事件发布
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     */
    public static void recordPublish(String eventName, EventScope scope) {
        PUBLISH_COUNT.incrementAndGet();
        if (scope == EventScope.LOCAL) {
            PUBLISH_LOCAL_COUNT.incrementAndGet();
        } else if (scope == EventScope.REMOTE) {
            PUBLISH_REMOTE_COUNT.incrementAndGet();
        }
        log.trace("[Goya] |- component [bus] EventMetrics |- publish event [{}] scope [{}]", eventName, scope);
    }

    /**
     * 记录事件消费
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     */
    public static void recordConsume(String eventName, EventScope scope) {
        CONSUME_COUNT.incrementAndGet();
        if (scope == EventScope.LOCAL) {
            CONSUME_LOCAL_COUNT.incrementAndGet();
        } else if (scope == EventScope.REMOTE) {
            CONSUME_REMOTE_COUNT.incrementAndGet();
        }
        log.trace("[Goya] |- component [bus] EventMetrics |- consume event [{}] scope [{}]", eventName, scope);
    }

    /**
     * 记录处理成功
     *
     * @param eventName 事件名称
     * @param duration  处理耗时（毫秒）
     */
    public static void recordSuccess(String eventName, long duration) {
        SUCCESS_COUNT.incrementAndGet();
        TOTAL_PROCESSING_TIME.addAndGet(duration);
        updateProcessingTime(duration);
        log.trace("[Goya] |- component [bus] EventMetrics |- success event [{}] duration [{}ms]", eventName, duration);
    }

    /**
     * 记录处理失败
     *
     * @param eventName 事件名称
     * @param error     错误信息
     */
    public static void recordFailure(String eventName, String error) {
        FAILURE_COUNT.incrementAndGet();
        log.warn("[Goya] |- component [bus] EventMetrics |- failure event [{}] error [{}]", eventName, error);
    }

    /**
     * 更新处理时间统计
     *
     * @param duration 处理耗时（毫秒）
     */
    private static void updateProcessingTime(long duration) {
        long currentMax = MAX_PROCESSING_TIME.get();
        while (duration > currentMax && !MAX_PROCESSING_TIME.compareAndSet(currentMax, duration)) {
            currentMax = MAX_PROCESSING_TIME.get();
        }

        long currentMin = MIN_PROCESSING_TIME.get();
        while (duration < currentMin && !MIN_PROCESSING_TIME.compareAndSet(currentMin, duration)) {
            currentMin = MIN_PROCESSING_TIME.get();
        }
    }

    /**
     * 获取指标快照
     *
     * @return 指标快照
     */
    public static MetricsSnapshot getSnapshot() {
        long totalProcessed = SUCCESS_COUNT.get() + FAILURE_COUNT.get();
        double avgProcessingTime = totalProcessed > 0
                ? (double) TOTAL_PROCESSING_TIME.get() / totalProcessed
                : 0.0;

        return new MetricsSnapshot(
                PUBLISH_COUNT.get(),
                PUBLISH_LOCAL_COUNT.get(),
                PUBLISH_REMOTE_COUNT.get(),
                CONSUME_COUNT.get(),
                CONSUME_LOCAL_COUNT.get(),
                CONSUME_REMOTE_COUNT.get(),
                SUCCESS_COUNT.get(),
                FAILURE_COUNT.get(),
                avgProcessingTime,
                MAX_PROCESSING_TIME.get() == Long.MAX_VALUE ? 0 : MAX_PROCESSING_TIME.get(),
                MIN_PROCESSING_TIME.get() == Long.MAX_VALUE ? 0 : MIN_PROCESSING_TIME.get()
        );
    }

    /**
     * 重置所有指标
     */
    public static void reset() {
        PUBLISH_COUNT.set(0);
        PUBLISH_LOCAL_COUNT.set(0);
        PUBLISH_REMOTE_COUNT.set(0);
        CONSUME_COUNT.set(0);
        CONSUME_LOCAL_COUNT.set(0);
        CONSUME_REMOTE_COUNT.set(0);
        SUCCESS_COUNT.set(0);
        FAILURE_COUNT.set(0);
        TOTAL_PROCESSING_TIME.set(0);
        MAX_PROCESSING_TIME.set(0);
        MIN_PROCESSING_TIME.set(Long.MAX_VALUE);
    }

    /**
     * 指标快照
     *
     * @param publishCount        总发布次数
     * @param publishLocalCount   本地发布次数
     * @param publishRemoteCount  远程发布次数
     * @param consumeCount        总消费次数
     * @param consumeLocalCount   本地消费次数
     * @param consumeRemoteCount  远程消费次数
     * @param successCount        成功次数
     * @param failureCount        失败次数
     * @param avgProcessingTime   平均处理时间（毫秒）
     * @param maxProcessingTime   最大处理时间（毫秒）
     * @param minProcessingTime   最小处理时间（毫秒）
     */
    public record MetricsSnapshot(
            long publishCount,
            long publishLocalCount,
            long publishRemoteCount,
            long consumeCount,
            long consumeLocalCount,
            long consumeRemoteCount,
            long successCount,
            long failureCount,
            double avgProcessingTime,
            long maxProcessingTime,
            long minProcessingTime
    ) {
        /**
         * 获取成功率
         *
         * @return 成功率（0.0-1.0）
         */
        public double getSuccessRate() {
            long total = successCount + failureCount;
            return total > 0 ? (double) successCount / total : 0.0;
        }
    }
}

