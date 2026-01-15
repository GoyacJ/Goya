package com.ysmjjsy.goya.component.cache.core.metrics;

/**
 * 缓存监控指标接口
 *
 * <p>定义缓存操作的监控指标，包括命中率、回填成功率、布隆过滤器误判率等。
 * 用于可观测性和性能分析。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>记录 L1/L2 缓存命中率</li>
 *   <li>记录缓存未命中次数</li>
 *   <li>记录回填成功/失败次数</li>
 *   <li>记录布隆过滤器误判率</li>
 * </ul>
 *
 * <p><b>实现方式：</b>
 * <ul>
 *   <li>可以使用 Micrometer、Prometheus 等监控框架</li>
 *   <li>也可以使用简单的计数器实现</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:32
 */

public interface CacheMetrics {

    /**
     * 记录 L1 缓存命中
     *
     * @param cacheName 缓存名称
     */
    void recordL1Hit(String cacheName);

    /**
     * 记录 L2 缓存命中
     *
     * @param cacheName 缓存名称
     */
    void recordL2Hit(String cacheName);

    /**
     * 记录缓存未命中
     *
     * @param cacheName 缓存名称
     */
    void recordMiss(String cacheName);

    /**
     * 记录布隆过滤器快速过滤
     *
     * <p>当布隆过滤器判断 key "不存在"时调用，用于监控布隆过滤器的过滤效果。
     *
     * @param cacheName 缓存名称
     */
    void recordBloomFilterFiltered(String cacheName);

    /**
     * 记录布隆过滤器误判（False Positive）
     *
     * <p>当布隆过滤器判断 key "不存在"但 L2 实际命中时调用，用于监控布隆过滤器的误判率。
     *
     * @param cacheName 缓存名称
     */
    void recordBloomFilterFalsePositive(String cacheName);

    /**
     * 记录回填成功
     *
     * @param cacheName 缓存名称
     */
    void recordRefillSuccess(String cacheName);

    /**
     * 记录回填失败
     *
     * @param cacheName 缓存名称
     */
    void recordRefillFailure(String cacheName);

    /**
     * 记录布隆过滤器扩容事件
     *
     * <p>当布隆过滤器扩容时调用，用于监控扩容频率和数据丢失风险。
     *
     * @param cacheName 缓存名称
     * @param oldInsertions 扩容前的插入量
     * @param oldExpectedInsertions 扩容前的预期插入量
     * @param newExpectedInsertions 扩容后的预期插入量
     */
    void recordBloomFilterResize(String cacheName, long oldInsertions, long oldExpectedInsertions, long newExpectedInsertions);

    /**
     * 记录 L1 查询延迟
     *
     * <p>用于监控 L1 缓存的查询性能，识别慢查询。
     *
     * @param cacheName 缓存名称
     * @param durationNanos 查询耗时（纳秒）
     */
    void recordL1Latency(String cacheName, long durationNanos);

    /**
     * 记录 L2 查询延迟
     *
     * <p>用于监控 L2 缓存的查询性能，识别慢查询和网络问题。
     *
     * @param cacheName 缓存名称
     * @param durationNanos 查询耗时（纳秒）
     */
    void recordL2Latency(String cacheName, long durationNanos);

    /**
     * 记录 Key 访问
     *
     * <p>用于追踪热 Key 和 Top N Key 统计。
     * 此方法会被频繁调用，实现应该考虑性能开销。
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    void recordKeyAccess(String cacheName, Object key);

    /**
     * 记录回源操作
     *
     * <p>当缓存未命中后，从数据源加载数据时调用。
     * 用于监控回源频率和性能。
     *
     * @param cacheName 缓存名称
     * @param durationNanos 回源耗时（纳秒）
     */
    void recordSourceLoad(String cacheName, long durationNanos);
}
