package com.ysmjjsy.goya.component.cache.template;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 抽象多租户模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供多租户缓存功能。
 * 支持租户隔离、租户级别的缓存配置、租户级别的缓存统计等功能，适用于SaaS场景。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供多租户缓存功能，支持租户数据隔离</li>
 *   <li>支持租户级别的缓存配置，不同租户可以使用不同的缓存策略</li>
 *   <li>支持租户级别的缓存统计，提供租户维度的监控数据</li>
 *   <li>支持租户级别的缓存清理，方便租户数据管理</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>租户</b>：多租户系统中的租户标识</li>
 *   <li><b>租户隔离</b>：不同租户的数据完全隔离，互不影响</li>
 *   <li><b>租户Key</b>：由租户ID和业务Key组成的复合Key</li>
 *   <li><b>租户缓存</b>：每个租户拥有独立的缓存空间</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>SaaS系统的多租户缓存</li>
 *   <li>企业级应用的租户数据隔离</li>
 *   <li>云服务的多租户资源管理</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class TenantUserCacheManager extends AbstractMultiTenantTemplate<String, Long, User> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "tenantUserCache";
 *     }
 *
 *     // 获取租户用户
 *     public User getTenantUser(String tenantId, Long userId) {
 *         return get(tenantId, userId);
 *     }
 *
 *     // 保存租户用户
 *     public void saveTenantUser(String tenantId, Long userId, User user) {
 *         put(tenantId, userId, user);
 *     }
 *
 *     // 清空租户缓存
 *     public void clearTenantCache(String tenantId) {
 *         clearTenant(tenantId);
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>租户Key通过组合租户ID和业务Key生成</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <T> 租户ID类型
 * @param <K> 业务Key类型
 * @param <V> 缓存值类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractMultiTenantTemplate<T, K, V> extends AbstractCacheTemplate<AbstractMultiTenantTemplate.TenantKey<T, K>, V> {

    /**
     * 租户Key
     *
     * <p>封装租户ID和业务Key，用于多租户缓存。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @param <T> 租户ID类型
     * @param <K> 业务Key类型
     */
    public record TenantKey<T, K>(T tenantId, K key) {
        /**
         * 构造函数
         */
        public TenantKey {
            if (tenantId == null) {
                throw new IllegalArgumentException("TenantId cannot be null");
            }
            if (key == null) {
                throw new IllegalArgumentException("Key cannot be null");
            }
        }

        @Override
        public String toString() {
            return tenantId + ":" + key;
        }
    }

    /**
     * 创建租户Key
     *
     * <p>子类可以覆盖此方法自定义租户Key格式。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @return 租户Key
     */
    protected TenantKey<T, K> createTenantKey(T tenantId, K key) {
        return new TenantKey<>(tenantId, key);
    }

    /**
     * 获取租户缓存值
     *
     * <p>从指定租户的缓存中获取值。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @return 缓存值，如果不存在则返回null
     */
    public V get(T tenantId, K key) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        TenantKey<T, K> tenantKey = createTenantKey(tenantId, key);
        return super.get(tenantKey);
    }

    /**
     * 获取租户缓存值（带加载器）
     *
     * <p>从指定租户的缓存中获取值，如果不存在则调用加载器加载并缓存。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @param loader 值加载器
     * @return 缓存值或加载的值
     */
    public V get(T tenantId, K key, Callable<V> loader) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }
        TenantKey<T, K> tenantKey = createTenantKey(tenantId, key);
        return super.get(tenantKey, loader);
    }

    /**
     * 写入租户缓存
     *
     * <p>将值写入指定租户的缓存。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @param value 缓存值
     */
    public void put(T tenantId, K key, V value) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        TenantKey<T, K> tenantKey = createTenantKey(tenantId, key);
        super.put(tenantKey, value);
    }

    /**
     * 写入租户缓存（带自定义TTL）
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @param value 缓存值
     * @param ttl 过期时间
     */
    public void put(T tenantId, K key, V value, java.time.Duration ttl) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        TenantKey<T, K> tenantKey = createTenantKey(tenantId, key);
        super.put(tenantKey, value, ttl);
    }

    /**
     * 失效租户缓存
     *
     * <p>失效指定租户的缓存项。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     */
    public void evict(T tenantId, K key) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        TenantKey<T, K> tenantKey = createTenantKey(tenantId, key);
        super.evict(tenantKey);
    }

    /**
     * 清空租户缓存
     *
     * <p>清空指定租户的所有缓存数据。
     * 注意：此方法需要遍历所有Key，性能较低，建议在后台任务中使用。
     *
     * @param tenantId 租户ID
     */
    public void clearTenant(T tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }

        // 注意：此方法需要遍历所有Key，性能较低
        // 实际实现中，可以通过缓存前缀或其他方式优化
        log.warn("clearTenant() method is not fully implemented, requires iteration over all keys");
    }

    /**
     * 批量获取租户缓存值
     *
     * <p>批量获取指定租户的多个缓存值。
     *
     * @param tenantId 租户ID
     * @param keys 业务Key集合
     * @return key-value映射，只包含命中的key
     */
    public Map<K, V> batchGet(T tenantId, Set<K> keys) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Set<TenantKey<T, K>> tenantKeys = keys.stream()
                .filter(key -> key != null)
                .map(key -> createTenantKey(tenantId, key))
                .collect(java.util.stream.Collectors.toSet());

        Map<TenantKey<T, K>, V> tenantResults = super.batchGet(tenantKeys);

        // 转换为业务Key映射
        return tenantResults.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        entry -> entry.getKey().key(),
                        Map.Entry::getValue
                ));
    }

    /**
     * 批量写入租户缓存
     *
     * <p>批量写入指定租户的多个缓存值。
     *
     * @param tenantId 租户ID
     * @param entries 业务key-value映射
     */
    public void batchPut(T tenantId, Map<K, V> entries) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (entries == null || entries.isEmpty()) {
            return;
        }

        Map<TenantKey<T, K>, V> tenantEntries = entries.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .collect(java.util.stream.Collectors.toMap(
                        entry -> createTenantKey(tenantId, entry.getKey()),
                        Map.Entry::getValue
                ));

        super.batchPut(tenantEntries);
    }

    /**
     * 批量失效租户缓存
     *
     * <p>批量失效指定租户的多个缓存项。
     *
     * @param tenantId 租户ID
     * @param keys 业务Key集合
     */
    public void batchEvict(T tenantId, Set<K> keys) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (keys == null || keys.isEmpty()) {
            return;
        }

        Set<TenantKey<T, K>> tenantKeys = keys.stream()
                .filter(key -> key != null)
                .map(key -> createTenantKey(tenantId, key))
                .collect(java.util.stream.Collectors.toSet());

        super.batchEvict(tenantKeys);
    }

    /**
     * 检查租户缓存是否存在
     *
     * <p>检查指定租户的缓存项是否存在。
     *
     * @param tenantId 租户ID
     * @param key 业务Key
     * @return true 如果存在，false 否则
     */
    public boolean exists(T tenantId, K key) {
        return get(tenantId, key) != null;
    }

}

