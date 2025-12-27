package com.ysmjjsy.goya.component.cache.support;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SingleFlight 加载器
 *
 * <p>防止同一 key 的并发回源请求，将多个并发请求合并为一个实际加载操作。
 * 这是解决缓存击穿问题的核心机制，避免高并发场景下大量请求同时回源数据库。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>防止缓存击穿：同一 key 的并发回源请求合并为一个</li>
 *   <li>减少数据库压力：避免大量重复的数据库查询</li>
 *   <li>提升系统稳定性：防止数据库连接池耗尽</li>
 * </ul>
 *
 * <p><b>工作原理：</b>
 * <ol>
 *   <li>第一个请求创建加载任务并执行</li>
 *   <li>后续请求等待第一个请求的结果</li>
 *   <li>加载完成后，所有等待的请求共享同一个结果</li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储加载任务，线程安全</li>
 *   <li>使用 {@link CompletableFuture} 实现异步等待</li>
 *   <li>加载任务完成后自动清理，防止内存泄漏</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class SingleFlightLoader {

    /**
     * 正在加载的任务 Map
     * Key: 缓存键
     * Value: 加载任务的 CompletableFuture
     */
    private final ConcurrentHashMap<Object, CompletableFuture<Object>> loadingTasks = new ConcurrentHashMap<>();

    /**
     * 默认超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    /**
     * 执行加载操作（SingleFlight 模式）
     *
     * <p>如果已有相同 key 的加载任务，等待该任务完成并返回结果。
     * 如果没有，创建新的加载任务并执行。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>检查是否已有加载任务</li>
     *   <li>如果有，等待该任务完成</li>
     *   <li>如果没有，创建新任务并执行</li>
     *   <li>任务完成后，从 Map 中移除</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果加载失败，从 Map 中移除任务，抛出异常</li>
     *   <li>如果超时，从 Map 中移除任务，抛出 {@link TimeoutException}</li>
     *   <li>所有等待该任务的请求都会收到相同的异常</li>
     * </ul>
     *
     * @param key 缓存键
     * @param valueLoader 值加载器
     * @param <V> 值类型
     * @return 加载的值
     * @throws Exception 如果加载失败或超时
     */
    @SuppressWarnings("unchecked")
    public <V> V load(Object key, Callable<V> valueLoader) throws Exception {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (valueLoader == null) {
            throw new IllegalArgumentException("ValueLoader cannot be null");
        }

        // 使用 computeIfAbsent 原子性创建或获取加载任务
        CompletableFuture<Object> future = loadingTasks.computeIfAbsent(key, k -> {
            // 创建异步加载任务
            CompletableFuture<Object> newFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    // 将检查异常包装为运行时异常，CompletableFuture 会自动处理
                    throw new CommonException(e);
                }
            });

            // 添加完成回调，确保任务完成后从 Map 中移除
            newFuture.whenComplete((_, throwable) -> {
                loadingTasks.remove(key);
                if (log.isTraceEnabled()) {
                    if (throwable != null) {
                        log.trace("SingleFlight load task completed with exception for key: {}", key, throwable);
                    } else {
                        log.trace("SingleFlight load task completed successfully for key: {}", key);
                    }
                }
            });

            return newFuture;
        });

        // 等待任务完成（带超时）
        try {
            @SuppressWarnings("unchecked")
            V result = (V) future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return result;
        } catch (java.util.concurrent.ExecutionException e) {
            // 解包原始异常
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException("Failed to load cache value", cause);
        } catch (TimeoutException _) {
            // 超时时，从 Map 中移除任务（如果还在）
            loadingTasks.remove(key, future);
            log.warn("SingleFlight load task timeout for key: {}", key);
            throw new TimeoutException("Load task timeout for key: " + key);
        }
    }

    /**
     * 执行加载操作（SingleFlight 模式，带自定义超时）
     *
     * @param key 缓存键
     * @param valueLoader 值加载器
     * @param timeoutSeconds 超时时间（秒）
     * @param <V> 值类型
     * @return 加载的值
     * @throws Exception 如果加载失败或超时
     */
    @SuppressWarnings("unchecked")
    public <V> V load(Object key, Callable<V> valueLoader, long timeoutSeconds) throws Exception {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (valueLoader == null) {
            throw new IllegalArgumentException("ValueLoader cannot be null");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive, got: " + timeoutSeconds);
        }

        CompletableFuture<Object> future = loadingTasks.computeIfAbsent(key, k -> {
            CompletableFuture<Object> newFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new CommonException(e);
                }
            });

            newFuture.whenComplete((result, throwable) -> {
                loadingTasks.remove(key);
            });

            return newFuture;
        });

        try {
            @SuppressWarnings("unchecked")
            V result = (V) future.get(timeoutSeconds, TimeUnit.SECONDS);
            return result;
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException("Failed to load cache value", cause);
        } catch (TimeoutException _) {
            loadingTasks.remove(key, future);
            log.warn("SingleFlight load task timeout for key: {}, timeout: {}s", key, timeoutSeconds);
            throw new TimeoutException("Load task timeout for key: " + key);
        }
    }

    /**
     * 获取当前正在加载的任务数量
     *
     * @return 正在加载的任务数量
     */
    public int getLoadingTaskCount() {
        return loadingTasks.size();
    }

    /**
     * 清除所有加载任务（用于测试或紧急情况）
     *
     * <p>注意：此操作会中断所有正在进行的加载任务，谨慎使用。
     */
    public void clear() {
        loadingTasks.clear();
    }
}

