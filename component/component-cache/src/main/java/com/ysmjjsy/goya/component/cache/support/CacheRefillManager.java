package com.ysmjjsy.goya.component.cache.support;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.core.LocalCache;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecificationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存回填管理器
 *
 * <p>控制异步回填 L1 的并发，避免多个线程同时查询 L2 命中时重复回填。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>管理异步回填任务，确保同一 key 只回填一次</li>
 *   <li>控制回填任务的并发执行</li>
 *   <li>处理回填失败的情况（记录日志，不影响主流程）</li>
 * </ul>
 *
 * <p><b>调用关系：</b>
 * <ul>
 *   <li>由 {@link GoyaCache} 在 L2 命中时调用</li>
 *   <li>使用 {@link LocalCache#put(Object, Object, Duration)} 回填 L1</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li>检查是否已有该 key 的回填任务</li>
 *   <li>如果没有，创建异步回填任务并提交到线程池</li>
 *   <li>如果有，直接返回（避免重复回填）</li>
 *   <li>回填完成后，从任务 Map 中移除</li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储回填任务，线程安全</li>
 *   <li>回填操作在独立的线程中执行（CompletableFuture.runAsync）</li>
 *   <li>使用 putIfAbsent 确保同一 key 只创建一个回填任务</li>
 * </ol>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>回填失败时记录警告日志，不影响主流程（L2 已命中，数据可用）</li>
 *   <li>确保回填任务完成后从 Map 中移除（使用 finally 块）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:56
 */
@Slf4j
public class CacheRefillManager {

    /**
     * 配置规范解析器
     */
    private final CacheSpecificationResolver specificationResolver;

    /**
     * 正在回填的任务 Map
     * Key: CacheKey（cacheName + key）
     * Value: CompletableFuture（回填任务）
     */
    private final ConcurrentHashMap<CacheKey, CompletableFuture<Void>> refillingTasks = new ConcurrentHashMap<>();

    /**
     * 默认回填 TTL（L1 TTL 的 90%，确保回填的数据不会立即过期）
     */
    private static final double REFILL_TTL_RATIO = 0.9;

    /**
     * 构造函数
     *
     * @param specificationResolver 配置规范解析器
     * @throws IllegalArgumentException 如果 specificationResolver 为 null
     */
    public CacheRefillManager(CacheSpecificationResolver specificationResolver) {
        if (specificationResolver == null) {
            throw new IllegalArgumentException("CacheSpecificationResolver cannot be null");
        }
        this.specificationResolver = specificationResolver;
    }

    /**
     * 异步回填 L1（带并发控制）
     *
     * <p>如果该 key 已有回填任务，不重复执行。使用 computeIfAbsent 确保原子性操作，避免竞态条件。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>构建 CacheKey（cacheName + key）</li>
     *   <li>使用 computeIfAbsent 原子性创建或获取回填任务</li>
     *   <li>如果任务已存在，直接返回（避免重复回填）</li>
     *   <li>如果任务不存在，创建异步回填任务</li>
     *   <li>添加 whenComplete 回调，确保任务完成后从 Map 中移除</li>
     * </ol>
     *
     * <p><b>线程模型：</b>
     * <ul>
     *   <li>使用 computeIfAbsent 确保原子性，避免竞态条件</li>
     *   <li>多个线程同时调用时，只有一个线程会创建任务</li>
     *   <li>其他线程会获取已存在的任务，避免重复回填</li>
     * </ul>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果回填失败，记录警告日志但不抛出异常</li>
     *   <li>使用 whenComplete 确保任务完成后从 Map 中移除（无论成功或失败）</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值（ValueWrapper）
     * @param l1        本地缓存实例
     */
    public void refillAsync(String cacheName, Object key, Cache.ValueWrapper value, LocalCache l1) {
        if (cacheName == null || key == null || value == null || l1 == null) {
            log.warn("Invalid parameters for refill: cacheName={}, key={}", cacheName, key);
            return;
        }

        CacheKey cacheKey = new CacheKey(cacheName, key);

        // 使用 computeIfAbsent 原子性创建或获取回填任务
        CompletableFuture<Void> future = refillingTasks.computeIfAbsent(cacheKey, k -> {
            // 创建异步回填任务
            CompletableFuture<Void> newFuture = CompletableFuture.runAsync(() -> {
                try {
                    // 计算回填 TTL（使用 L1 TTL 的 90%）
                    Duration refillTtl = calculateRefillTtl(cacheName);

                    // 回填 L1
                    l1.put(key, value.get(), refillTtl);

                    if (log.isTraceEnabled()) {
                        log.trace("Successfully refilled L1 cache for key: {}", cacheKey);
                    }
                } catch (Exception e) {
                    log.warn("Failed to refill L1 cache for key: {}", cacheKey, e);
                    // 不抛出异常，不影响主流程
                }
            });

            // 添加完成回调，确保任务完成后从 Map 中移除
            newFuture.whenComplete((result, throwable) -> {
                refillingTasks.remove(cacheKey);
                if (log.isTraceEnabled()) {
                    if (throwable != null) {
                        log.trace("Refill task completed with exception for key: {}", cacheKey, throwable);
                    } else {
                        log.trace("Refill task completed successfully for key: {}", cacheKey);
                    }
                }
            });

            return newFuture;
        });

        // 如果任务已存在（computeIfAbsent 返回已存在的任务），记录日志
        if (log.isTraceEnabled() && future != null) {
            log.trace("Refill task already exists for key: {}, using existing task", cacheKey);
        }
    }

    /**
     * 计算回填 TTL
     *
     * <p>回填 TTL 使用 L1 TTL 的 90%，确保回填的数据不会立即过期。
     * 从 CacheSpecification 读取 L1 TTL，确保与配置一致。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>从配置解析器获取 CacheSpecification</li>
     *   <li>获取 L1 TTL（spec.getLocalTtl()）</li>
     *   <li>计算回填 TTL（L1 TTL × 90%）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果配置解析失败，使用默认 TTL（1 小时）并记录警告日志</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @return 回填 TTL
     */
    private Duration calculateRefillTtl(String cacheName) {
        try {
            CacheSpecification spec = specificationResolver.resolve(cacheName);
            Duration localTtl = spec.getLocalTtl();
            long millis = (long) (localTtl.toMillis() * REFILL_TTL_RATIO);
            if (millis <= 0) {
                log.warn("Calculated refill TTL is not positive for cache: {}, using default", cacheName);
                return Duration.ofHours(1);
            }
            return Duration.ofMillis(millis);
        } catch (Exception e) {
            log.warn("Failed to resolve cache specification for refill TTL calculation: cacheName={}, using default", cacheName, e);
            // 降级到默认 TTL
            Duration defaultTtl = Duration.ofHours(1);
            long millis = (long) (defaultTtl.toMillis() * REFILL_TTL_RATIO);
            return Duration.ofMillis(millis);
        }
    }

    /**
     * 缓存键（cacheName + key）
     *
     * <p>用于唯一标识一个回填任务。
     */
    private record CacheKey(String cacheName, Object key) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CacheKey cacheKey)) {
                return false;
            }
            return Objects.equals(key(), cacheKey.key()) && Objects.equals(cacheName(), cacheKey.cacheName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheName(), key());
        }
    }
}
