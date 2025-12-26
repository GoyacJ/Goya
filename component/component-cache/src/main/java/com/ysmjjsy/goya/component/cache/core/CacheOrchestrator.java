package com.ysmjjsy.goya.component.cache.core;

import com.ysmjjsy.goya.component.cache.enums.ConsistencyLevel;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.local.NullValueWrapper;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.support.SingleFlightLoader;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 缓存编排器
 *
 * <p>专门负责 L1/L2 多级缓存的编排逻辑，包括查询、写入、失效等操作的协调。
 * 将编排逻辑从 GoyaCache 中分离出来，使 GoyaCache 专注于 Spring Cache 适配。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>分离职责：GoyaCache 专注于 Spring Cache 适配，CacheOrchestrator 负责编排逻辑</li>
 *   <li>提升可测试性：编排逻辑独立，便于单元测试</li>
 *   <li>支持扩展：未来可以支持不同的编排策略（如只读优化、写回模式等）</li>
 * </ul>
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>编排 L1 和 L2 的访问逻辑（查询、写入、失效）</li>
 *   <li>集成布隆过滤器进行缓存穿透保护</li>
 *   <li>管理异步回填 L1 的逻辑</li>
 *   <li>处理降级策略和一致性等级</li>
 *   <li>记录监控指标</li>
 * </ul>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>get() 操作：同步查询 L1/L2，异步回填 L1</li>
 *   <li>put() 操作：根据一致性等级决定写入策略</li>
 *   <li>evict() 操作：同步失效 L1/L2，异步发布事件</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class CacheOrchestrator {

    /**
     * 缓存名称
     */
    private final String cacheName;

    /**
     * 本地缓存（L1）
     */
    private final LocalCache l1;

    /**
     * 远程缓存（L2）
     */
    private final RemoteCache l2;

    /**
     * 缓存配置规范
     */
    private final CacheSpecification spec;

    /**
     * 布隆过滤器管理器
     */
    private final BloomFilterManager bloomFilter;

    /**
     * 缓存回填管理器
     */
    private final CacheRefillManager refillManager;

    /**
     * 降级策略
     */
    private final FallbackStrategy fallbackStrategy;

    /**
     * 监控指标
     */
    private final CacheMetrics metrics;

    /**
     * SingleFlight 加载器
     */
    private final SingleFlightLoader singleFlightLoader;

    /**
     * 构造函数
     *
     * @param cacheName 缓存名称
     * @param l1 本地缓存
     * @param l2 远程缓存
     * @param spec 缓存配置规范
     * @param bloomFilter 布隆过滤器管理器
     * @param refillManager 缓存回填管理器
     * @param fallbackStrategy 降级策略
     * @param metrics 监控指标
     * @param singleFlightLoader SingleFlight 加载器
     */
    public CacheOrchestrator(
            String cacheName,
            LocalCache l1,
            RemoteCache l2,
            CacheSpecification spec,
            BloomFilterManager bloomFilter,
            CacheRefillManager refillManager,
            FallbackStrategy fallbackStrategy,
            CacheMetrics metrics,
            SingleFlightLoader singleFlightLoader) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (l1 == null) {
            throw new IllegalArgumentException("LocalCache cannot be null");
        }
        if (l2 == null) {
            throw new IllegalArgumentException("RemoteCache cannot be null");
        }
        if (spec == null) {
            throw new IllegalArgumentException("CacheSpecification cannot be null");
        }
        if (bloomFilter == null) {
            throw new IllegalArgumentException("BloomFilterManager cannot be null");
        }
        if (refillManager == null) {
            throw new IllegalArgumentException("CacheRefillManager cannot be null");
        }
        if (fallbackStrategy == null) {
            throw new IllegalArgumentException("FallbackStrategy cannot be null");
        }
        if (singleFlightLoader == null) {
            throw new IllegalArgumentException("SingleFlightLoader cannot be null");
        }
        this.cacheName = cacheName;
        this.l1 = l1;
        this.l2 = l2;
        this.spec = spec;
        this.bloomFilter = bloomFilter;
        this.refillManager = refillManager;
        this.fallbackStrategy = fallbackStrategy;
        this.metrics = metrics;
        this.singleFlightLoader = singleFlightLoader;
    }

    /**
     * 获取缓存值
     *
     * <p>编排 L1/L2 的查询逻辑，包括布隆过滤器检查、回填管理等。
     *
     * @param key 缓存键
     * @return ValueWrapper，如果不存在则返回 null
     */
    public Cache.ValueWrapper get(Object key) {
        // 记录Key访问（用于热Key检测）
        if (metrics != null) {
            metrics.recordKeyAccess(cacheName, key);
        }

        // 1. 布隆过滤器快速路径（仅优化，不阻塞）
        boolean bloomFilterCheck = true;
        if (spec.isEnableBloomFilter()) {
            bloomFilterCheck = bloomFilter.mightContain(cacheName, key);
            if (!bloomFilterCheck) {
                // 布隆过滤器判断"不存在"，记录快速过滤指标
                // 但仍查询 L2（避免误判）
                recordBloomFilterFiltered();
            }
        }

        // 2. 查询 L1（记录延迟）
        long l1StartNanos = System.nanoTime();
        Cache.ValueWrapper l1Value = l1.get(key);
        long l1DurationNanos = System.nanoTime() - l1StartNanos;
        if (metrics != null) {
            metrics.recordL1Latency(cacheName, l1DurationNanos);
        }

        if (l1Value != null) {
            recordHit(HitLevel.L1);
            return unwrapNullValue(l1Value);
        }

        // 3. 查询 L2（即使布隆过滤器判断"不存在"也查询，避免误判）
        long l2StartNanos = System.nanoTime();
        try {
            Cache.ValueWrapper l2Value = l2.get(key);
            long l2DurationNanos = System.nanoTime() - l2StartNanos;
            if (metrics != null) {
                metrics.recordL2Latency(cacheName, l2DurationNanos);
            }

            if (l2Value != null) {
                recordHit(HitLevel.L2);

                // 如果布隆过滤器误判（判断"不存在"但 L2 命中），记录误判指标
                if (spec.isEnableBloomFilter() && !bloomFilterCheck) {
                    recordBloomFilterFalsePositive();
                }

                // 异步回填 L1（并发控制）
                refillManager.refillAsync(cacheName, key, l2Value, l1);
                return unwrapNullValue(l2Value);
            }
        } catch (Exception e) {
            long l2DurationNanos = System.nanoTime() - l2StartNanos;
            if (metrics != null) {
                metrics.recordL2Latency(cacheName, l2DurationNanos);
            }
            // L2 查询失败，根据降级策略处理
            log.warn("Failed to query L2 cache for key: {}", key, e);
            Cache.ValueWrapper fallbackValue = fallbackStrategy.onL2Failure(key, l1, e);
            if (fallbackValue != null) {
                return unwrapNullValue(fallbackValue);
            }
        }

        // 4. 未命中
        recordMiss();
        return null;
    }

    /**
     * 获取缓存值（带加载器，使用 SingleFlight 机制）
     *
     * <p>如果缓存未命中，使用 SingleFlight 机制防止缓存击穿。
     *
     * @param key 缓存键
     * @param valueLoader 值加载器
     * @param <V> 值类型
     * @return 缓存值或加载的值
     * @throws Exception 如果加载失败
     */
    @SuppressWarnings("unchecked")
    public <V> V get(Object key, Callable<V> valueLoader) throws Exception {
        // 先尝试从缓存获取
        Cache.ValueWrapper existing = get(key);
        if (existing != null) {
            return (V) existing.get();
        }

        // 使用 SingleFlight 机制防止缓存击穿
        Callable<V> wrappedLoader = () -> {
            V loadedValue = valueLoader.call();
            // 写入缓存
            put(key, loadedValue, spec.getTtl());
            return loadedValue;
        };
        return singleFlightLoader.load(key, wrappedLoader);
    }

    /**
     * 写入缓存
     *
     * <p>根据配置的一致性等级决定写入策略。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @throws IllegalArgumentException 如果 ttl 为 null 或无效
     */
    public void put(Object key, Object value, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        ConsistencyLevel consistencyLevel = spec.getConsistencyLevel();
        put(key, value, ttl, consistencyLevel);
    }

    /**
     * 写入缓存（带一致性等级）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @param consistencyLevel 一致性等级
     */
    public void put(Object key, Object value, Duration ttl, ConsistencyLevel consistencyLevel) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }
        if (consistencyLevel == null) {
            consistencyLevel = ConsistencyLevel.EVENTUAL;
        }

        // Null 值处理
        Object actualValue = wrapNullValue(value);

        // 根据一致性等级执行写入策略
        switch (consistencyLevel) {
            case STRONG -> putWithStrongConsistency(key, actualValue, ttl);
            case EVENTUAL -> putWithEventualConsistency(key, actualValue, ttl);
            case BEST_EFFORT -> putWithBestEffort(key, actualValue, ttl);
        }

        // 更新布隆过滤器（异步，失败不影响主流程）
        if (spec.isEnableBloomFilter()) {
            bloomFilter.putAsync(cacheName, key).exceptionally(e -> {
                log.warn("Failed to update bloom filter for key: {}", key, e);
                return null;
            });
        }
    }

    /**
     * 失效缓存
     *
     * @param key 缓存键
     */
    public void evict(Object key) {
        // 1. 失效 L1
        try {
            l1.evict(key);
        } catch (Exception e) {
            log.warn("Failed to evict from L1 cache for key: {}", key, e);
        }

        // 2. 失效 L2
        try {
            l2.evict(key);
        } catch (Exception e) {
            log.error("Failed to evict from L2 cache for key: {}", key, e);
        }
    }

    /**
     * 清空缓存
     */
    public void clear() {
        // 1. 清空 L1
        try {
            l1.clear();
        } catch (Exception e) {
            log.warn("Failed to clear L1 cache", e);
        }

        // 2. 清空 L2
        try {
            l2.clear();
        } catch (Exception e) {
            log.error("Failed to clear L2 cache", e);
        }
    }

    /**
     * 批量获取缓存值
     *
     * @param keys 缓存键集合
     * @return key-value 映射，只包含命中的 key
     */
    public Map<Object, Cache.ValueWrapper> batchGet(Set<Object> keys) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<Object, Cache.ValueWrapper> result = new java.util.HashMap<>();

        // 记录Key访问（用于热Key检测）
        if (metrics != null) {
            for (Object key : keys) {
                metrics.recordKeyAccess(cacheName, key);
            }
        }

        // 1. 批量查询 L1（记录延迟）
        long l1StartNanos = System.nanoTime();
        Map<Object, Cache.ValueWrapper> l1Results = l1.getAll(keys);
        long l1DurationNanos = System.nanoTime() - l1StartNanos;
        if (metrics != null && !keys.isEmpty()) {
            metrics.recordL1Latency(cacheName, l1DurationNanos / keys.size());
        }

        for (Map.Entry<Object, Cache.ValueWrapper> entry : l1Results.entrySet()) {
            Cache.ValueWrapper wrapper = entry.getValue();
            if (wrapper != null) {
                result.put(entry.getKey(), unwrapNullValue(wrapper));
                recordHit(HitLevel.L1);
            }
        }

        // 2. 找出 L1 未命中的 key
        Set<Object> l2Keys = new java.util.HashSet<>(keys);
        l2Keys.removeAll(result.keySet());

        if (l2Keys.isEmpty()) {
            return result;
        }

        // 3. 批量查询 L2（使用批量 API，记录延迟）
        long l2StartNanos = System.nanoTime();
        try {
            Map<Object, Cache.ValueWrapper> l2Results = l2.getAll(l2Keys);
            long l2DurationNanos = System.nanoTime() - l2StartNanos;
            if (metrics != null && !l2Keys.isEmpty()) {
                metrics.recordL2Latency(cacheName, l2DurationNanos / l2Keys.size());
            }

            for (Map.Entry<Object, Cache.ValueWrapper> entry : l2Results.entrySet()) {
                Cache.ValueWrapper wrapper = entry.getValue();
                if (wrapper != null) {
                    result.put(entry.getKey(), unwrapNullValue(wrapper));
                    recordHit(HitLevel.L2);

                    // 异步回填 L1
                    refillManager.refillAsync(cacheName, entry.getKey(), wrapper, l1);
                }
            }
        } catch (Exception e) {
            long l2DurationNanos = System.nanoTime() - l2StartNanos;
            if (metrics != null && !l2Keys.isEmpty()) {
                metrics.recordL2Latency(cacheName, l2DurationNanos / l2Keys.size());
            }
            log.warn("Failed to batch query L2 cache", e);
        }

        // 4. 记录未命中
        long misses = keys.size() - result.size();
        for (int i = 0; i < misses; i++) {
            recordMiss();
        }

        return result;
    }

    /**
     * 批量写入缓存
     *
     * @param entries 键值对映射
     * @param ttl 过期时间
     */
    public void batchPut(Map<Object, Object> entries, Duration ttl) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        ConsistencyLevel consistencyLevel = spec.getConsistencyLevel();
        if (consistencyLevel == null) {
            consistencyLevel = ConsistencyLevel.EVENTUAL;
        }

        // 过滤 null key 并包装 null value
        Map<Object, Object> l2Entries = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            Object wrappedValue = wrapNullValue(entry.getValue());
            l2Entries.put(entry.getKey(), wrappedValue);
        }

        if (l2Entries.isEmpty()) {
            return;
        }

        // 根据一致性等级执行批量写入策略
        switch (consistencyLevel) {
            case STRONG -> batchPutWithStrongConsistency(l2Entries, ttl);
            case EVENTUAL -> batchPutWithEventualConsistency(l2Entries, ttl);
            case BEST_EFFORT -> batchPutWithBestEffort(l2Entries, ttl);
        }

        // 更新布隆过滤器（异步）
        if (spec.isEnableBloomFilter()) {
            for (Object key : entries.keySet()) {
                if (key != null) {
                    bloomFilter.putAsync(cacheName, key).exceptionally(e -> {
                        log.warn("Failed to update bloom filter for key: {}", key, e);
                        return null;
                    });
                }
            }
        }
    }

    /**
     * 批量失效缓存
     *
     * @param keys 缓存键集合
     */
    public void batchEvict(Set<Object> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        // 1. 批量失效 L1
        for (Object key : keys) {
            try {
                l1.evict(key);
            } catch (Exception e) {
                log.warn("Failed to evict from L1 cache for key: {}", key, e);
            }
        }

        // 2. 批量失效 L2
        for (Object key : keys) {
            try {
                l2.evict(key);
            } catch (Exception e) {
                log.error("Failed to evict from L2 cache for key: {}", key, e);
            }
        }
    }

    // ========== 私有方法：一致性写入策略 ==========

    /**
     * 强一致性写入
     */
    private void putWithStrongConsistency(Object key, Object actualValue, Duration ttl) {
        // L2 写入（必须成功）
        try {
            l2.put(key, actualValue, ttl);
        } catch (Exception e) {
            log.error("Failed to write to L2 cache with STRONG consistency for key: {}", key, e);
            throw new com.ysmjjsy.goya.component.cache.exception.CacheException(
                    "L2 write failed with STRONG consistency", e);
        }

        // L1 写入（必须成功）
        try {
            l1.put(key, actualValue, ttl);
        } catch (Exception e) {
            log.error("Failed to write to L1 cache with STRONG consistency for key: {}", key, e);
            // 尝试回滚 L2（如果可能）
            try {
                l2.evict(key);
            } catch (Exception rollbackException) {
                log.warn("Failed to rollback L2 cache for key: {}", key, rollbackException);
            }
            throw new com.ysmjjsy.goya.component.cache.exception.CacheException(
                    "L1 write failed with STRONG consistency", e);
        }
    }

    /**
     * 最终一致性写入（默认）
     */
    private void putWithEventualConsistency(Object key, Object actualValue, Duration ttl) {
        // L2 写入（必须成功）
        boolean l2Success = false;
        try {
            l2.put(key, actualValue, ttl);
            l2Success = true;
        } catch (Exception e) {
            log.error("Failed to write to L2 cache for key: {}", key, e);
            fallbackStrategy.onL2WriteFailure(key, actualValue, l1, e);
            return;
        }

        // L1 写入（失败不影响主流程）
        if (l2Success) {
            try {
                l1.put(key, actualValue, ttl);
            } catch (Exception e) {
                log.error("Failed to write to L1 cache for key: {}", key, e);
            }
        }
    }

    /**
     * 尽力而为写入
     */
    private void putWithBestEffort(Object key, Object actualValue, Duration ttl) {
        // 尝试写入 L2
        boolean l2Success = false;
        try {
            l2.put(key, actualValue, ttl);
            l2Success = true;
        } catch (Exception e) {
            log.warn("Failed to write to L2 cache with BEST_EFFORT for key: {}", key, e);
            // 降级到 L1
            fallbackStrategy.onL2WriteFailure(key, actualValue, l1, e);
        }

        // 尝试写入 L1（即使 L2 失败也尝试）
        try {
            l1.put(key, actualValue, ttl);
        } catch (Exception e) {
            log.warn("Failed to write to L1 cache with BEST_EFFORT for key: {}", key, e);
        }
    }

    /**
     * 批量写入（强一致性）
     */
    private void batchPutWithStrongConsistency(Map<Object, Object> entries, Duration ttl) {
        // L2 批量写入（必须成功）
        try {
            l2.putAll(entries, ttl);
        } catch (Exception e) {
            log.error("Failed to batch write to L2 cache with STRONG consistency", e);
            throw new com.ysmjjsy.goya.component.cache.exception.CacheException(
                    "L2 batch write failed with STRONG consistency", e);
        }

        // L1 批量写入（必须成功）
        try {
            l1.putAll(entries, ttl);
        } catch (Exception e) {
            log.error("Failed to batch write to L1 cache with STRONG consistency", e);
            // 尝试回滚 L2（如果可能）
            for (Object key : entries.keySet()) {
                try {
                    l2.evict(key);
                } catch (Exception rollbackException) {
                    log.warn("Failed to rollback L2 cache for key: {}", key, rollbackException);
                }
            }
            throw new com.ysmjjsy.goya.component.cache.exception.CacheException(
                    "L1 batch write failed with STRONG consistency", e);
        }
    }

    /**
     * 批量写入（最终一致性）
     */
    private void batchPutWithEventualConsistency(Map<Object, Object> entries, Duration ttl) {
        // L2 批量写入
        boolean l2Success = false;
        try {
            l2.putAll(entries, ttl);
            l2Success = true;
        } catch (Exception e) {
            log.error("Failed to batch write to L2 cache", e);
            // 降级到单个写入
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                try {
                    put(entry.getKey(), entry.getValue(), ttl);
                } catch (Exception ex) {
                    log.warn("Failed to put key to cache: {}", entry.getKey(), ex);
                }
            }
        }

        // L1 批量写入（失败不影响主流程）
        if (l2Success) {
            try {
                l1.putAll(entries, ttl);
            } catch (Exception e) {
                log.error("Failed to batch write to L1 cache", e);
            }
        }
    }

    /**
     * 批量写入（尽力而为）
     */
    private void batchPutWithBestEffort(Map<Object, Object> entries, Duration ttl) {
        // 尝试批量写入 L2
        boolean l2Success = false;
        try {
            l2.putAll(entries, ttl);
            l2Success = true;
        } catch (Exception e) {
            log.warn("Failed to batch write to L2 cache with BEST_EFFORT", e);
            // 降级到单个写入
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                try {
                    put(entry.getKey(), entry.getValue(), ttl, ConsistencyLevel.BEST_EFFORT);
                } catch (Exception ex) {
                    log.warn("Failed to put key to cache: {}", entry.getKey(), ex);
                }
            }
        }

        // 尝试批量写入 L1（即使 L2 失败也尝试）
        try {
            l1.putAll(entries, ttl);
        } catch (Exception e) {
            log.warn("Failed to batch write to L1 cache with BEST_EFFORT", e);
        }
    }

    // ========== 私有方法：辅助方法 ==========

    /**
     * 解包 NullValueWrapper
     */
    private Cache.ValueWrapper unwrapNullValue(Cache.ValueWrapper wrapper) {
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (NullValueWrapper.isNullValue(value)) {
            return new SimpleValueWrapper(null);
        }
        return wrapper;
    }

    /**
     * 包装 null 值为 NullValueWrapper
     */
    private Object wrapNullValue(Object value) {
        if (value == null) {
            if (!spec.isAllowNullValues()) {
                throw new IllegalArgumentException("Null values are not allowed for cache: " + cacheName);
            }
            return NullValueWrapper.INSTANCE;
        }
        return value;
    }

    /**
     * 记录缓存命中
     */
    private void recordHit(HitLevel level) {
        if (metrics != null) {
            if (level == HitLevel.L1) {
                metrics.recordL1Hit(cacheName);
            } else {
                metrics.recordL2Hit(cacheName);
            }
        }
    }

    /**
     * 记录缓存未命中
     */
    private void recordMiss() {
        if (metrics != null) {
            metrics.recordMiss(cacheName);
        }
    }

    /**
     * 记录布隆过滤器快速过滤
     */
    private void recordBloomFilterFiltered() {
        if (metrics != null) {
            metrics.recordBloomFilterFiltered(cacheName);
        }
    }

    /**
     * 记录布隆过滤器误判
     */
    private void recordBloomFilterFalsePositive() {
        if (metrics != null) {
            metrics.recordBloomFilterFalsePositive(cacheName);
        }
    }

    /**
     * 缓存命中级别
     */
    private enum HitLevel {
        L1, L2
    }
}

