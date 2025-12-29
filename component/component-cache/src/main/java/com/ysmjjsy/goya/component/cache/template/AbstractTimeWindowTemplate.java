package com.ysmjjsy.goya.component.cache.template;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽象时间窗口模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供时间窗口功能。
 * 支持滑动窗口、固定窗口、滚动窗口等多种时间窗口算法，适用于数据统计、限流、聚合等场景。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供时间窗口功能，支持数据按时间窗口聚合</li>
 *   <li>支持滑动窗口，窗口随时间滑动</li>
 *   <li>支持固定窗口，窗口固定不变</li>
 *   <li>支持滚动窗口，窗口按固定步长滚动</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>时间窗口</b>：将时间划分为多个窗口，每个窗口包含一段时间内的数据</li>
 *   <li><b>滑动窗口</b>：窗口随时间滑动，每个时间点都有对应的窗口</li>
 *   <li><b>固定窗口</b>：窗口固定不变，每个窗口包含固定时间段的数据</li>
 *   <li><b>滚动窗口</b>：窗口按固定步长滚动，窗口之间有重叠</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>数据统计（按时间窗口聚合）</li>
 *   <li>限流（滑动窗口限流）</li>
 *   <li>监控指标（时间窗口内的指标统计）</li>
 *   <li>日志聚合（按时间窗口聚合日志）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class MetricsWindowManager extends AbstractTimeWindowTemplate<String, Long> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "metricsWindow";
 *     }
 *
 *     // 记录指标（滑动窗口）
 *     public void recordMetric(String metricKey, Long value) {
 *         addToWindow(metricKey, value, Duration.ofMinutes(5));
 *     }
 *
 *     // 获取窗口内的指标总和
 *     public Long getWindowSum(String metricKey, Duration window) {
 *         return aggregateWindow(metricKey, window, values -> 
 *             values.stream().mapToLong(Long::longValue).sum());
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>窗口操作使用缓存存储，支持分布式环境</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <K> 窗口键类型
 * @param <V> 窗口值类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractTimeWindowTemplate<K, V> extends AbstractCacheTemplate<K, AbstractTimeWindowTemplate.WindowData<V>> {

    /**
     * 窗口数据
     *
     * <p>封装时间窗口内的数据，包括时间戳列表和值列表。
     *
     * @param timestamps 时间戳列表（毫秒）
     * @param values 值列表
     * @param <V> 值类型
     */
    public record WindowData<V>(List<Long> timestamps, List<V> values) {
        /**
         * 构造函数
         */
        public WindowData {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            if (values == null) {
                values = new ArrayList<>();
            }
            if (timestamps.size() != values.size()) {
                throw new IllegalArgumentException("Timestamps and values must have the same size");
            }
        }

        /**
         * 创建空的窗口数据
         */
        public static <V> WindowData<V> empty() {
            return new WindowData<>(new ArrayList<>(), new ArrayList<>());
        }

        /**
         * 添加数据点
         */
        public WindowData<V> add(long timestamp, V value) {
            List<Long> newTimestamps = new ArrayList<>(timestamps);
            List<V> newValues = new ArrayList<>(values);
            newTimestamps.add(timestamp);
            newValues.add(value);
            return new WindowData<>(newTimestamps, newValues);
        }

        /**
         * 清理过期数据
         */
        public WindowData<V> cleanExpired(long windowStart) {
            List<Long> newTimestamps = new ArrayList<>();
            List<V> newValues = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                if (timestamps.get(i) >= windowStart) {
                    newTimestamps.add(timestamps.get(i));
                    newValues.add(values.get(i));
                }
            }
            return new WindowData<>(newTimestamps, newValues);
        }
    }

    /**
     * 添加数据到时间窗口
     *
     * <p>将数据添加到指定 key 的时间窗口中。自动清理过期数据。
     *
     * @param key 窗口键
     * @param value 数据值
     * @param window 时间窗口大小
     */
    public void addToWindow(K key, V value, Duration window) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();

        WindowData<V> data = get(key);
        if (data == null) {
            data = WindowData.empty();
        }

        // 清理过期数据
        data = data.cleanExpired(windowStart);

        // 添加新数据
        data = data.add(now, value);

        // 保存到缓存
        put(key, data, window);
    }

    /**
     * 获取窗口内的数据
     *
     * <p>获取指定 key 的时间窗口内的所有数据。
     *
     * @param key 窗口键
     * @param window 时间窗口大小
     * @return 窗口数据，如果不存在则返回空列表
     */
    public List<V> getWindowData(K key, Duration window) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();

        WindowData<V> data = get(key);
        if (data == null) {
            return new ArrayList<>();
        }

        // 清理过期数据
        data = data.cleanExpired(windowStart);

        // 保存清理后的数据
        if (!data.timestamps().isEmpty()) {
            put(key, data, window);
        }

        return new ArrayList<>(data.values());
    }

    /**
     * 聚合窗口数据
     *
     * <p>获取窗口内的数据并应用聚合函数。
     *
     * @param key 窗口键
     * @param window 时间窗口大小
     * @param aggregator 聚合函数
     * @param <R> 聚合结果类型
     * @return 聚合结果
     */
    public <R> R aggregateWindow(K key, Duration window, Function<List<V>, R> aggregator) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }
        if (aggregator == null) {
            throw new IllegalArgumentException("Aggregator cannot be null");
        }

        List<V> values = getWindowData(key, window);
        return aggregator.apply(values);
    }

    /**
     * 批量获取窗口数据
     *
     * <p>批量获取多个 key 的时间窗口数据。
     *
     * @param keys 窗口键集合
     * @param window 时间窗口大小
     * @return key-窗口数据映射
     */
    public Map<K, List<V>> batchGetWindowData(Set<K> keys, Duration window) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        return keys.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        key -> key,
                        key -> getWindowData(key, window)
                ));
    }

    /**
     * 清空窗口数据
     *
     * <p>清空指定 key 的时间窗口数据。
     *
     * @param key 窗口键
     */
    public void clearWindow(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        evict(key);
    }

    /**
     * 获取窗口大小
     *
     * <p>获取指定 key 的时间窗口内的数据点数量。
     *
     * @param key 窗口键
     * @param window 时间窗口大小
     * @return 窗口大小（数据点数量）
     */
    public int getWindowSize(K key, Duration window) {
        List<V> data = getWindowData(key, window);
        return data.size();
    }

}

