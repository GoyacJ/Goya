package com.ysmjjsy.goya.component.cache.core;

import com.ysmjjsy.goya.component.cache.event.CacheEventPublisher;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import com.ysmjjsy.goya.component.cache.metrics.CacheMetrics;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.support.CacheRefillManager;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GoyaCache 的 CacheManager 实现
 *
 * <p>实现 Spring Cache 的 {@link CacheManager} 接口，管理所有 {@link GoyaCache} 实例。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>管理所有 GoyaCache 实例（按 cacheName）</li>
 *   <li>延迟创建 GoyaCache 实例（首次访问时创建）</li>
 *   <li>从配置解析器获取 CacheSpecification</li>
 *   <li>通过工厂创建 LocalCache 和 RemoteCache</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>实现 {@link CacheManager} 接口，完全兼容 Spring Cache SPI</li>
 *   <li>由 Spring Boot 自动配置注册为 Bean</li>
 *   <li>Spring Cache 拦截器通过 {@link CacheManager#getCache(String)} 获取 Cache 实例</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li><b>getCache(String name)：</b>
 *     <ol>
 *       <li>从缓存 Map 中查找（computeIfAbsent）</li>
 *       <li>如果不存在，调用 createCache() 创建</li>
 *       <li>返回 GoyaCache 实例</li>
 *     </ol>
 *   </li>
 *   <li><b>createCache(String name)：</b>
 *     <ol>
 *       <li>从配置解析器获取 CacheSpecification</li>
 *       <li>通过工厂创建 LocalCache 和 RemoteCache</li>
 *       <li>创建 GoyaCache 实例并返回</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储 Cache 实例，线程安全</li>
 *   <li>使用 computeIfAbsent 确保同一 cacheName 只创建一个实例</li>
 *   <li>Cache 实例创建后不可变</li>
 * </ul>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>如果配置解析失败，抛出 {@link IllegalStateException}</li>
 *   <li>如果 LocalCache 或 RemoteCache 创建失败，抛出 {@link RuntimeException}</li>
 *   <li>如果 GoyaCache 创建失败，抛出 {@link RuntimeException}</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:45
 */

public class GoyaCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(GoyaCacheManager.class);

    /**
     * Cache 实例 Map
     * Key: cacheName
     * Value: GoyaCache 实例
     */
    private final ConcurrentHashMap<String, GoyaCache> caches = new ConcurrentHashMap<>();

    /**
     * 配置规范解析器
     */
    private final CacheSpecificationResolver specificationResolver;

    /**
     * 本地缓存工厂
     */
    private final LocalCacheFactory localCacheFactory;

    /**
     * 远程缓存工厂
     */
    private final RemoteCacheFactory remoteCacheFactory;

    /**
     * 布隆过滤器管理器
     */
    private final BloomFilterManager bloomFilterManager;

    /**
     * 缓存回填管理器
     */
    private final CacheRefillManager refillManager;

    /**
     * 缓存事件发布器
     */
    private final CacheEventPublisher eventPublisher;

    /**
     * 降级策略工厂
     */
    private final FallbackStrategyFactory fallbackStrategyFactory;

    /**
     * 监控指标（可选）
     */
    private final CacheMetrics metrics;

    /**
     * 构造函数
     *
     * @param specificationResolver 配置规范解析器
     * @param localCacheFactory 本地缓存工厂
     * @param remoteCacheFactory 远程缓存工厂
     * @param bloomFilterManager 布隆过滤器管理器
     * @param refillManager 缓存回填管理器
     * @param eventPublisher 缓存事件发布器
     * @param fallbackStrategyFactory 降级策略工厂
     * @param metrics 监控指标（可选，如果为 null 则不记录指标）
     * @throws IllegalArgumentException 如果任何参数为 null
     */
    public GoyaCacheManager(CacheSpecificationResolver specificationResolver,
                            LocalCacheFactory localCacheFactory,
                            RemoteCacheFactory remoteCacheFactory,
                            BloomFilterManager bloomFilterManager,
                            CacheRefillManager refillManager,
                            CacheEventPublisher eventPublisher,
                            FallbackStrategyFactory fallbackStrategyFactory,
                            CacheMetrics metrics) {
        if (specificationResolver == null || localCacheFactory == null || remoteCacheFactory == null) {
            throw new IllegalArgumentException("SpecificationResolver, LocalCacheFactory, and RemoteCacheFactory cannot be null");
        }
        if (bloomFilterManager == null || refillManager == null || eventPublisher == null || fallbackStrategyFactory == null) {
            throw new IllegalArgumentException("BloomFilterManager, RefillManager, EventPublisher, and FallbackStrategyFactory cannot be null");
        }
        this.specificationResolver = specificationResolver;
        this.localCacheFactory = localCacheFactory;
        this.remoteCacheFactory = remoteCacheFactory;
        this.bloomFilterManager = bloomFilterManager;
        this.refillManager = refillManager;
        this.eventPublisher = eventPublisher;
        this.fallbackStrategyFactory = fallbackStrategyFactory;
        this.metrics = metrics; // 可以为 null
    }

    @Override
    public Cache getCache(@NonNull String name) {
        return caches.computeIfAbsent(name, this::createCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableCollection(caches.keySet());
    }

    /**
     * 创建 GoyaCache 实例
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>从配置解析器获取 CacheSpecification</li>
     *   <li>通过工厂创建 LocalCache 和 RemoteCache</li>
     *   <li>创建降级策略实例</li>
     *   <li>创建 GoyaCache 实例并返回</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果配置解析失败，抛出 {@link IllegalStateException}</li>
     *   <li>如果 LocalCache 或 RemoteCache 创建失败，抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param name 缓存名称
     * @return GoyaCache 实例
     * @throws IllegalStateException 如果配置解析失败
     * @throws RuntimeException 如果 Cache 创建失败
     */
    private GoyaCache createCache(String name) {
        try {
            // 1. 解析配置
            CacheSpecification spec = specificationResolver.resolve(name);
            if (spec == null) {
                throw new IllegalStateException("Failed to resolve cache specification for: " + name);
            }

            // 2. 创建 LocalCache 和 RemoteCache
            LocalCache l1 = localCacheFactory.create(name, spec);
            RemoteCache l2 = remoteCacheFactory.create(name, spec);

            if (l1 == null || l2 == null) {
                throw new IllegalStateException("Failed to create LocalCache or RemoteCache for: " + name);
            }

            // 3. 创建降级策略
            FallbackStrategy fallbackStrategy = fallbackStrategyFactory.create(spec.getFallbackStrategyType());

            // 4. 创建 GoyaCache
            GoyaCache cache = new GoyaCache(name, l1, l2, spec, bloomFilterManager, refillManager, eventPublisher, fallbackStrategy, metrics);

            log.info("Created GoyaCache: name={}, ttl={}, localMaxSize={}, bloomFilterEnabled={}",
                    name, spec.getTtl(), spec.getLocalMaxSize(), spec.isEnableBloomFilter());

            return cache;
        } catch (IllegalStateException e) {
            // 配置错误，直接抛出，不包装
            log.error("Failed to create GoyaCache due to configuration error: " + name, e);
            throw e;
        } catch (IllegalArgumentException e) {
            // 参数错误，直接抛出，不包装
            log.error("Failed to create GoyaCache due to invalid argument: " + name, e);
            throw e;
        } catch (Exception e) {
            // 其他异常，包装后抛出，保留原始异常
            log.error("Failed to create GoyaCache for: " + name, e);
            throw new CacheException("Failed to create GoyaCache for: " + name, e);
        }
    }

    /**
     * 本地缓存工厂接口
     */
    public interface LocalCacheFactory {
        /**
         * 创建本地缓存实例
         *
         * @param cacheName 缓存名称
         * @param spec 缓存配置规范
         * @return LocalCache 实例
         */
        LocalCache create(String cacheName, CacheSpecification spec);
    }

    /**
     * 远程缓存工厂接口
     */
    public interface RemoteCacheFactory {
        /**
         * 创建远程缓存实例
         *
         * @param cacheName 缓存名称
         * @param spec 缓存配置规范
         * @return RemoteCache 实例
         */
        RemoteCache create(String cacheName, CacheSpecification spec);
    }

    /**
     * 降级策略工厂接口
     */
    public interface FallbackStrategyFactory {
        /**
         * 创建降级策略实例
         *
         * @param type 降级策略类型
         * @return FallbackStrategy 实例
         */
        FallbackStrategy create(FallbackStrategy.Type type);
    }
}