package com.ysmjjsy.goya.component.cache.template;

import com.ysmjjsy.goya.component.cache.filter.BloomFilterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象布隆过滤器模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供布隆过滤器功能。
 * 支持缓存穿透保护、快速过滤不存在的Key、异步更新布隆过滤器等功能。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供布隆过滤器功能，快速过滤不存在的Key</li>
 *   <li>支持缓存穿透保护，减少无效查询</li>
 *   <li>支持异步更新布隆过滤器，不影响主流程性能</li>
 *   <li>支持动态扩容，自动处理布隆过滤器饱和</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>布隆过滤器</b>：概率性数据结构，用于快速判断元素是否存在</li>
 *   <li><b>缓存穿透保护</b>：使用布隆过滤器过滤不存在的Key，减少无效查询</li>
 *   <li><b>误判率</b>：布隆过滤器可能误判（判断存在但实际不存在），但不会漏判</li>
 *   <li><b>动态扩容</b>：当布隆过滤器接近饱和时，自动扩容</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>缓存穿透保护</li>
 *   <li>快速过滤不存在的Key</li>
 *   <li>减少无效数据库查询</li>
 *   <li>提升系统性能</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class UserBloomFilterManager extends AbstractBloomFilterTemplate<Long> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userBloomFilter";
 *     }
 *
 *     // 检查用户是否存在（缓存穿透保护）
 *     public boolean mightContainUser(Long userId) {
 *         return mightContain(userId);
 *     }
 *
 *     // 添加用户到布隆过滤器
 *     public void addUser(Long userId) {
 *         putAsync(userId);
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>布隆过滤器操作委托给 BloomFilterManager</li>
 *   <li>异步更新操作在独立线程中执行</li>
 * </ul>
 *
 * @param <K> 布隆过滤器Key类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractBloomFilterTemplate<K> extends AbstractCacheTemplate<K, Boolean> {

    /**
     * 布隆过滤器管理器（可选，用于布隆过滤器功能）
     */
    @Autowired(required = false)
    protected BloomFilterManager bloomFilterManager;

    /**
     * 检查Key是否可能存在
     *
     * <p>使用布隆过滤器检查Key是否可能存在。
     * 如果布隆过滤器未初始化，返回true（允许查询，避免阻塞）。
     *
     * @param key 要检查的Key
     * @return 如果Key可能存在，返回true；如果确定不存在，返回false
     */
    public boolean mightContain(K key) {
        if (key == null) {
            // null key 允许查询
            return true;
        }
        if (bloomFilterManager == null) {
            log.debug("BloomFilterManager not injected, returning true for key: {}", key);
            // 未注入布隆过滤器管理器，允许查询
            return true;
        }

        String cacheName = getCacheName();
        return bloomFilterManager.mightContain(cacheName, key);
    }

    /**
     * 异步更新布隆过滤器
     *
     * <p>将Key添加到布隆过滤器。如果布隆过滤器未初始化，延迟初始化。
     * 如果接近饱和，自动扩容。
     *
     * @param key 要添加的Key
     * @return CompletableFuture，正常完成时返回null，异常完成时包含异常
     */
    public CompletableFuture<Void> putAsync(K key) {
        if (key == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (bloomFilterManager == null) {
            log.debug("BloomFilterManager not injected, skipping putAsync for key: {}", key);
            return CompletableFuture.completedFuture(null);
        }

        String cacheName = getCacheName();
        return bloomFilterManager.putAsync(cacheName, key);
    }

    /**
     * 批量检查Key是否可能存在
     *
     * <p>批量使用布隆过滤器检查多个Key是否可能存在。
     *
     * @param keys 要检查的Key集合
     * @return Key-是否存在映射
     */
    public java.util.Map<K, Boolean> batchMightContain(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return java.util.Map.of();
        }

        return keys.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        key -> key,
                        this::mightContain
                ));
    }

    /**
     * 批量异步更新布隆过滤器
     *
     * <p>批量将Key添加到布隆过滤器。
     *
     * @param keys 要添加的Key集合
     * @return CompletableFuture数组，每个对应一个Key的更新操作
     */
    public CompletableFuture[] batchPutAsync(Set<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return new CompletableFuture[0];
        }

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = keys.stream()
                .filter(Objects::nonNull)
                .map(this::putAsync)
                .toArray(CompletableFuture[]::new);

        return futures;
    }

    /**
     * 检查布隆过滤器是否可用
     *
     * <p>检查布隆过滤器管理器是否已注入。
     *
     * @return true 如果可用，false 否则
     */
    public boolean isAvailable() {
        return bloomFilterManager != null;
    }

    /**
     * 使用布隆过滤器保护的操作
     *
     * <p>先使用布隆过滤器检查Key是否存在，如果不存在则直接返回null，避免无效查询。
     * 如果布隆过滤器判断可能存在，则执行查询操作。
     *
     * @param key 要查询的Key
     * @param queryFunction 查询函数（当布隆过滤器判断可能存在时调用）
     * @param <V> 查询结果类型
     * @return 查询结果，如果布隆过滤器判断不存在则返回null
     */
    public <V> V queryWithBloomFilter(K key, java.util.function.Function<K, V> queryFunction) {
        if (key == null) {
            return null;
        }
        if (queryFunction == null) {
            throw new IllegalArgumentException("QueryFunction cannot be null");
        }

        // 先检查布隆过滤器
        if (!mightContain(key)) {
            // 布隆过滤器判断不存在，直接返回null
            return null;
        }

        // 布隆过滤器判断可能存在，执行查询
        try {
            V result = queryFunction.apply(key);
            // 如果查询成功，异步更新布隆过滤器
            if (result != null) {
                putAsync(key);
            }
            return result;
        } catch (Exception e) {
            log.warn("Query failed for key: {}", key, e);
            return null;
        }
    }

}

