package com.ysmjjsy.goya.component.cache.multilevel.template;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.multilevel.enums.CacheLevelEnum;
import com.ysmjjsy.goya.component.cache.multilevel.enums.ConsistencyLevelEnum;
import com.ysmjjsy.goya.component.cache.multilevel.factory.CacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecificationResolver;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.FallbackStrategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 抽象缓存管理器
 *
 * <p>管理特定 cacheName 的缓存实例，提供类型安全的缓存操作方法。
 * 支持配置动态刷新和生命周期托管。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>封装特定 cacheName 的缓存操作，简化业务代码</li>
 *   <li>支持配置动态刷新，无需重启应用</li>
 *   <li>支持生命周期管理，自动初始化和销毁</li>
 *   <li>支持自定义配置或使用系统默认配置</li>
 * </ul>
 *
 * <p><b>使用方式：</b>
 *
 * <p><b>方式1：使用系统默认配置</b>
 * <pre>{@code
 * @Component
 * public class UserCacheManager extends AbstractCacheTemplate<Long, User> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userCache";
 *     }
 *     // 不需要实现 getCacheSpecification()，使用系统默认配置
 * }
 * }</pre>
 *
 * <p><b>方式2：完全自定义配置</b>
 * <pre>{@code
 * @Component
 * public class CustomCacheManager extends AbstractCacheTemplate<String, Object> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "customCache";
 *     }
 *
 *     @Override
 *     protected CacheSpecification getCacheSpecification() {
 *         // 完全自定义配置
 *         return new CacheSpecification.Builder()
 *                 .ttl(Duration.ofMinutes(30))
 *                 .localMaxSize(5000)
 *                 .cacheLevel(CacheLevel.L1_L2)
 *                 .build();
 *     }
 * }
 * }</pre>
 *
 * <p><b>方式3：基于默认配置修改</b>
 * <pre>{@code
 * @Component
 * public class UserCacheManager extends AbstractCacheTemplate<Long, User> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userCache";
 *     }
 *
 *     @Override
 *     protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
 *         // 基于默认配置修改
 *         return new CacheSpecification.Builder(defaultSpec)
 *                 .ttl(Duration.ofHours(2))  // 覆盖 TTL
 *                 .localMaxSize(20000)      // 覆盖本地缓存大小
 *                 .build();
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>配置刷新使用 synchronized 保护</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/28 02:04
 */
@Slf4j
public abstract class AbstractCacheTemplate<K, V> implements InitializingBean, DisposableBean {

    /**
     * 缓存工厂（用于创建 GoyaCache 实例）
     */
    @Autowired(required = false)
    protected CacheFactory cacheFactory;

    /**
     * 配置解析器（用于获取系统默认配置）
     */
    @Autowired(required = false)
    protected CacheSpecificationResolver specificationResolver;

    /**
     * 当前缓存实例（volatile 保证可见性）
     */
    private volatile GoyaCache<K, V> cache;

    /**
     * 当前配置规范（用于配置刷新比较）
     */
    @Getter
    private volatile CacheSpecification currentSpec;

    /**
     * 配置刷新锁（保证刷新操作的原子性）
     */
    private final Object refreshLock = new Object();

    /**
     * 获取缓存名称
     *
     * <p>子类必须实现此方法，返回管理的 cacheName。
     *
     * @return 缓存名称，不能为 null
     */
    protected abstract String getCacheName();

    /**
     * 获取缓存配置规范
     *
     * <p>默认实现：从系统配置解析器获取默认配置，然后调用 {@link #buildCacheSpecification(CacheSpecification)}
     * 允许子类基于默认配置进行修改。
     *
     * <p>子类可以选择：
     * <ul>
     *   <li><b>不覆盖</b>：使用系统默认配置（从配置文件读取）</li>
     *   <li><b>覆盖此方法</b>：完全自定义配置</li>
     *   <li><b>覆盖 {@link #buildCacheSpecification(CacheSpecification)}</b>：基于默认配置修改</li>
     * </ul>
     *
     * @return 缓存配置规范，不能为 null
     */
    protected CacheSpecification getCacheSpecification() {
        String cacheName = getCacheName();
        // 从系统配置获取默认配置
        CacheSpecification defaultSpec = specificationResolver.resolve(cacheName);
        // 调用钩子方法，允许子类基于默认配置修改
        return buildCacheSpecification(defaultSpec);
    }

    /**
     * 基于默认配置构建缓存配置规范
     *
     * <p>子类可以覆盖此方法，基于系统默认配置进行修改。
     * 例如：修改 TTL、本地缓存大小等。
     *
     * <p>默认实现：直接返回默认配置，不做修改。
     *
     * @param defaultSpec 系统默认配置（从配置文件或配置解析器获取）
     * @return 最终的缓存配置规范，不能为 null
     */
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        // 默认实现：直接返回默认配置
        return defaultSpec;
    }

    /**
     * 构建默认配置规范
     *
     * <p>当没有配置解析器时，使用此方法构建默认配置。
     * 子类可以覆盖此方法自定义默认配置。
     *
     * @return 默认配置规范
     */
    protected CacheSpecification buildDefaultSpecification() {
        return new CacheSpecification.Builder()
                .ttl(Duration.ofHours(1))
                .localMaxSize(10000)
                .cacheLevel(CacheLevelEnum.L1_L2)
                .allowNullValues(true)
                .enableBloomFilter(false)
                .fallbackStrategyType(FallbackStrategy.Type.DEGRADE_TO_L1)
                .consistencyLevel(ConsistencyLevelEnum.EVENTUAL)
                .build();
    }

    /**
     * 初始化缓存
     *
     * <p>在 Spring Bean 初始化时调用，创建缓存实例。
     */
    @Override
    public void afterPropertiesSet() {
        if (cacheFactory == null) {
            throw new IllegalStateException("CacheFactory is required but not injected");
        }
        initializeCache();
    }

    /**
     * 清空缓存资源
     *
     * <p>清空缓存数据并清理资源引用。
     * 在 Bean 销毁时调用，确保资源正确释放。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>如果缓存已初始化，清空缓存数据</li>
     *   <li>清理缓存实例引用</li>
     *   <li>清理配置规范引用</li>
     * </ol>
     */
    protected void clearCache() {
        synchronized (refreshLock) {
            GoyaCache<K, V> cacheToClear = this.cache;
            if (cacheToClear != null) {
                try {
                    cacheToClear.clear();
                    log.debug("Cleared cache data: cacheName={}", getCacheName());
                } catch (Exception e) {
                    log.warn("Failed to clear cache data: cacheName={}", getCacheName(), e);
                }
            }
            // 清理引用
            this.cache = null;
            this.currentSpec = null;
            log.debug("Cleared cache references: cacheName={}", getCacheName());
        }
    }

    /**
     * 销毁缓存
     *
     * <p>在 Spring Bean 销毁时调用，清理缓存资源。
     */
    @Override
    public void destroy() {
        clearCache();
    }

    /**
     * 初始化缓存实例
     *
     * <p>根据配置创建 GoyaCache 实例。
     */
    protected void initializeCache() {
        String cacheName = getCacheName();
        CacheSpecification spec = getCacheSpecification();

        if (cacheName == null) {
            throw new IllegalStateException("Cache name cannot be null");
        }
        if (spec == null) {
            throw new IllegalStateException("Cache specification cannot be null");
        }

        synchronized (refreshLock) {
            this.cache = createCache(spec);
            this.currentSpec = spec;
            log.info("Initialized cache manager: cacheName={}, ttl={}, localMaxSize={}, cacheLevel={}",
                    cacheName, spec.getTtl(), spec.getLocalMaxSize(), spec.getCacheLevel());
        }
    }

    /**
     * 刷新缓存配置
     *
     * <p>重新获取配置并重建缓存实例（如果配置发生变化）。
     * 此方法可以在运行时调用，支持配置热更新。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>获取新的配置规范（调用 {@link #getCacheSpecification()}）</li>
     *   <li>与当前配置比较</li>
     *   <li>如果配置变化，重新创建缓存实例</li>
     *   <li>清理旧缓存数据（可选）</li>
     * </ol>
     */
    public void refresh() {
        synchronized (refreshLock) {
            CacheSpecification newSpec = getCacheSpecification();
            if (newSpec == null) {
                log.warn("Failed to refresh cache: new specification is null, cacheName={}", getCacheName());
                return;
            }

            // 比较配置是否变化（使用 equals 方法）
            if (newSpec.equals(currentSpec)) {
                log.debug("Cache configuration unchanged, skip refresh: cacheName={}", getCacheName());
                return;
            }

            // 配置变化，重新创建缓存实例
            String cacheName = getCacheName();
            log.info("Refreshing cache configuration: cacheName={}, oldTtl={}, newTtl={}",
                    cacheName, currentSpec != null ? currentSpec.getTtl() : null, newSpec.getTtl());

            GoyaCache<K, V> oldCache = this.cache;
            this.cache = createCache(newSpec);
            this.currentSpec = newSpec;

            // 清理旧缓存数据（可选，根据业务需求决定）
            if (oldCache != null) {
                oldCache.clear();
                log.debug("Cleared old cache data: cacheName={}", cacheName);
            }
        }
    }

    /**
     * 创建缓存实例
     *
     * @param spec 缓存配置规范
     * @return GoyaCache 实例
     */
    protected GoyaCache<K, V> createCache(CacheSpecification spec) {
        return cacheFactory.createCache(getCacheName(), spec);
    }

    /**
     * 获取当前缓存实例
     *
     * @return GoyaCache 实例
     * @throws IllegalStateException 如果缓存未初始化
     */
    protected GoyaCache<K, V> getCache() {
        GoyaCache<K, V> cache = this.cache;
        if (cache == null) {
            throw new IllegalStateException("Cache not initialized: cacheName=" + getCacheName());
        }
        return cache;
    }

    // ========== 便捷的缓存操作方法 ==========

    /**
     * 获取缓存值
     */
    public V get(K key, Class<V> clazz) {
        return getCache().get(key, clazz);
    }

    /**
     * 获取缓存值
     */
    public V get(K key) {
        return getCache().getTypedValue(key);
    }

    /**
     * 获取缓存值（带加载器）
     */
    public V get(K key, Callable<V> loader) {
        try {
            return getCache().get(key, loader);
        } catch (Exception e) {
            throw new CacheException("Failed to get or load cache value", e);
        }
    }

    /**
     * 写入缓存
     */
    public void put(K key, V value) {
        getCache().putTyped(key, value);
    }

    /**
     * 写入缓存（带自定义 TTL）
     */
    public void put(K key, V value, Duration ttl) {
        getCache().putTyped(key, value, ttl);
    }

    /**
     * 失效缓存
     */
    public void evict(K key) {
        getCache().evictTyped(key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        getCache().clear();
    }

    /**
     * 批量获取缓存值
     */
    public Map<K, V> batchGet(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        return getCache().batchGetTyped(keys);
    }

    /**
     * 批量写入缓存
     */
    public void batchPut(Map<K, V> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        getCache().batchPutTyped(entries);
    }

    /**
     * 批量失效缓存
     */
    public void batchEvict(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        getCache().batchEvictTyped(keys);
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(K key) {
        return get(key) != null;
    }

    /**
     * 获取当前配置规范
     *
     * @return 当前配置规范
     */
    public CacheSpecification getCurrentSpecification() {
        return currentSpec;
    }

}
