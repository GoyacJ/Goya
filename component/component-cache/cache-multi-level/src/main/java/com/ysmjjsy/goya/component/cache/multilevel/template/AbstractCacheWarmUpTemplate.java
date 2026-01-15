package com.ysmjjsy.goya.component.cache.multilevel.template;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 抽象缓存预热模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供缓存预热功能。
 * 支持批量预热、异步预热、预热进度监控等功能，适用于系统启动时的缓存预热场景。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供缓存预热功能，系统启动时预加载热点数据</li>
 *   <li>支持批量预热，提升预热效率</li>
 *   <li>支持异步预热，不阻塞系统启动</li>
 *   <li>支持预热进度监控，了解预热状态</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>缓存预热</b>：系统启动时预加载热点数据到缓存</li>
 *   <li><b>批量预热</b>：一次性预热多个Key的数据</li>
 *   <li><b>异步预热</b>：在后台线程中执行预热，不阻塞主流程</li>
 *   <li><b>预热策略</b>：根据业务需求选择预热的数据和时机</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>系统启动时的缓存预热</li>
 *   <li>定时任务预热热点数据</li>
 *   <li>数据变更后的缓存刷新</li>
 *   <li>系统维护后的缓存重建</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class UserCacheWarmUpManager extends AbstractCacheWarmUpTemplate<Long, User> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userCache";
 *     }
 *
 *     // 预热用户缓存
 *     public void warmUpUserCache(List<Long> userIds) {
 *         warmUp(userIds, userId -> {
 *             // 从数据库加载用户数据
 *             return userRepository.findById(userId);
 *         });
 *     }
 *
 *     // 异步预热用户缓存
 *     public CompletableFuture<Void> warmUpUserCacheAsync(List<Long> userIds) {
 *         return warmUpAsync(userIds, userId -> {
 *             return userRepository.findById(userId);
 *         });
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>异步预热使用线程池执行，支持并发预热</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <K> 缓存Key类型
 * @param <V> 缓存值类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractCacheWarmUpTemplate<K, V> extends AbstractCacheTemplate<K, V> {

    /**
     * 预热线程池（用于异步预热）
     */
    private volatile ExecutorService warmUpExecutor;

    /**
     * 获取预热线程池
     *
     * <p>懒加载创建预热线程池。
     *
     * @return 预热线程池
     */
    private ExecutorService getWarmUpExecutor() {
        if (warmUpExecutor == null) {
            synchronized (this) {
                if (warmUpExecutor == null) {
                    warmUpExecutor = Executors.newFixedThreadPool(
                            Math.max(2, Runtime.getRuntime().availableProcessors()),
                            r -> {
                                Thread thread = new Thread(r, "cache-warmup-" + getCacheName());
                                thread.setDaemon(true);
                                return thread;
                            }
                    );
                }
            }
        }
        return warmUpExecutor;
    }

    /**
     * 预热单个Key
     *
     * <p>使用加载器加载数据并写入缓存。
     *
     * @param key 要预热的Key
     * @param loader 数据加载器
     */
    public void warmUp(K key, Function<K, V> loader) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }

        try {
            V value = loader.apply(key);
            if (value != null) {
                put(key, value);
                log.debug("Warmed up cache: cacheName={}, key={}", getCacheName(), key);
            } else {
                log.debug("Loader returned null, skip warm up: cacheName={}, key={}", getCacheName(), key);
            }
        } catch (Exception e) {
            log.warn("Failed to warm up cache: cacheName={}, key={}", getCacheName(), key, e);
        }
    }

    /**
     * 预热单个Key（带自定义TTL）
     *
     * @param key 要预热的Key
     * @param loader 数据加载器
     * @param ttl 过期时间
     */
    public void warmUp(K key, Function<K, V> loader, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        try {
            V value = loader.apply(key);
            if (value != null) {
                put(key, value, ttl);
                log.debug("Warmed up cache with TTL: cacheName={}, key={}, ttl={}", getCacheName(), key, ttl);
            } else {
                log.debug("Loader returned null, skip warm up: cacheName={}, key={}", getCacheName(), key);
            }
        } catch (Exception e) {
            log.warn("Failed to warm up cache: cacheName={}, key={}, ttl={}", getCacheName(), key, ttl, e);
        }
    }

    /**
     * 批量预热
     *
     * <p>批量预热多个Key的数据。
     *
     * @param keys 要预热的Key集合
     * @param loader 数据加载器
     */
    public void warmUp(Set<K> keys, Function<K, V> loader) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }

        int successCount = 0;
        int failCount = 0;

        for (K key : keys) {
            if (key == null) {
                continue;
            }
            try {
                warmUp(key, loader);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("Failed to warm up cache: cacheName={}, key={}", getCacheName(), key, e);
            }
        }

        log.info("Batch warm up completed: cacheName={}, total={}, success={}, fail={}",
                getCacheName(), keys.size(), successCount, failCount);
    }

    /**
     * 批量预热（带自定义TTL）
     *
     * @param keys 要预热的Key集合
     * @param loader 数据加载器
     * @param ttl 过期时间
     */
    public void warmUp(Set<K> keys, Function<K, V> loader, Duration ttl) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        int successCount = 0;
        int failCount = 0;

        for (K key : keys) {
            if (key == null) {
                continue;
            }
            try {
                warmUp(key, loader, ttl);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("Failed to warm up cache: cacheName={}, key={}, ttl={}", getCacheName(), key, ttl, e);
            }
        }

        log.info("Batch warm up completed: cacheName={}, total={}, success={}, fail={}, ttl={}",
                getCacheName(), keys.size(), successCount, failCount, ttl);
    }

    /**
     * 异步预热单个Key
     *
     * <p>在后台线程中执行预热，不阻塞主流程。
     *
     * @param key 要预热的Key
     * @param loader 数据加载器
     * @return CompletableFuture，正常完成时返回null，异常完成时包含异常
     */
    public CompletableFuture<Void> warmUpAsync(K key, Function<K, V> loader) {
        if (key == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (loader == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Loader cannot be null"));
        }

        return CompletableFuture.runAsync(() -> warmUp(key, loader), getWarmUpExecutor());
    }

    /**
     * 异步预热单个Key（带自定义TTL）
     *
     * @param key 要预热的Key
     * @param loader 数据加载器
     * @param ttl 过期时间
     * @return CompletableFuture
     */
    public CompletableFuture<Void> warmUpAsync(K key, Function<K, V> loader, Duration ttl) {
        if (key == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (loader == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Loader cannot be null"));
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("TTL must be positive"));
        }

        return CompletableFuture.runAsync(() -> warmUp(key, loader, ttl), getWarmUpExecutor());
    }

    /**
     * 批量异步预热
     *
     * <p>在后台线程中批量执行预热，不阻塞主流程。
     *
     * @param keys 要预热的Key集合
     * @param loader 数据加载器
     * @return CompletableFuture，正常完成时返回null，异常完成时包含异常
     */
    public CompletableFuture<Void> warmUpAsync(Set<K> keys, Function<K, V> loader) {
        if (keys == null || keys.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if (loader == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Loader cannot be null"));
        }

        return CompletableFuture.runAsync(() -> warmUp(keys, loader), getWarmUpExecutor());
    }

    /**
     * 批量异步预热（带自定义TTL）
     *
     * @param keys 要预热的Key集合
     * @param loader 数据加载器
     * @param ttl 过期时间
     * @return CompletableFuture
     */
    public CompletableFuture<Void> warmUpAsync(Set<K> keys, Function<K, V> loader, Duration ttl) {
        if (keys == null || keys.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if (loader == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Loader cannot be null"));
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("TTL must be positive"));
        }

        return CompletableFuture.runAsync(() -> warmUp(keys, loader, ttl), getWarmUpExecutor());
    }

    /**
     * 预热所有Key
     *
     * <p>使用Key生成器生成所有Key并预热。
     * 注意：此方法需要生成所有Key，性能较低，建议在后台任务中使用。
     *
     * @param keyGenerator Key生成器
     * @param loader 数据加载器
     */
    public void warmUpAll(Function<Void, Set<K>> keyGenerator, Function<K, V> loader) {
        if (keyGenerator == null) {
            throw new IllegalArgumentException("KeyGenerator cannot be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }

        Set<K> keys = keyGenerator.apply(null);
        if (keys == null || keys.isEmpty()) {
            log.info("No keys to warm up: cacheName={}", getCacheName());
            return;
        }

        warmUp(keys, loader);
    }

    /**
     * 清理预热线程池
     *
     * <p>在Bean销毁时调用，清理预热线程池资源。
     */
    @Override
    public void destroy() {
        super.destroy();
        if (warmUpExecutor != null) {
            warmUpExecutor.shutdown();
            log.debug("Shutdown warm up executor: cacheName={}", getCacheName());
        }
    }

}

