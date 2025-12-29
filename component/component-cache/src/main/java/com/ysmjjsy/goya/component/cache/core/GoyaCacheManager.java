package com.ysmjjsy.goya.component.cache.core;

import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.factory.CacheFactory;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.ttl.FallbackStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
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
 *   <li>委托给 CacheFactory 创建缓存实例</li>
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
 *       <li>委托给 CacheFactory 创建缓存实例</li>
 *       <li>返回 GoyaCache 实例</li>
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
 *   <li>如果缓存创建失败，抛出 {@link RuntimeException}</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:45
 */
@Slf4j
public class GoyaCacheManager implements CacheManager {

    /**
     * Cache 实例 Map
     * Key: cacheName
     * Value: GoyaCache 实例（类型擦除，实际是 GoyaCache<?, ?>）
     */
    private final ConcurrentHashMap<String, GoyaCache<?, ?>> caches = new ConcurrentHashMap<>();

    /**
     * 缓存工厂
     */
    private final CacheFactory cacheFactory;

    /**
     * 构造函数
     *
     * @param cacheFactory 缓存工厂
     * @throws IllegalArgumentException 如果 cacheFactory 为 null
     */
    public GoyaCacheManager(CacheFactory cacheFactory) {
        if (cacheFactory == null) {
            throw new IllegalArgumentException("CacheFactory cannot be null");
        }
        this.cacheFactory = cacheFactory;
    }

    @Override
    public Cache getCache(@NonNull String name) {
        return caches.computeIfAbsent(name, this::createCache);
    }

    @Override
    @NullMarked
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableCollection(caches.keySet());
    }

    /**
     * 创建 GoyaCache 实例
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>委托给 CacheFactory 创建缓存实例</li>
     *   <li>返回 GoyaCache 实例（类型擦除，实际是 GoyaCache<?, ?>）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果配置解析失败，抛出 {@link IllegalStateException}</li>
     *   <li>如果缓存创建失败，抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param name 缓存名称
     * @return GoyaCache 实例（类型擦除）
     * @throws IllegalStateException 如果配置解析失败
     * @throws RuntimeException      如果 Cache 创建失败
     */
    private GoyaCache<?, ?> createCache(String name) {
        try {
            // CacheFactory 返回的是 GoyaCache（类型擦除），实际是 GoyaCache<?, ?>
            return cacheFactory.createCache(name);
        } catch (IllegalStateException e) {
            log.error("Failed to create GoyaCache due to configuration error: {}", name, e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Failed to create GoyaCache due to invalid argument: {}", name, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to create GoyaCache for: {}", name, e);
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
         * @param spec      缓存配置规范
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
         * @param spec      缓存配置规范
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