package com.ysmjjsy.goya.component.cache.core;

import com.ysmjjsy.goya.component.cache.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.event.LocalCacheEvictionEvent;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.local.NullValueWrapper;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.context.event.EventListener;

import java.util.concurrent.Callable;

/**
 * 多级缓存编排实现
 *
 * <p>实现 Spring Cache 的 {@link Cache} 接口，编排本地缓存（L1）和远程缓存（L2），
 * 提供统一的多级缓存访问能力。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>实现 Spring Cache SPI，提供标准的缓存操作接口</li>
 *   <li>编排 L1 和 L2 的访问逻辑（查询、写入、失效）</li>
 *   <li>处理 null 值的包装和解包（使用 NullValueWrapper）</li>
 *   <li>集成布隆过滤器进行缓存穿透保护</li>
 *   <li>管理异步回填 L1 的逻辑</li>
 *   <li>发布缓存失效事件</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>实现 {@link Cache} 接口，完全兼容 Spring Cache 注解（@Cacheable、@CacheEvict 等）</li>
 *   <li>由 {@link GoyaCacheManager} 管理，通过 {@link org.springframework.cache.CacheManager#getCache(String)} 获取</li>
 *   <li>Spring Cache 拦截器会调用此类的 get/put/evict 方法</li>
 * </ul>
 *
 * <p><b>多级缓存访问流程：</b>
 * <ol>
 *   <li><b>get() 操作：</b>
 *     <ol>
 *       <li>布隆过滤器快速路径检查（如果启用）</li>
 *       <li>查询 L1，命中则返回</li>
 *       <li>查询 L2，命中则异步回填 L1 并返回</li>
 *       <li>未命中则返回 null（触发方法执行）</li>
 *     </ol>
 *   </li>
 *   <li><b>put() 操作：</b>
 *     <ol>
 *       <li>包装 null 值为 NullValueWrapper（如果允许）</li>
 *       <li>写入 L2（同步，确保持久化）</li>
 *       <li>写入 L1（同步，保证当前节点一致性）</li>
 *       <li>更新布隆过滤器（异步，失败不影响主流程）</li>
 *     </ol>
 *   </li>
 *   <li><b>evict() 操作：</b>
 *     <ol>
 *       <li>失效 L1（同步）</li>
 *       <li>失效 L2（同步）</li>
 *       <li>发布失效事件（其他节点会收到通知）</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>get() 操作：同步查询 L1/L2，异步回填 L1</li>
 *   <li>put() 操作：同步写入 L1/L2，异步更新布隆过滤器</li>
 *   <li>evict() 操作：同步失效 L1/L2，异步发布事件</li>
 * </ul>
 *
 * <p><b>异常处理策略：</b>
 * <ul>
 *   <li>L2 查询失败：根据降级策略处理（降级到 L1 / 快速失败 / 忽略）</li>
 *   <li>L2 写入失败：记录错误日志，继续写入 L1（保证当前节点可用）</li>
 *   <li>L1 回填失败：记录警告日志，不影响主流程（L2 已命中，数据可用）</li>
 *   <li>布隆过滤器更新失败：记录警告日志，不影响主流程（布隆过滤器是优化，非必需）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:42
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaCache<K, V> implements Cache {

    /**
     * 缓存名称
     */
    private final String name;

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
     * 缓存事件发布器
     */
    private final CacheEventPublisher eventPublisher;

    /**
     * 降级策略
     */
    private final FallbackStrategy fallbackStrategy;

    /**
     * 监控指标
     */
    private final CacheMetrics metrics;

    @Override
    @NullMarked
    public String getName() {
        return name;
    }

    @Override
    @NullMarked
    public Object getNativeCache() {
        // 返回 L1 的 native cache（通常是 Caffeine Cache）
        return l1.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        @SuppressWarnings("unchecked")
        K typedKey = (K) key;
        return getTyped(typedKey);
    }

    /**
     * 类型安全的获取方法
     *
     * @param key 缓存键
     * @return ValueWrapper，如果不存在则返回 null
     */
    public ValueWrapper getTyped(K key) {
        // 1. 布隆过滤器快速路径（仅优化，不阻塞）
        // 即使布隆过滤器判断"不存在"，仍查询 L2，避免误判
        boolean bloomFilterCheck = true;
        if (spec.isEnableBloomFilter()) {
            bloomFilterCheck = bloomFilter.mightContain(name, key);
            if (!bloomFilterCheck) {
                // 布隆过滤器判断"不存在"，记录快速过滤指标
                // 但仍查询 L2（避免误判）
                recordBloomFilterFiltered();
            }
        }

        // 2. 查询 L1
        ValueWrapper l1Value = l1.get(key);
        if (l1Value != null) {
            recordHit(HitLevel.L1);
            return unwrapNullValue(l1Value);
        }

        // 3. 查询 L2（即使布隆过滤器判断"不存在"也查询，避免误判）
        try {
            ValueWrapper l2Value = l2.get(key);
            if (l2Value != null) {
                recordHit(HitLevel.L2);

                // 如果布隆过滤器误判（判断"不存在"但 L2 命中），记录误判指标
                if (spec.isEnableBloomFilter() && !bloomFilterCheck) {
                    recordBloomFilterFalsePositive();
                }

                // 异步回填 L1（并发控制）
                refillManager.refillAsync(name, key, l2Value, l1);
                return unwrapNullValue(l2Value);
            }
        } catch (Exception e) {
            // L2 查询失败，根据降级策略处理
            log.warn("Failed to query L2 cache for key: {}", key, e);
            ValueWrapper fallbackValue = fallbackStrategy.onL2Failure(key, l1, e);
            if (fallbackValue != null) {
                return unwrapNullValue(fallbackValue);
            }
        }

        // 4. 未命中
        recordMiss();
        return null;
    }

    /**
     * 类型安全的获取值方法（直接返回类型化值）
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public V getTypedValue(K key) {
        ValueWrapper wrapper = getTyped(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (value == null) {
            return null;
        }
        return (V) value;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        ValueWrapper wrapper = getTyped((K) key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // Spring Cache 标准方法：支持缓存穿透保护
        @SuppressWarnings("unchecked")
        K typedKey = (K) key;
        ValueWrapper existing = getTyped(typedKey);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            T value = (T) existing.get();
            return value;
        }

        // 执行 valueLoader（可能包含锁，防止缓存击穿）
        T value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }

        // 写入缓存
        @SuppressWarnings("unchecked")
        V typedValue = (V) value;
        putTyped(typedKey, typedValue);
        return value;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        @SuppressWarnings("unchecked")
        K typedKey = (K) key;
        @SuppressWarnings("unchecked")
        V typedValue = (V) value;
        putTyped(typedKey, typedValue);
    }

    /**
     * 类型安全的写入方法
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void putTyped(K key, V value) {
        // 1. Null 值处理
        Object actualValue = wrapNullValue(value);

        // 2. 写入 L2（同步，确保持久化）
        try {
            l2.put(key, actualValue, spec.getTtl());
        } catch (Exception e) {
            // L2 写入失败，根据降级策略处理
            log.error("Failed to write to L2 cache for key: {}", key, e);
            fallbackStrategy.onL2WriteFailure(key, actualValue, l1, e);
            // 继续写入 L1（保证当前节点可用）
        }

        // 3. 写入 L1（同步，保证当前节点一致性）
        try {
            l1.put(key, actualValue, spec.getLocalTtl());
        } catch (Exception e) {
            log.error("Failed to write to L1 cache for key: {}", key, e);
            // L1 写入失败不影响 L2，但记录错误
        }

        // 4. 更新布隆过滤器（异步，失败不影响主流程）
        if (spec.isEnableBloomFilter()) {
            bloomFilter.putAsync(name, key).exceptionally(e -> {
                log.warn("Failed to update bloom filter for key: {}", key, e);
                return null;
            });
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        @SuppressWarnings("unchecked")
        K typedKey = (K) key;
        evictTyped(typedKey);
    }

    /**
     * 类型安全的失效方法
     *
     * @param key 缓存键
     */
    public void evictTyped(K key) {
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

        // 3. 发布失效事件（其他节点会收到通知）
        eventPublisher.publishEviction(name, key);
    }

    @Override
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

        // 3. 发布清空事件
        eventPublisher.publishClear(name);
    }

    /**
     * 解包 NullValueWrapper
     *
     * <p>如果 ValueWrapper 中的值是 NullValueWrapper 实例，则返回包含 null 的 ValueWrapper。
     *
     * @param wrapper 原始 ValueWrapper
     * @return 解包后的 ValueWrapper
     */
    private ValueWrapper unwrapNullValue(ValueWrapper wrapper) {
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
     *
     * <p>如果 value 为 null 且允许 null 值，则返回 NullValueWrapper.INSTANCE。
     * 否则，如果 value 为 null 但不允许 null 值，抛出异常。
     *
     * @param value 原始值
     * @return 包装后的值（如果为 null 则返回 NullValueWrapper.INSTANCE）
     * @throws IllegalArgumentException 如果 value 为 null 但不允许 null 值
     */
    private Object wrapNullValue(Object value) {
        if (value == null) {
            if (!spec.isAllowNullValues()) {
                throw new IllegalArgumentException("Null values are not allowed for cache: " + name);
            }
            return NullValueWrapper.INSTANCE;
        }
        return value;
    }

    /**
     * 记录缓存命中
     *
     * @param level 命中级别（L1 或 L2）
     */
    private void recordHit(HitLevel level) {
        if (metrics != null) {
            if (level == HitLevel.L1) {
                metrics.recordL1Hit(name);
            } else {
                metrics.recordL2Hit(name);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Cache hit: cache={}, level={}", name, level);
        }
    }

    /**
     * 记录缓存未命中
     */
    private void recordMiss() {
        if (metrics != null) {
            metrics.recordMiss(name);
        }
        if (log.isTraceEnabled()) {
            log.trace("Cache miss: cache={}", name);
        }
    }

    /**
     * 记录布隆过滤器快速过滤
     *
     * <p>当布隆过滤器判断 key "不存在"时调用，用于监控布隆过滤器的过滤效果。
     */
    private void recordBloomFilterFiltered() {
        if (metrics != null) {
            metrics.recordBloomFilterFiltered(name);
        }
        if (log.isTraceEnabled()) {
            log.trace("Bloom filter filtered: cache={}", name);
        }
    }

    /**
     * 记录布隆过滤器误判（False Positive）
     *
     * <p>当布隆过滤器判断 key "不存在"但 L2 实际命中时调用，用于监控布隆过滤器的误判率。
     */
    private void recordBloomFilterFalsePositive() {
        if (metrics != null) {
            metrics.recordBloomFilterFalsePositive(name);
        }
        if (log.isDebugEnabled()) {
            log.debug("Bloom filter false positive detected: cache={}", name);
        }
    }

    /**
     * 订阅本地缓存失效事件
     *
     * <p>当收到其他节点的缓存失效消息时，失效本地 L1 缓存。
     * 此方法由 Spring 事件机制自动调用。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>检查事件中的 cacheName 是否匹配当前缓存</li>
     *   <li>如果匹配，失效本地 L1 缓存</li>
     *   <li>记录日志</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果失效操作失败，记录警告日志但不抛出异常</li>
     * </ul>
     *
     * @param event 本地缓存失效事件
     */
    @EventListener
    public void onLocalCacheEviction(LocalCacheEvictionEvent event) {
        // 只处理匹配当前缓存的事件
        if (!name.equals(event.getCacheName())) {
            return;
        }

        try {
            l1.evict(event.getKey());
            if (log.isTraceEnabled()) {
                log.trace("Evicted local L1 cache due to remote eviction event: cacheName={}, key={}",
                        event.getCacheName(), event.getKey());
            }
        } catch (Exception e) {
            log.warn("Failed to evict local L1 cache due to remote eviction event: cacheName={}, key={}",
                    event.getCacheName(), event.getKey(), e);
        }
    }

    /**
     * 检查并失效 L2 缓存（如果存在）
     *
     * <p>用于延迟检查机制，确保 L2 缓存已失效。
     * 如果 L2 中仍然存在该 key，则主动失效。
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>发布失效消息后，延迟检查 L2 是否已失效</li>
     *   <li>如果未失效，主动失效（兜底机制）</li>
     * </ul>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果检查失败，记录警告日志但不抛出异常</li>
     * </ul>
     *
     * @param key 缓存键
     * @return true 如果 L2 中存在该 key 并已失效，false 如果不存在或检查失败
     */
    public boolean checkAndEvictL2IfPresent(Object key) {
        if (key == null) {
            return false;
        }

        try {
            // 检查 L2 是否存在该 key
            ValueWrapper l2Value = l2.get(key);
            if (l2Value != null) {
                // L2 中仍然存在，主动失效
                l2.evict(key);
                if (log.isDebugEnabled()) {
                    log.debug("Evicted L2 cache during delayed check: cacheName={}, key={}", name, key);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check and evict L2 cache: cacheName={}, key={}", name, key, e);
            return false;
        }
    }

    /**
     * 缓存命中级别
     */
    private enum HitLevel {
        L1, L2
    }
}

