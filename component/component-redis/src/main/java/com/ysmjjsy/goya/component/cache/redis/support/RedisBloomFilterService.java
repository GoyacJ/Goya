package com.ysmjjsy.goya.component.cache.redis.support;

import com.ysmjjsy.goya.component.framework.cache.support.CacheBloomFilter;

import java.util.Collection;

/**
 * <p>Redis 布隆过滤器服务</p>
 * <p>用于防缓存穿透、快速判定成员存在性（有误判率、无漏判）。</p>
 *
 * <p><b>说明：</b></p>
 * <ul>
 *   <li>布隆过滤器适合“只增不减”的场景；删除会带来复杂性与误判变化</li>
 *   <li>初始化参数必须慎重：expectedInsertions / falsePositiveRate 决定内存占用</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 23:41
 */
public interface RedisBloomFilterService extends CacheBloomFilter {

    /**
     * 初始化布隆过滤器（若已存在则不覆盖）。
     *
     * @param name               过滤器名
     * @param expectedInsertions 预估插入量
     * @param falsePositiveRate  误判率（0~1，例如 0.01）
     * @return 是否初始化成功（true 表示本次完成初始化；false 表示已存在）
     */
    boolean initIfAbsent(String name, long expectedInsertions, double falsePositiveRate);

    /**
     * 批量添加元素。
     *
     * @param name   过滤器名
     * @param values 值集合
     * @return 添加成功的数量（近似语义：以实际 Redisson 返回为准）
     */
    long addAll(String name, Collection<?> values);
}