package com.ysmjjsy.goya.component.cache.core;

import com.ysmjjsy.goya.component.cache.enums.ConsistencyLevel;
import com.ysmjjsy.goya.component.cache.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.event.LocalCacheEvictionEvent;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.support.SingleFlightLoader;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
     * 缓存事件发布器
     */
    private final CacheEventPublisher eventPublisher;

    /**
     * 监控指标
     */
    private final CacheMetrics metrics;

    /**
     * 缓存编排器（负责 L1/L2 编排逻辑）
     */
    private final CacheOrchestrator orchestrator;

    /**
     * 构造函数
     *
     * <p>创建 GoyaCache 实例，并初始化 CacheOrchestrator 负责编排逻辑。
     * GoyaCache 专注于 Spring Cache 适配，编排逻辑委托给 CacheOrchestrator。
     */
    public GoyaCache(
            String name,
            LocalCache l1,
            RemoteCache l2,
            CacheSpecification spec,
            BloomFilterManager bloomFilter,
            CacheRefillManager refillManager,
            CacheEventPublisher eventPublisher,
            FallbackStrategy fallbackStrategy,
            CacheMetrics metrics,
            SingleFlightLoader singleFlightLoader) {
        this.name = name;
        this.l1 = l1;
        this.l2 = l2;
        this.spec = spec;
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
        // 初始化编排器，将编排逻辑委托给它
        this.orchestrator = new CacheOrchestrator(
                name, l1, l2, spec, bloomFilter, refillManager,
                fallbackStrategy, metrics, singleFlightLoader);
    }

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
     * <p>委托给 CacheOrchestrator 执行编排逻辑。
     *
     * @param key 缓存键
     * @return ValueWrapper，如果不存在则返回 null
     */
    public ValueWrapper getTyped(K key) {
        return orchestrator.get(key);
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
    public <T> T get(@NonNull Object key, @NonNull Callable<T> loader) {
        // Spring Cache 标准方法：支持缓存穿透保护和 SingleFlight 机制
        @SuppressWarnings("unchecked")
        K typedKey = (K) key;
        ValueWrapper existing = getTyped(typedKey);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            T value = (T) existing.get();
            return value;
        }

        // 委托给 orchestrator，使用 SingleFlight 机制防止缓存击穿
        long sourceLoadStartNanos = System.nanoTime();
        T value;
        try {
            value = orchestrator.get(typedKey, loader);
        } catch (Exception e) {
            long sourceLoadDurationNanos = System.nanoTime() - sourceLoadStartNanos;
            if (metrics != null) {
                metrics.recordSourceLoad(name, sourceLoadDurationNanos);
            }
            throw new ValueRetrievalException(key, loader, e);
        }
        long sourceLoadDurationNanos = System.nanoTime() - sourceLoadStartNanos;
        if (metrics != null) {
            metrics.recordSourceLoad(name, sourceLoadDurationNanos);
        }

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
        putTyped(key, value, spec.getTtl());
    }

    /**
     * 类型安全的写入方法（带自定义 TTL）
     *
     * <p>使用指定的 TTL 写入缓存。根据配置的一致性等级决定写入策略：
     * <ul>
     *   <li><b>STRONG</b>：L2 写入成功 + L1 写入成功 + 跨节点同步完成（带超时）</li>
     *   <li><b>EVENTUAL</b>（默认）：L2 写入成功，L1 和跨节点同步异步执行</li>
     *   <li><b>BEST_EFFORT</b>：写入失败时降级，不保证一致性</li>
     * </ul>
     *
     * <p><b>一致性保证：</b>
     * <ul>
     *   <li>STRONG：如果 L2 或 L1 写入失败，抛出异常；等待跨节点同步完成</li>
     *   <li>EVENTUAL：如果 L2 写入成功，L1 写入失败不影响主流程；不等待跨节点同步</li>
     *   <li>BEST_EFFORT：写入失败时根据降级策略处理，不抛出异常</li>
     * </ul>
     *
     * <p>注意：Caffeine 本地缓存可能不支持每个 key 独立的 TTL，会使用全局策略。
     * 但为了接口一致性，仍然传入 ttl 参数。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @throws IllegalArgumentException 如果 ttl 为 null 或无效
     * @throws RuntimeException 如果一致性等级为 STRONG 且写入失败
     */
    public void putTyped(K key, V value, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        ConsistencyLevel consistencyLevel = spec.getConsistencyLevel();
        putTyped(key, value, ttl, consistencyLevel);
    }

    /**
     * 类型安全的写入方法（带自定义 TTL 和一致性等级）
     *
     * <p>委托给 CacheOrchestrator 执行编排逻辑。
     * 注意：写入操作不发布失效事件，只有失效操作（evict）才发布失效事件。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @param consistencyLevel 一致性等级
     * @throws IllegalArgumentException 如果 ttl 为 null 或无效
     * @throws RuntimeException 如果一致性等级为 STRONG 且写入失败
     */
    public void putTyped(K key, V value, Duration ttl, ConsistencyLevel consistencyLevel) {
        // 委托给 orchestrator 执行编排逻辑
        orchestrator.put(key, value, ttl, consistencyLevel);
        // 注意：写入操作不应该发布失效事件，否则会导致刚写入的缓存被清除
        // 只有失效操作（evict）才应该发布失效事件
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
     * <p>委托给 CacheOrchestrator 执行编排逻辑，然后发布事件。
     *
     * @param key 缓存键
     */
    public void evictTyped(K key) {
        // 委托给 orchestrator 执行编排逻辑
        orchestrator.evict(key);
        // 发布失效事件（Spring Cache 适配职责）
        eventPublisher.publishEviction(name, key);
    }

    @Override
    public void clear() {
        // 委托给 orchestrator 执行编排逻辑
        orchestrator.clear();
        // 发布清空事件（Spring Cache 适配职责）
        eventPublisher.publishClear(name);
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
     * 批量获取缓存值（类型安全）
     *
     * <p>利用 L1/L2 的批量 API 提升性能，减少网络往返次数。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>批量查询 L1，获取命中的 key-value 对</li>
     *   <li>找出 L1 未命中的 key</li>
     *   <li>批量查询 L2（使用批量 API，如 Redis MGET）</li>
     *   <li>异步回填 L1（对于 L2 命中的 key）</li>
     *   <li>合并 L1 和 L2 的结果并返回</li>
     * </ol>
     *
     * <p><b>性能优化：</b>
     * <ul>
     *   <li>使用 L1.getAll() 批量查询本地缓存</li>
     *   <li>使用 L2.getAll() 批量查询远程缓存（减少网络往返）</li>
     *   <li>异步回填 L1，不阻塞主流程</li>
     * </ul>
     *
     * @param keys 缓存键集合
     * @return key-value 映射，只包含命中的 key
     */
    @SuppressWarnings("unchecked")
    public Map<K, V> batchGetTyped(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        // 过滤 null key
        Set<Object> validKeys = keys.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (validKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        // 委托给 orchestrator 执行编排逻辑
        Map<Object, ValueWrapper> wrapperResults = orchestrator.batchGet(validKeys);

        // 转换为类型安全的 Map<K, V>
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<Object, ValueWrapper> entry : wrapperResults.entrySet()) {
            @SuppressWarnings("unchecked")
            K key = (K) entry.getKey();
            ValueWrapper wrapper = entry.getValue();
            if (wrapper != null) {
                Object value = wrapper.get();
                if (value != null) {
                    @SuppressWarnings("unchecked")
                    V typedValue = (V) value;
                    result.put(key, typedValue);
                }
            }
        }

        return result;
    }

    /**
     * 批量写入缓存（类型安全，使用配置的默认 TTL）
     *
     * <p>利用 L1/L2 的批量 API 提升性能，减少网络往返次数。
     * 使用缓存配置的默认 TTL。
     *
     * @param entries 键值对映射
     */
    public void batchPutTyped(Map<K, V> entries) {
        batchPutTyped(entries, spec.getTtl());
    }

    /**
     * 批量写入缓存（类型安全，带自定义 TTL）
     *
     * <p>利用 L1/L2 的批量 API 提升性能，减少网络往返次数。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>包装 null 值（如果允许）</li>
     *   <li>批量写入 L2（使用批量 API，如 Redis Pipeline）</li>
     *   <li>如果 L2 写入成功，批量写入 L1</li>
     *   <li>更新布隆过滤器（异步）</li>
     * </ol>
     *
     * <p><b>性能优化：</b>
     * <ul>
     *   <li>使用 L2.putAll() 批量写入远程缓存（减少网络往返）</li>
     *   <li>使用 L1.putAll() 批量写入本地缓存</li>
     *   <li>异步更新布隆过滤器，不阻塞主流程</li>
     * </ul>
     *
     * @param entries 键值对映射
     * @param ttl 过期时间
     */
    public void batchPutTyped(Map<K, V> entries, Duration ttl) {
        // 委托给 orchestrator 执行编排逻辑
        @SuppressWarnings("unchecked")
        Map<Object, Object> objectEntries = (Map<Object, Object>) (Map<?, ?>) entries;
        orchestrator.batchPut(objectEntries, ttl);
        // 注意：写入操作不应该发布失效事件，否则会导致刚写入的缓存被清除
        // 只有失效操作（evict）才应该发布失效事件
    }

    /**
     * 批量失效缓存（类型安全）
     *
     * <p>批量失效多个 key，提升性能。虽然 L1/L2 接口没有批量失效方法，
     * 但可以通过批量发布事件来优化跨节点同步。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>循环失效 L1（同步）</li>
     *   <li>循环失效 L2（同步）</li>
     *   <li>批量发布失效事件（其他节点会收到通知）</li>
     * </ol>
     *
     * <p><b>性能优化：</b>
     * <ul>
     *   <li>虽然 L1/L2 接口没有批量失效方法，但批量发布事件可以减少事件发布次数</li>
     *   <li>未来如果 L1/L2 接口支持批量失效，可以进一步优化</li>
     * </ul>
     *
     * <p><b>注意：</b>
     * <ul>
     *   <li>当前实现仍然循环调用单个失效操作，因为 LocalCache 和 RemoteCache 接口没有批量失效方法</li>
     *   <li>如果某个 key 失效失败，记录日志但继续处理其他 key</li>
     * </ul>
     *
     * @param keys 缓存键集合
     */
    public void batchEvictTyped(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        // 过滤 null key
        Set<K> validKeys = keys.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (validKeys.isEmpty()) {
            return;
        }

        // 1. 批量失效 L1
        for (K key : validKeys) {
            try {
                l1.evict(key);
            } catch (Exception e) {
                log.warn("Failed to evict from L1 cache for key: {}", key, e);
            }
        }

        // 2. 批量失效 L2
        for (K key : validKeys) {
            try {
                l2.evict(key);
            } catch (Exception e) {
                log.error("Failed to evict from L2 cache for key: {}", key, e);
            }
        }

        // 3. 批量发布失效事件（每个 key 发布一个事件）
        // 注意：当前 CacheEventPublisher 没有批量发布方法，所以仍然循环发布
        // 未来可以考虑添加批量发布方法，减少事件发布次数
        for (K key : validKeys) {
            eventPublisher.publishEviction(name, key);
        }
    }
}

