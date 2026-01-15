package com.ysmjjsy.goya.component.cache.multilevel.core;

import com.ysmjjsy.goya.component.cache.core.definition.ICache;
import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * <p>多级缓存实现</p>
 * <p>实现 L1（本地缓存）+ L2（远程缓存）的多级缓存策略</p>
 * <p>读取流程：L1 → L2 → DB（回写 L1）</p>
 * <p>写入流程：同时写入 L1 和 L2</p>
 * <p>删除流程：同时删除 L1 和 L2</p>
 *
 * @author goya
 * @since 2026/1/15 11:26
 */
@Slf4j
public class MultiLevelCache extends AbstractValueAdaptingCache implements ICache<Object, Object> {

    /**
     * 缓存名称
     */
    private final String cacheName;

    /**
     * L1 本地缓存
     */
    private final LocalCache<Object, Object> localCache;

    /**
     * L2 远程缓存
     */
    private final RemoteCache<Object, Object> remoteCache;

    /**
     * 多级缓存编排器
     */
    private final MultiLevelCacheOrchestrator orchestrator;

    /**
     * 缓存配置规范
     */
    private final MultiLevelCacheSpec spec;

    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param cacheName    缓存名称
     * @param localCache   L1 本地缓存
     * @param remoteCache  L2 远程缓存
     * @param orchestrator 多级缓存编排器
     * @param spec         缓存配置规范
     */
    public MultiLevelCache(
            String cacheName,
            LocalCache<Object, Object> localCache,
            RemoteCache<Object, Object> remoteCache,
            MultiLevelCacheOrchestrator orchestrator,
            MultiLevelCacheSpec spec) {
        super(Boolean.TRUE.equals(spec.allowNullValues()));
        this.cacheName = cacheName;
        this.localCache = localCache;
        this.remoteCache = remoteCache;
        this.orchestrator = orchestrator;
        this.spec = spec;
    }

    @Override
    @NullMarked
    public String getName() {
        return cacheName;
    }

    @Override
    @NullMarked
    public Object getNativeCache() {
        return this;
    }

    @Override
    @NullMarked
    protected @Nullable Object lookup(Object key) {
        try {
            // 1. 检查布隆过滤器，防止缓存穿透
            if (Boolean.TRUE.equals(spec.enableBloomFilter()) && !orchestrator.mightContain(cacheName, key)) {
                if (log.isTraceEnabled()) {
                    log.trace("Bloom filter prevented cache penetration for key: {}", key);
                }
                return null;
            }

            // 2. 查询 L1 本地缓存
            Object value = localCache.get(key);
            if (value != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Cache hit in L1: cacheName={}, key={}", cacheName, key);
                }
                return value;
            }

            // 3. L1 未命中，查询 L2 远程缓存
            value = remoteCache.get(key);
            if (value != null) {
                // 回写 L1
                localCache.put(key, value);
                if (log.isTraceEnabled()) {
                    log.trace("Cache hit in L2, write back to L1: cacheName={}, key={}", cacheName, key);
                }
                return value;
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to lookup cache: cacheName={}, key={}", cacheName, key, e);
            // 对于可恢复的异常（如网络超时），返回 null 允许降级
            // 对于不可恢复的异常，应该抛出异常，但这里为了兼容性返回 null
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @NullMarked
    public <T> T get(Object key, Callable<T> valueLoader) {

        try {
            // 1. 先查 L1（本地缓存）
            Object value = localCache.get(key);
            if (value != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Cache hit in L1: cacheName={}, key={}", cacheName, key);
                }
                return (T) value;
            }

            // 2. L1 未命中，查 L2（远程缓存）
            value = remoteCache.get(key);
            if (value != null) {
                // 回写 L1
                localCache.put(key, value);
                if (log.isTraceEnabled()) {
                    log.trace("Cache hit in L2, write back to L1: cacheName={}, key={}", cacheName, key);
                }
                return (T) value;
            }

            // 3. L2 未命中，尝试获取分布式锁（防止缓存击穿）
            boolean lockAcquired = false;
            try {
                // 尝试获取锁，等待 100ms，持有 10 秒（防止死锁）
                lockAcquired = orchestrator.tryLock(cacheName, key, 100, 10);
                if (lockAcquired) {
                    // 获取锁后，再次检查缓存（双重检查）
                    value = localCache.get(key);
                    if (value != null) {
                        if (log.isTraceEnabled()) {
                            log.trace("Cache hit in L1 after lock (double-check): cacheName={}, key={}", cacheName, key);
                        }
                        return (T) value;
                    }

                    value = remoteCache.get(key);
                    if (value != null) {
                        localCache.put(key, value);
                        if (log.isTraceEnabled()) {
                            log.trace("Cache hit in L2 after lock (double-check): cacheName={}, key={}", cacheName, key);
                        }
                        return (T) value;
                    }
                    // 获取到锁且缓存仍未命中，继续执行加载逻辑
                } else {
                    // 未获取到锁，说明其他线程正在加载，等待一段时间后再次查询缓存
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting for lock: cacheName={}, key={}", cacheName, key);
                    }
                    value = localCache.get(key);
                    if (value != null) {
                        if (log.isTraceEnabled()) {
                            log.trace("Cache hit in L1 after lock wait: cacheName={}, key={}", cacheName, key);
                        }
                        return (T) value;
                    }
                    value = remoteCache.get(key);
                    if (value != null) {
                        localCache.put(key, value);
                        if (log.isTraceEnabled()) {
                            log.trace("Cache hit in L2 after lock wait: cacheName={}, key={}", cacheName, key);
                        }
                        return (T) value;
                    }
                    // 如果仍然没有，返回 null（避免重复加载，让调用者重试或使用 valueLoader）
                    if (log.isTraceEnabled()) {
                        log.trace("Lock contention, other thread is loading, returning null: cacheName={}, key={}", cacheName, key);
                    }
                    return null;
                }
            } catch (Exception _) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while trying to acquire lock: cacheName={}, key={}", cacheName, key);
                // 中断时返回 null，避免继续执行
                return null;
            } finally {
                if (lockAcquired) {
                    orchestrator.unlock(cacheName, key);
                }
            }

            // 4. L2 未命中且获取到锁，调用 valueLoader 加载数据
            if (log.isTraceEnabled()) {
                log.trace("Cache miss, loading from source: cacheName={}, key={}", cacheName, key);
            }

            // 检查布隆过滤器（如果启用）
            if (Boolean.TRUE.equals(spec.enableBloomFilter())) {
                if (!orchestrator.mightContain(cacheName, key)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Bloom filter indicates key not exists: cacheName={}, key={}", cacheName, key);
                    }
                    return null;
                }
            }

            T loadedValue = valueLoader.call();

            // 5. 加载后同时写入 L1 和 L2
            put(key, loadedValue);
            // 更新布隆过滤器
            if (Boolean.TRUE.equals(spec.enableBloomFilter())) {
                orchestrator.put(cacheName, key);
            }

            return loadedValue;
        } catch (Exception e) {
            log.error("Failed to get cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to get cache", e);
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {

        try {
            // 同时写入 L1 和 L2
            localCache.put(key, value);
            remoteCache.put(key, value);

            // 更新布隆过滤器
            if (Boolean.TRUE.equals(spec.enableBloomFilter())) {
                orchestrator.put(cacheName, key);
            }

            // 如果启用失效通知，发布失效消息（通知其他节点删除 L1）
            if (Boolean.TRUE.equals(spec.enableInvalidationNotify())) {
                orchestrator.publishInvalidation(cacheName, key);
            }

            if (log.isTraceEnabled()) {
                log.trace("Put cache to L1 and L2: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to put cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to put cache", e);
        }
    }

    @Override
    public void delete(Object key) {
        evict(key);
    }

    @Override
    @NullMarked
    public void evict(Object key) {

        try {
            // 同时删除 L1 和 L2
            localCache.delete(key);
            remoteCache.delete(key);

            // 如果启用失效通知，发布失效消息
            if (Boolean.TRUE.equals(spec.enableInvalidationNotify())) {
                orchestrator.publishInvalidation(cacheName, key);
            }

            if (log.isTraceEnabled()) {
                log.trace("Evict cache from L1 and L2: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to evict cache", e);
        }
    }

    @Override
    public void clear() {
        try {
            // 清空 L1 和 L2
            localCache.clear();
            remoteCache.clear();

            // 如果启用失效通知，发布清空消息
            if (Boolean.TRUE.equals(spec.enableInvalidationNotify())) {
                // 注意：清空操作通常不需要发布失效消息，因为已经清空了所有缓存
                // 如果需要通知其他节点清空，可以扩展 CacheInvalidationPublisher 接口
            }

            if (log.isDebugEnabled()) {
                log.debug("Clear cache: cacheName={}", cacheName);
            }
        } catch (Exception e) {
            log.error("Failed to clear cache: cacheName={}", cacheName, e);
            throw new CacheException("Failed to clear cache", e);
        }
    }

    @Override
    public boolean exists(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            // 先查 L1
            if (localCache.exists(key)) {
                return true;
            }

            // L1 未命中，查 L2
            return remoteCache.exists(key);
        } catch (Exception e) {
            log.error("Failed to check cache exists: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to check cache exists", e);
        }
    }
}
