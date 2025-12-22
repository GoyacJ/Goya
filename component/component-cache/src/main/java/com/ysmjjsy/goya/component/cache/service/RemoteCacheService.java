package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>远程缓存服务（IL2Cache 包装器）</p>
 * <p>特点：</p>
 * <ul>
 *     <li>分布式，多节点共享</li>
 *     <li>适合需要共享、强一致性的数据</li>
 *     <li>支持大数据量</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *     <li>需要跨节点共享</li>
 *     <li>强一致性要求</li>
 *     <li>大数据量</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class SharedConfigService {
 *     private final RemoteCacheService remoteCache;
 *
 *     public SharedConfigService(CacheServiceFactory factory) {
 *         // 多实例共享配置，只用远程缓存（强一致性）
 *         this.remoteCache = factory.createRemote();
 *     }
 *
 *     public Config getConfig(String key) {
 *         return remoteCache.get("sharedConfig", key, this::loadConfig);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see AbstractCacheService
 * @see IL2Cache
 */
@Slf4j
public class RemoteCacheService extends AbstractCacheService {

    private final IL2Cache delegate;

    public RemoteCacheService(CacheProperties properties, IL2Cache delegate) {
        super(properties);
        this.delegate = delegate;
    }

    /**
     * 获取远程缓存类型
     *
     * @return 缓存类型标识
     */
    public String getRemoteType() {
        return delegate.getCacheType();
    }

    /**
     * 检查远程缓存是否可用
     *
     * @return true 表示可用
     */
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    protected <K, V> V doGet(String cacheName, K key) {
        return delegate.get(cacheName, key);
    }

    @Override
    protected <K, V> V doGetOrLoad(String cacheName, K key, Function<? super K, ? extends V> mappingFunction) {
        return delegate.get(cacheName, key, mappingFunction);
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatch(String cacheName, Set<? extends K> keys) {
        return delegate.get(cacheName, keys);
    }

    @Override
    protected <K, V> Map<K, @NonNull V> doGetBatchOrLoad(
            String cacheName,
            Set<? extends K> keys,
            Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
        return delegate.get(cacheName, keys, mappingFunction);
    }

    @Override
    protected <K, V> void doPut(String cacheName, K key, V value, Duration duration) {
        delegate.put(cacheName, key, value, duration);
    }

    @Override
    protected <K> Boolean doRemove(String cacheName, K key) {
        return delegate.remove(cacheName, key);
    }

    @Override
    protected <K> void doRemoveBatch(String cacheName, Set<? extends K> keys) {
        delegate.remove(cacheName, keys);
    }

    @Override
    protected <K, V> V doComputeIfAbsent(String cacheName, K key, Function<K, V> loader) {
        return delegate.computeIfAbsent(cacheName, key, loader);
    }

    @Override
    protected <K> void doTryLock(String cacheName, K key, Duration expire) {
        delegate.tryLock(cacheName, key, expire);
    }

    @Override
    protected <K> boolean doLockAndRun(String cacheName, K key, Duration expire, Runnable action) {
        return delegate.lockAndRun(cacheName, key, expire, action);
    }

    @Override
    public <K> boolean mightContain(String cacheName, K key) {
        return delegate.mightContain(cacheName, key);
    }

    @Override
    public <K> void addToBloomFilter(String cacheName, K key) {
        delegate.addToBloomFilter(cacheName, key);
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        delegate.clearAll();
        log.info("[Goya] |- Cache |- Remote cache cleared");
    }
}

