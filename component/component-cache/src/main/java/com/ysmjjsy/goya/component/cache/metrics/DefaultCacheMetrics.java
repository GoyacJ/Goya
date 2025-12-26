package com.ysmjjsy.goya.component.cache.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

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

public class DefaultCacheMetrics implements CacheMetrics {

    private static final Logger log = LoggerFactory.getLogger(DefaultCacheMetrics.class);

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
}

