package com.ysmjjsy.goya.component.cache.core.support;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Guava 布隆过滤器实现</p>
 * <p>基于 Guava 的 BloomFilter 实现缓存穿透防护</p>
 * <p>支持按 cacheName 创建不同的布隆过滤器</p>
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
 * @since 2026/1/15 11:53
 * @see CacheBloomFilter
 * @see BloomFilter
 */
@Slf4j
public class GuavaCacheBloomFilter implements CacheBloomFilter {

    /**
     * 默认预期插入数量
     */
    private static final long DEFAULT_EXPECTED_INSERTIONS = 10000L;

    /**
     * 默认误判率
     */
    private static final double DEFAULT_FPP = 0.01;

    /**
     * 布隆过滤器映射
     * Key: cacheName
     * Value: BloomFilter 实例
     */
    private final Map<String, BloomFilter<String>> bloomFilterMap = new ConcurrentHashMap<>();

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer cacheKeySerializer;

    /**
     * 构造函数
     *
     * @param cacheKeySerializer 缓存键序列化器
     */
    public GuavaCacheBloomFilter(CacheKeySerializer cacheKeySerializer) {
        this.cacheKeySerializer = cacheKeySerializer;
    }

    @Override
    public <K> boolean mightContain(String cacheName, K key) {
        if (cacheName == null || cacheName.isBlank()) {
            return true; // 如果 cacheName 无效，默认返回 true
        }
        if (key == null) {
            return false;
        }

        try {
            BloomFilter<String> bloomFilter = getOrCreateBloomFilter(cacheName);
            String serializedKey = serializeKey(key);
            return bloomFilter.mightContain(serializedKey);
        } catch (Exception e) {
            log.error("Failed to check bloom filter: cacheName={}, key={}", cacheName, key, e);
            return true; // 出错时默认返回 true，避免误判
        }
    }

    @Override
    public <K> void put(String cacheName, K key) {
        if (cacheName == null || cacheName.isBlank()) {
            return;
        }
        if (key == null) {
            return;
        }

        try {
            BloomFilter<String> bloomFilter = getOrCreateBloomFilter(cacheName);
            String serializedKey = serializeKey(key);
            bloomFilter.put(serializedKey);

            if (log.isTraceEnabled()) {
                log.trace("Put key to bloom filter: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to put key to bloom filter: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * 获取或创建布隆过滤器
     *
     * @param cacheName 缓存名称
     * @return BloomFilter 实例
     */
    private BloomFilter<String> getOrCreateBloomFilter(String cacheName) {
        return bloomFilterMap.computeIfAbsent(cacheName, name -> {
            BloomFilter<String> filter = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    DEFAULT_EXPECTED_INSERTIONS,
                    DEFAULT_FPP
            );
            if (log.isDebugEnabled()) {
                log.debug("Created bloom filter: cacheName={}, expectedInsertions={}, fpp={}",
                        name, DEFAULT_EXPECTED_INSERTIONS, DEFAULT_FPP);
            }
            return filter;
        });
    }

    /**
     * 序列化缓存键
     *
     * @param key 缓存键
     * @param <K> 键类型
     * @return 序列化后的字符串
     */
    private <K> String serializeKey(K key) {
        if (key instanceof String str) {
            return str;
        }
        return cacheKeySerializer.serializeToString(key);
    }
}
