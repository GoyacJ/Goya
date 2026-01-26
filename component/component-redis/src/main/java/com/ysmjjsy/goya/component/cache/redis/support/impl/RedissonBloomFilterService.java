package com.ysmjjsy.goya.component.cache.redis.support.impl;

import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.RedisBloomFilterService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.Objects;

/**
 * <p>基于 Redisson {@link RBloomFilter} 的布隆过滤器实现</p>
 * <p>对象名：{@code bf:{name}}</p>
 *
 * @author goya
 * @since 2026/1/25 23:42
 */
public class RedissonBloomFilterService implements RedisBloomFilterService {

    /**
     * Redis 命名空间：bf（bloom filter）。
     */
    private static final String NS = "bf";

    private final RedissonClient redisson;
    private final RedisKeySupport keys;

    public RedissonBloomFilterService(RedissonClient redisson, RedisKeySupport keys) {
        this.redisson = Objects.requireNonNull(redisson, "redisson 不能为空");
        this.keys = Objects.requireNonNull(keys, "keys 不能为空");
    }

    @Override
    public boolean initIfAbsent(String name, long expectedInsertions, double falsePositiveRate) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException("expectedInsertions 必须 > 0");
        }
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("falsePositiveRate 必须在 (0,1) 区间内");
        }
        RBloomFilter<Object> bf = redisson.getBloomFilter(keys.name(NS, name));
        return bf.tryInit(expectedInsertions, falsePositiveRate);
    }

    @Override
    public boolean add(String name, Object value) {
        Objects.requireNonNull(value, "value 不能为空");
        RBloomFilter<Object> bf = redisson.getBloomFilter(keys.name(NS, name));
        return bf.add(value);
    }

    @Override
    public long addAll(String name, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        RBloomFilter<Object> bf = redisson.getBloomFilter(keys.name(NS, name));
        // Redisson 的 addAll 返回 boolean 或 long 在不同版本可能不同；这里用逐个 add 保证语义稳定
        long cnt = 0L;
        for (Object v : values) {
            if (v == null) {
                continue;
            }
            if (bf.add(v)) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public boolean contains(String name, Object value) {
        Objects.requireNonNull(value, "value 不能为空");
        RBloomFilter<Object> bf = redisson.getBloomFilter(keys.name(NS, name));
        return bf.contains(value);
    }
}