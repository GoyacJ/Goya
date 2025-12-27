package com.ysmjjsy.goya.component.bus.metrics;

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
    private static final AtomicLong publishCount = new AtomicLong(0);
    private static final AtomicLong publishLocalCount = new AtomicLong(0);
    private static final AtomicLong publishRemoteCount = new AtomicLong(0);

    // 消费指标
    private static final AtomicLong consumeCount = new AtomicLong(0);
    private static final AtomicLong consumeLocalCount = new AtomicLong(0);
    private static final AtomicLong consumeRemoteCount = new AtomicLong(0);

    // 成功/失败指标
    private static final AtomicLong successCount = new AtomicLong(0);
    private static final AtomicLong failureCount = new AtomicLong(0);

    // 延迟指标（毫秒）
    private static final AtomicLong totalProcessingTime = new AtomicLong(0);
    private static final AtomicLong maxProcessingTime = new AtomicLong(0);
    private static final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);

    /**
     * 记录事件发布
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     */
    public static void recordPublish(String eventName, com.ysmjjsy.goya.component.bus.definition.EventScope scope) {
        publishCount.incrementAndGet();
        if (scope == com.ysmjjsy.goya.component.bus.definition.EventScope.LOCAL) {
            publishLocalCount.incrementAndGet();
        } else if (scope == com.ysmjjsy.goya.component.bus.definition.EventScope.REMOTE) {
            publishRemoteCount.incrementAndGet();
        }
        log.trace("[Goya] |- component [bus] EventMetrics |- publish event [{}] scope [{}]", eventName, scope);
    }

    /**
     * 记录事件消费
     *
     * @param eventName 事件名称
     * @param scope     事件作用域
     */
    public static void recordConsume(String eventName, com.ysmjjsy.goya.component.bus.definition.EventScope scope) {
        consumeCount.incrementAndGet();
        if (scope == com.ysmjjsy.goya.component.bus.definition.EventScope.LOCAL) {
            consumeLocalCount.incrementAndGet();
        } else if (scope == com.ysmjjsy.goya.component.bus.definition.EventScope.REMOTE) {
            consumeRemoteCount.incrementAndGet();
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
        successCount.incrementAndGet();
        totalProcessingTime.addAndGet(duration);
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
        failureCount.incrementAndGet();
        log.warn("[Goya] |- component [bus] EventMetrics |- failure event [{}] error [{}]", eventName, error);
    }

    /**
     * 更新处理时间统计
     *
     * @param duration 处理耗时（毫秒）
     */
    private static void updateProcessingTime(long duration) {
        long currentMax = maxProcessingTime.get();
        while (duration > currentMax && !maxProcessingTime.compareAndSet(currentMax, duration)) {
            currentMax = maxProcessingTime.get();
        }

        long currentMin = minProcessingTime.get();
        while (duration < currentMin && !minProcessingTime.compareAndSet(currentMin, duration)) {
            currentMin = minProcessingTime.get();
        }
    }

    /**
     * 获取指标快照
     *
     * @return 指标快照
     */
    public static MetricsSnapshot getSnapshot() {
        long totalProcessed = successCount.get() + failureCount.get();
        double avgProcessingTime = totalProcessed > 0
                ? (double) totalProcessingTime.get() / totalProcessed
                : 0.0;

        return new MetricsSnapshot(
                publishCount.get(),
                publishLocalCount.get(),
                publishRemoteCount.get(),
                consumeCount.get(),
                consumeLocalCount.get(),
                consumeRemoteCount.get(),
                successCount.get(),
                failureCount.get(),
                avgProcessingTime,
                maxProcessingTime.get() == Long.MAX_VALUE ? 0 : maxProcessingTime.get(),
                minProcessingTime.get() == Long.MAX_VALUE ? 0 : minProcessingTime.get()
        );
    }

    /**
     * 重置所有指标
     */
    public static void reset() {
        publishCount.set(0);
        publishLocalCount.set(0);
        publishRemoteCount.set(0);
        consumeCount.set(0);
        consumeLocalCount.set(0);
        consumeRemoteCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        totalProcessingTime.set(0);
        maxProcessingTime.set(0);
        minProcessingTime.set(Long.MAX_VALUE);
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

        @Override
        public String toString() {
            return String.format(
                    "EventMetrics{ publish=%d (local=%d, remote=%d), consume=%d (local=%d, remote=%d), " +
                            "success=%d, failure=%d, successRate=%.2f%%, avgTime=%.2fms, maxTime=%dms, minTime=%dms }",
                    publishCount, publishLocalCount, publishRemoteCount,
                    consumeCount, consumeLocalCount, consumeRemoteCount,
                    successCount, failureCount, getSuccessRate() * 100,
                    avgProcessingTime, maxProcessingTime, minProcessingTime
            );
        }
    }
}

