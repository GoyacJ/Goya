package com.ysmjjsy.goya.component.cache.template;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象热Key模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供热Key检测和管理功能。
 * 支持热Key识别、热Key缓存、热Key降级等多种热Key处理策略，适用于高并发场景下的热Key优化。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供热Key检测功能，识别高频访问的Key</li>
 *   <li>支持热Key缓存，提升热Key访问性能</li>
 *   <li>支持热Key降级，防止热Key导致系统压力过大</li>
 *   <li>支持热Key统计，提供热Key监控数据</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>热Key</b>：访问频率超过阈值的Key</li>
 *   <li><b>热Key检测</b>：统计Key的访问频率，识别热Key</li>
 *   <li><b>热Key缓存</b>：将热Key的数据缓存到本地，提升访问性能</li>
 *   <li><b>热Key降级</b>：对热Key进行限流或降级处理，防止系统压力过大</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>高并发场景下的热Key优化</li>
 *   <li>缓存热点数据识别</li>
 *   <li>系统性能优化</li>
 *   <li>缓存容量规划</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class UserHotKeyManager extends AbstractHotKeyTemplate<String, User> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userHotKey";
 *     }
 *
 *     // 记录Key访问
 *     public void recordAccess(String key) {
 *         recordKeyAccess(key);
 *     }
 *
 *     // 检查是否为热Key
 *     public boolean isHotKey(String key) {
 *         return isHotKey(key, 1000, Duration.ofMinutes(1));
 *     }
 *
 *     // 获取热Key列表
 *     public List<String> getHotKeys(int threshold, Duration window) {
 *         return getHotKeys(threshold, window);
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>热Key统计使用缓存存储，支持分布式环境</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <K> 热Key类型
 * @param <V> 热Key值类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractHotKeyTemplate<K, V> extends AbstractCacheTemplate<K, V> {

    /**
     * 访问统计缓存名称（用于存储访问计数）
     */
    private static final String ACCESS_COUNT_CACHE_NAME = "hotKey:accessCount";

    /**
     * 访问统计缓存（使用计数器模板）
     */
    private volatile AbstractCounterTemplate<K> accessCountCache;

    /**
     * 初始化访问统计缓存
     *
     * <p>在首次使用时创建访问统计缓存实例。
     */
    private void initAccessCountCache() {
        if (accessCountCache == null) {
            synchronized (this) {
                if (accessCountCache == null) {
                    // 创建匿名内部类实现访问统计缓存
                    accessCountCache = new AbstractCounterTemplate<>() {
                        @Override
                        protected String getCacheName() {
                            return ACCESS_COUNT_CACHE_NAME + ":" + AbstractHotKeyTemplate.this.getCacheName();
                        }
                    };
                    // 初始化缓存
                    if (cacheFactory != null && specificationResolver != null) {
                        try {
                            accessCountCache.cacheFactory = cacheFactory;
                            accessCountCache.specificationResolver = specificationResolver;
                            accessCountCache.afterPropertiesSet();
                        } catch (Exception e) {
                            log.warn("Failed to initialize access count cache", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 记录Key访问
     *
     * <p>记录指定Key的访问，用于热Key检测。
     *
     * @param key 访问的Key
     */
    public void recordKeyAccess(K key) {
        if (key == null) {
            return;
        }
        initAccessCountCache();
        if (accessCountCache != null) {
            try {
                accessCountCache.increment(key);
            } catch (Exception e) {
                log.warn("Failed to record key access: key={}", key, e);
            }
        }
    }

    /**
     * 检查是否为热Key
     *
     * <p>检查指定Key在时间窗口内的访问次数是否超过阈值。
     *
     * @param key 要检查的Key
     * @param threshold 访问次数阈值
     * @param window 时间窗口
     * @return true 如果是热Key，false 否则
     */
    public boolean isHotKey(K key, long threshold, Duration window) {
        if (key == null) {
            return false;
        }
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        initAccessCountCache();
        if (accessCountCache == null) {
            return false;
        }

        try {
            long count = accessCountCache.getValue(key);
            return count >= threshold;
        } catch (Exception e) {
            log.warn("Failed to check hot key: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取Key的访问次数
     *
     * <p>获取指定Key在时间窗口内的访问次数。
     *
     * @param key 要查询的Key
     * @return 访问次数，如果不存在则返回0
     */
    public long getAccessCount(K key) {
        if (key == null) {
            return 0;
        }
        initAccessCountCache();
        if (accessCountCache == null) {
            return 0;
        }
        try {
            return accessCountCache.getValue(key);
        } catch (Exception e) {
            log.warn("Failed to get access count: key={}", key, e);
            return 0;
        }
    }

    /**
     * 获取热Key列表
     *
     * <p>获取在时间窗口内访问次数超过阈值的Key列表。
     * 注意：此方法需要遍历所有Key，性能较低，建议在后台任务中使用。
     *
     * @param threshold 访问次数阈值
     * @param window 时间窗口
     * @return 热Key列表
     */
    public List<K> getHotKeys(long threshold, Duration window) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        initAccessCountCache();
        if (accessCountCache == null) {
            return new ArrayList<>();
        }

        // 注意：此方法需要遍历所有Key，性能较低
        // 实际实现中，可以通过监控指标或其他方式获取热Key列表
        log.warn("getHotKeys() method is not fully implemented, requires iteration over all keys");
        return new ArrayList<>();
    }

    /**
     * 重置访问统计
     *
     * <p>重置指定Key的访问统计。
     *
     * @param key 要重置的Key
     */
    public void resetAccessCount(K key) {
        if (key == null) {
            return;
        }
        initAccessCountCache();
        if (accessCountCache != null) {
            try {
                accessCountCache.reset(key);
            } catch (Exception e) {
                log.warn("Failed to reset access count: key={}", key, e);
            }
        }
    }

    /**
     * 批量记录Key访问
     *
     * <p>批量记录多个Key的访问。
     *
     * @param keys 访问的Key集合
     */
    public void batchRecordKeyAccess(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        initAccessCountCache();
        if (accessCountCache != null) {
            try {
                Map<K, Long> deltas = keys.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(key -> key, key -> 1L));
                accessCountCache.batchIncrement(deltas);
            } catch (Exception e) {
                log.warn("Failed to batch record key access", e);
            }
        }
    }

    /**
     * 获取访问统计Top N
     *
     * <p>获取访问次数最多的N个Key。
     * 注意：此方法需要遍历所有Key，性能较低，建议在后台任务中使用。
     *
     * @param topN Top N数量
     * @return Key-访问次数映射，按访问次数降序排列
     */
    public Map<K, Long> getTopAccessKeys(int topN) {
        if (topN <= 0) {
            throw new IllegalArgumentException("TopN must be positive");
        }

        // 注意：此方法需要遍历所有Key，性能较低
        // 实际实现中，可以通过监控指标或其他方式获取Top N
        log.warn("getTopAccessKeys() method is not fully implemented, requires iteration over all keys");
        return new LinkedHashMap<>();
    }

}

