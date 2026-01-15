package com.ysmjjsy.goya.component.cache.core.metrics;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 默认缓存监控指标实现
 *
 * <p>使用简单的计数器实现监控指标，记录各种缓存操作的统计信息。
 * 未来可以扩展为使用 Micrometer 等监控框架。
 *
 * <p><b>实现方式：</b>
 * <ul>
 *   <li>使用 {@link LongAdder} 实现线程安全的计数器</li>
 *   <li>按 cacheName 分别统计</li>
 *   <li>提供简单的统计信息访问方法</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:33
 */
@Slf4j
public class DefaultCacheMetrics implements CacheMetrics {

    /**
     * L1 命中计数器
     * Key: cacheName
     * Value: 命中次数
     */
    private final ConcurrentHashMap<String, LongAdder> l1Hits = new ConcurrentHashMap<>();

    /**
     * L2 命中计数器
     * Key: cacheName
     * Value: 命中次数
     */
    private final ConcurrentHashMap<String, LongAdder> l2Hits = new ConcurrentHashMap<>();

    /**
     * 未命中计数器
     * Key: cacheName
     * Value: 未命中次数
     */
    private final ConcurrentHashMap<String, LongAdder> misses = new ConcurrentHashMap<>();

    /**
     * 布隆过滤器快速过滤计数器
     * Key: cacheName
     * Value: 过滤次数
     */
    private final ConcurrentHashMap<String, LongAdder> bloomFilterFiltered = new ConcurrentHashMap<>();

    /**
     * 布隆过滤器误判计数器
     * Key: cacheName
     * Value: 误判次数
     */
    private final ConcurrentHashMap<String, LongAdder> bloomFilterFalsePositives = new ConcurrentHashMap<>();

    /**
     * 回填成功计数器
     * Key: cacheName
     * Value: 成功次数
     */
    private final ConcurrentHashMap<String, LongAdder> refillSuccesses = new ConcurrentHashMap<>();

    /**
     * 回填失败计数器
     * Key: cacheName
     * Value: 失败次数
     */
    private final ConcurrentHashMap<String, LongAdder> refillFailures = new ConcurrentHashMap<>();

    /**
     * 布隆过滤器扩容计数器
     * Key: cacheName
     * Value: 扩容次数
     */
    private final ConcurrentHashMap<String, LongAdder> bloomFilterResizes = new ConcurrentHashMap<>();

    /**
     * L1 延迟统计
     * Key: cacheName
     * Value: 延迟统计器（用于计算平均延迟、P99延迟等）
     */
    private final ConcurrentHashMap<String, LatencyStats> l1Latencies = new ConcurrentHashMap<>();

    /**
     * L2 延迟统计
     * Key: cacheName
     * Value: 延迟统计器
     */
    private final ConcurrentHashMap<String, LatencyStats> l2Latencies = new ConcurrentHashMap<>();

    /**
     * Key 访问频率统计
     * Key: cacheName
     * Value: Key访问计数器（Key -> 访问次数）
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<Object, LongAdder>> keyAccessCounts = new ConcurrentHashMap<>();

    /**
     * 回源操作统计
     * Key: cacheName
     * Value: 回源次数和延迟统计
     */
    private final ConcurrentHashMap<String, SourceLoadStats> sourceLoadStats = new ConcurrentHashMap<>();

    /**
     * 热Key检测的采样率（0.0 - 1.0）
     * 为了控制内存开销，只采样部分Key的访问
     */
    private static final double KEY_ACCESS_SAMPLING_RATE = 0.1;

    /**
     * 热Key统计的最大Key数量（防止内存无限增长）
     */
    private static final int MAX_TRACKED_KEYS = 10000;

    @Override
    public void recordL1Hit(String cacheName) {
        l1Hits.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordL2Hit(String cacheName) {
        l2Hits.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordMiss(String cacheName) {
        misses.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordBloomFilterFiltered(String cacheName) {
        bloomFilterFiltered.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordBloomFilterFalsePositive(String cacheName) {
        bloomFilterFalsePositives.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordRefillSuccess(String cacheName) {
        refillSuccesses.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordRefillFailure(String cacheName) {
        refillFailures.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
    }

    @Override
    public void recordBloomFilterResize(String cacheName, long oldInsertions, long oldExpectedInsertions, long newExpectedInsertions) {
        bloomFilterResizes.computeIfAbsent(cacheName, k -> new LongAdder()).increment();
        if (log.isInfoEnabled()) {
            log.info("Bloom filter resize recorded: cacheName={}, oldInsertions={}, oldExpected={}, newExpected={}",
                    cacheName, oldInsertions, oldExpectedInsertions, newExpectedInsertions);
        }
    }

    @Override
    public void recordL1Latency(String cacheName, long durationNanos) {
        LatencyStats stats = l1Latencies.computeIfAbsent(cacheName, k -> new LatencyStats());
        stats.record(durationNanos);
    }

    @Override
    public void recordL2Latency(String cacheName, long durationNanos) {
        LatencyStats stats = l2Latencies.computeIfAbsent(cacheName, k -> new LatencyStats());
        stats.record(durationNanos);
    }

    @Override
    public void recordKeyAccess(String cacheName, Object key) {
        if (key == null) {
            return;
        }

        // 采样：只记录部分Key的访问（控制内存开销）
        if (Math.random() > KEY_ACCESS_SAMPLING_RATE) {
            return;
        }

        ConcurrentHashMap<Object, LongAdder> keyCounts = keyAccessCounts.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>());

        // 如果已跟踪的Key数量超过限制，清理低频Key
        if (keyCounts.size() >= MAX_TRACKED_KEYS && !keyCounts.containsKey(key)) {
            cleanupLowFrequencyKeys(keyCounts);
        }

        keyCounts.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    @Override
    public void recordSourceLoad(String cacheName, long durationNanos) {
        SourceLoadStats stats = sourceLoadStats.computeIfAbsent(cacheName, k -> new SourceLoadStats());
        stats.record(durationNanos);
    }

    /**
     * 清理低频Key（保留访问频率最高的Key）
     *
     * <p>当跟踪的Key数量超过限制时，清理访问频率最低的Key，保留高频Key。
     * 使用简单的策略：随机清理部分低频Key。
     *
     * @param keyCounts Key访问计数器
     */
    private void cleanupLowFrequencyKeys(ConcurrentHashMap<Object, LongAdder> keyCounts) {
        // 计算平均访问次数
        long totalAccess = keyCounts.values().stream()
                .mapToLong(LongAdder::sum)
                .sum();
        long avgAccess = keyCounts.isEmpty() ? 0 : totalAccess / keyCounts.size();

        // 清理访问次数低于平均值的Key（保留50%）
        List<Object> keysToRemove = keyCounts.entrySet().stream()
                .filter(entry -> entry.getValue().sum() < avgAccess)
                .limit(keyCounts.size() / 2)
                .map(Map.Entry::getKey)
                .toList();

        keysToRemove.forEach(keyCounts::remove);
    }

    /**
     * 获取布隆过滤器扩容次数
     *
     * @param cacheName 缓存名称
     * @return 扩容次数
     */
    public long getBloomFilterResizes(String cacheName) {
        LongAdder adder = bloomFilterResizes.get(cacheName);
        return adder != null ? adder.sum() : 0;
    }

    /**
     * 获取 L1 命中次数
     *
     * @param cacheName 缓存名称
     * @return 命中次数
     */
    public long getL1Hits(String cacheName) {
        LongAdder adder = l1Hits.get(cacheName);
        return adder != null ? adder.sum() : 0;
    }

    /**
     * 获取 L2 命中次数
     *
     * @param cacheName 缓存名称
     * @return 命中次数
     */
    public long getL2Hits(String cacheName) {
        LongAdder adder = l2Hits.get(cacheName);
        return adder != null ? adder.sum() : 0;
    }

    /**
     * 获取未命中次数
     *
     * @param cacheName 缓存名称
     * @return 未命中次数
     */
    public long getMisses(String cacheName) {
        LongAdder adder = misses.get(cacheName);
        return adder != null ? adder.sum() : 0;
    }

    /**
     * 计算缓存命中率
     *
     * @param cacheName 缓存名称
     * @return 命中率（0.0 - 1.0），如果没有请求则返回 0.0
     */
    public double getHitRate(String cacheName) {
        long l1Hits = getL1Hits(cacheName);
        long l2Hits = getL2Hits(cacheName);
        long misses = getMisses(cacheName);
        long total = l1Hits + l2Hits + misses;
        if (total == 0) {
            return 0.0;
        }
        return (double) (l1Hits + l2Hits) / total;
    }

    /**
     * 获取布隆过滤器误判次数
     *
     * @param cacheName 缓存名称
     * @return 误判次数
     */
    public long getBloomFilterFalsePositives(String cacheName) {
        LongAdder adder = bloomFilterFalsePositives.get(cacheName);
        return adder != null ? adder.sum() : 0;
    }

    /**
     * 获取回填成功率
     *
     * @param cacheName 缓存名称
     * @return 成功率（0.0 - 1.0），如果没有回填操作则返回 1.0
     */
    public double getRefillSuccessRate(String cacheName) {
        long successes = refillSuccesses.getOrDefault(cacheName, new LongAdder()).sum();
        long failures = refillFailures.getOrDefault(cacheName, new LongAdder()).sum();
        long total = successes + failures;
        if (total == 0) {
            return 1.0;
        }
        return (double) successes / total;
    }

    /**
     * 获取 L1 平均延迟（毫秒）
     *
     * @param cacheName 缓存名称
     * @return 平均延迟（毫秒），如果没有数据则返回 0
     */
    public double getL1AvgLatencyMs(String cacheName) {
        LatencyStats stats = l1Latencies.get(cacheName);
        return stats != null ? stats.getAvgLatencyMs() : 0.0;
    }

    /**
     * 获取 L2 平均延迟（毫秒）
     *
     * @param cacheName 缓存名称
     * @return 平均延迟（毫秒），如果没有数据则返回 0
     */
    public double getL2AvgLatencyMs(String cacheName) {
        LatencyStats stats = l2Latencies.get(cacheName);
        return stats != null ? stats.getAvgLatencyMs() : 0.0;
    }

    /**
     * 获取 L1 P99 延迟（毫秒）
     *
     * @param cacheName 缓存名称
     * @return P99 延迟（毫秒），如果没有数据则返回 0
     */
    public double getL1P99LatencyMs(String cacheName) {
        LatencyStats stats = l1Latencies.get(cacheName);
        return stats != null ? stats.getP99LatencyMs() : 0.0;
    }

    /**
     * 获取 L2 P99 延迟（毫秒）
     *
     * @param cacheName 缓存名称
     * @return P99 延迟（毫秒），如果没有数据则返回 0
     */
    public double getL2P99LatencyMs(String cacheName) {
        LatencyStats stats = l2Latencies.get(cacheName);
        return stats != null ? stats.getP99LatencyMs() : 0.0;
    }

    /**
     * 获取热Key列表（Top N）
     *
     * <p>返回访问频率最高的Key列表，用于识别热Key和容量规划。
     *
     * @param cacheName 缓存名称
     * @param topN      返回前N个热Key
     * @return 热Key列表，按访问频率降序排列
     */
    public List<HotKey> getHotKeys(String cacheName, int topN) {
        ConcurrentHashMap<Object, LongAdder> keyCounts = keyAccessCounts.get(cacheName);
        if (keyCounts == null || keyCounts.isEmpty()) {
            return Collections.emptyList();
        }

        return keyCounts.entrySet().stream()
                .map(entry -> new HotKey(entry.getKey(), entry.getValue().sum()))
                .sorted(Comparator.comparing(HotKey::accessCount).reversed())
                .limit(Math.min(topN, keyCounts.size()))
                .collect(Collectors.toList());
    }

    /**
     * 获取回源次数
     *
     * @param cacheName 缓存名称
     * @return 回源次数
     */
    public long getSourceLoadCount(String cacheName) {
        SourceLoadStats stats = sourceLoadStats.get(cacheName);
        return stats != null ? stats.getCount() : 0;
    }

    /**
     * 获取回源平均延迟（毫秒）
     *
     * @param cacheName 缓存名称
     * @return 平均延迟（毫秒），如果没有数据则返回 0
     */
    public double getSourceLoadAvgLatencyMs(String cacheName) {
        SourceLoadStats stats = sourceLoadStats.get(cacheName);
        return stats != null ? stats.getAvgLatencyMs() : 0.0;
    }

    /**
     * 延迟统计器
     *
     * <p>使用滑动窗口统计延迟，支持计算平均值和P99延迟。
     * 为了简化实现，使用固定大小的数组存储最近的延迟值。
     */
    private static class LatencyStats {
        private static final int WINDOW_SIZE = 1000;
        private final long[] latencies = new long[WINDOW_SIZE];
        private volatile int index = 0;
        private volatile long count = 0;
        private volatile long sum = 0;

        synchronized void record(long durationNanos) {
            latencies[index] = durationNanos;
            index = (index + 1) % WINDOW_SIZE;
            count++;
            sum += durationNanos;
        }

        double getAvgLatencyMs() {
            if (count == 0) {
                return 0.0;
            }
            return (sum / (double) count) / 1_000_000.0; // 纳秒转毫秒
        }

        double getP99LatencyMs() {
            if (count == 0) {
                return 0.0;
            }
            // 简化实现：使用最近WINDOW_SIZE个样本计算P99
            long[] sorted = Arrays.copyOf(latencies, Math.min(WINDOW_SIZE, (int) count));
            Arrays.sort(sorted);
            int p99Index = (int) (sorted.length * 0.99);
            return sorted[p99Index] / 1_000_000.0; // 纳秒转毫秒
        }
    }

    /**
     * 回源操作统计
     */
    private static class SourceLoadStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalLatencyNanos = new LongAdder();

        void record(long durationNanos) {
            count.increment();
            totalLatencyNanos.add(durationNanos);
        }

        long getCount() {
            return count.sum();
        }

        double getAvgLatencyMs() {
            long cnt = count.sum();
            if (cnt == 0) {
                return 0.0;
            }
            return (totalLatencyNanos.sum() / (double) cnt) / 1_000_000.0; // 纳秒转毫秒
        }
    }

    /**
     * 热Key信息
     */
    public record HotKey(Object key, long accessCount) {
    }
}

