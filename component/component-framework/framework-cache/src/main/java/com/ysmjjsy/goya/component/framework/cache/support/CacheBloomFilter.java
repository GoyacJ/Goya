package com.ysmjjsy.goya.component.framework.cache.support;

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
     * 判断元素是否可能存在。
     *
     * @param name  过滤器名
     * @param value 值
     * @return true 表示“可能存在”；false 表示“一定不存在”
     */
    boolean contains(String name, Object value);


    /**
     * 添加元素。
     *
     * @param name  过滤器名
     * @param value 值
     * @return 是否添加成功（Redisson 语义：可能返回 true/false，通常用于表示位是否发生变化）
     */
    boolean add(String name, Object value);
}
