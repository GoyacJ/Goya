package com.ysmjjsy.goya.component.cache.core.support;

import java.util.concurrent.CompletableFuture;

/**
 * <p>缓存布隆过滤器接口</p>
 * <p>用于缓存穿透防护，预判 key 是否可能存在</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 检查 key 是否可能存在
 * if (bloomFilter.mightContain("userCache", "user:123")) {
 *     // 可能存在，继续查询缓存
 * }
 *
 * // 添加 key 到布隆过滤器
 * bloomFilter.put("userCache", "user:123");
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15 11:42
 */
public interface CacheBloomFilter {

    /**
     * 检查 key 是否可能存在
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return true 如果可能存在，false 如果肯定不存在
     */
    <K> boolean mightContain(String cacheName, K key);

    /**
     * 将 key 添加到布隆过滤器
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     */
    <K> CompletableFuture<Void> putAsync(String cacheName, K key);
}
