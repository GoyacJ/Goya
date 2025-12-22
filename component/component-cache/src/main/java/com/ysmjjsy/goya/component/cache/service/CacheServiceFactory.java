package com.ysmjjsy.goya.component.cache.service;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>缓存服务工厂</p>
 * <p>提供创建各种缓存服务实例的工厂方法</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     private final LocalCacheService localCache;
 *     private final RemoteCacheService remoteCache;
 *
 *     public UserService(CacheServiceFactory factory) {
 *         // 创建本地缓存实例
 *         this.localCache = factory.createLocal();
 *
 *         // 创建远程缓存实例
 *         this.remoteCache = factory.createRemote();
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see LocalCacheService
 * @see RemoteCacheService
 * @see HybridCacheService
 */
@Slf4j
public class CacheServiceFactory {

    private final CacheProperties properties;
    private final IL2Cache l2Cache;
    private final ICacheInvalidatePublisher publisher;

    public CacheServiceFactory(
            CacheProperties properties,
            IL2Cache l2Cache,
            ICacheInvalidatePublisher publisher) {
        this.properties = properties;
        this.l2Cache = l2Cache;
        this.publisher = publisher;
    }

    /**
     * 创建本地缓存服务
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>高频访问的数据</li>
     *     <li>不需要跨节点共享</li>
     *     <li>对一致性要求不高</li>
     * </ul>
     *
     * @return LocalCacheService 实例
     */
    public LocalCacheService createLocal() {
        LocalCacheService service = new LocalCacheService(properties);
        log.debug("[Goya] |- Cache |- LocalCacheService created via factory");
        return service;
    }

    /**
     * 创建远程缓存服务
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>需要跨节点共享</li>
     *     <li>强一致性要求</li>
     *     <li>大数据量</li>
     * </ul>
     *
     * @return RemoteCacheService 实例
     * @throws IllegalStateException 如果没有 L2 缓存实现
     */
    public RemoteCacheService createRemote() {
        if (l2Cache == null) {
            throw new IllegalStateException(
                    "No remote cache implementation (IL2Cache) found. " +
                            "Please add a remote cache starter (e.g., redis-boot-starter)."
            );
        }
        RemoteCacheService service = new RemoteCacheService(properties, l2Cache);
        log.debug("[Goya] |- Cache |- RemoteCacheService created via factory, type: {}",
                l2Cache.getCacheType());
        return service;
    }

    /**
     * 创建混合缓存服务（多级缓存）
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>通用场景（默认推荐）</li>
     *     <li>兼顾性能和一致性</li>
     * </ul>
     *
     * @return HybridCacheService 实例
     */
    public HybridCacheService createHybrid() {
        RemoteCacheService remote = l2Cache != null
                ? new RemoteCacheService(properties, l2Cache)
                : null;
        HybridCacheService service = new HybridCacheService(properties, remote, publisher);
        log.debug("[Goya] |- Cache |- HybridCacheService created via factory");
        return service;
    }
}

