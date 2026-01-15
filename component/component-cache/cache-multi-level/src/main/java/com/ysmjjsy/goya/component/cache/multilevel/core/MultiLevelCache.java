package com.ysmjjsy.goya.component.cache.multilevel.core;

import com.ysmjjsy.goya.component.cache.core.definition.ICache;
import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @param cacheName      缓存名称
     * @param localCache    L1 本地缓存
     * @param remoteCache   L2 远程缓存
     * @param orchestrator  多级缓存编排器
     * @param spec          缓存配置规范
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
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    protected @Nullable Object lookup(Object key) {
        if (key == null) {
            return null;
        }

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
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

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

            // 3. L2 未命中，调用 valueLoader 加载数据
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

            // 4. 加载后同时写入 L1 和 L2
            if (loadedValue != null || Boolean.TRUE.equals(spec.allowNullValues())) {
                put(key, loadedValue);
                // 更新布隆过滤器
                if (Boolean.TRUE.equals(spec.enableBloomFilter())) {
                    orchestrator.put(cacheName, key);
                }
            }

            return loadedValue;
        } catch (Exception e) {
            log.error("Failed to get cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to get cache", e);
        }
    }

    @Override
    public Object get(Object key) {
        return get(key, () -> null);
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

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
    public void evict(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

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
