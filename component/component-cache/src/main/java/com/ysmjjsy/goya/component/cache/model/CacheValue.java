package com.ysmjjsy.goya.component.cache.model;

import java.io.Serializable;

/**
 * <p>缓存值包装类（带版本号）</p>
 * <p>用于多级缓存的一致性控制</p>
 * 
 * <p>设计目的：</p>
 * <ul>
 *     <li>解决多级缓存的数据一致性问题</li>
 *     <li>使用版本号机制确保总是使用最新版本的数据</li>
 *     <li>避免在 L2 写入失败时导致的数据不一致</li>
 * </ul>
 * 
 * @param <V> 实际缓存值类型
 * @param value 实际缓存值
 * @param version 版本号（纳秒时间戳，确保唯一性和递增性）
 * @param timestamp 创建时间戳（毫秒）
 * 
 * @author goya
 * @since 2025/12/22
 */
public record CacheValue<V>(
    V value,
    long version,
    long timestamp
) implements Serializable {
    
    /**
     * 创建包装值
     * 使用纳秒时间戳作为版本号，确保单调递增
     * 
     * @param value 实际缓存值
     * @param <V> 值类型
     * @return 包装后的缓存值
     */
    public static <V> CacheValue<V> of(V value) {
        return new CacheValue<>(
            value,
            System.nanoTime(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * 判断当前版本是否比另一个版本新
     * 
     * @param other 另一个缓存值
     * @return true 如果当前版本更新
     */
    public boolean isNewerThan(CacheValue<V> other) {
        return other == null || this.version > other.version;
    }
}

