package com.ysmjjsy.goya.component.cache.multilevel.template;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 抽象签章缓存模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供版本控制（Stamp）功能。
 * 支持乐观锁、数据变更检测、缓存失效策略等企业级特性。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>为缓存数据添加版本戳（Stamp），支持版本控制</li>
 *   <li>支持乐观锁机制，防止并发更新冲突</li>
 *   <li>支持数据变更检测，判断缓存是否过期</li>
 *   <li>支持基于版本戳的缓存失效策略</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>Stamp（签章）</b>：版本号或时间戳，用于标识数据的版本</li>
 *   <li><b>StampedValue（带签章的值）</b>：包含实际值和版本戳的数据结构</li>
 *   <li><b>版本比较</b>：通过比较版本戳判断数据是否变化</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>需要检测数据变更的场景（如配置缓存、权限缓存）</li>
 *   <li>需要实现乐观锁的场景（如库存扣减、余额更新）</li>
 *   <li>需要基于版本控制缓存失效的场景</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class UserConfigStampManager extends AbstractStampTemplate<String, UserConfig> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "userConfigStamp";
 *     }
 *
     *     // 获取配置（带版本检查）
     *     public UserConfig getConfigWithVersion(String key) {
     *         StampedValue<UserConfig> stamped = getStamped(key);
     *         return stamped != null ? stamped.value() : null;
     *     }
     *
     *     // 更新配置（乐观锁）
     *     public boolean updateConfig(String key, UserConfig config, long expectedVersion) {
     *         return putIfVersion(key, config, expectedVersion);
     *     }
     *
     *     // 写入配置（自动生成版本戳）
     *     public void saveConfig(String key, UserConfig config) {
     *         putValue(key, config);
     *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>版本号生成使用 {@link AtomicLong}，保证原子性</li>
 *   <li>乐观锁操作使用版本比较，保证一致性</li>
 * </ul>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractStampTemplate<K, V> extends AbstractCacheTemplate<K, AbstractStampTemplate.StampedValue<V>> {

    /**
     * 版本号生成器（原子递增）
     * 用于生成全局唯一的版本号
     */
    private static final AtomicLong VERSION_GENERATOR = new AtomicLong(0);

    /**
     * 获取缓存值（带版本戳）
     *
     * <p>从缓存中获取带版本戳的值。如果缓存不存在，返回 null。
     *
     * @param key 缓存键
     * @return 带版本戳的值，如果不存在则返回 null
     */
    public StampedValue<V> getStamped(K key) {
        return get(key);
    }

    /**
     * 获取缓存值（仅值，不包含版本戳）
     *
     * <p>从缓存中获取值，忽略版本戳信息。
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回 null
     */
    public V getValue(K key) {
        StampedValue<V> stamped = getStamped(key);
        return stamped != null ? stamped.value() : null;
    }

    /**
     * 获取版本戳
     *
     * <p>获取指定 key 的版本戳，用于版本比较。
     *
     * @param key 缓存键
     * @return 版本戳，如果不存在则返回 -1
     */
    public long getVersion(K key) {
        StampedValue<V> stamped = getStamped(key);
        return stamped != null ? stamped.version() : -1;
    }

    /**
     * 写入缓存值（自动生成版本戳）
     *
     * <p>将值写入缓存，自动生成新的版本戳。
     * 此方法会自动将值包装为 StampedValue 并调用父类的 put 方法。
     *
     * <p><b>注意：</b>此方法命名为 putValue 而不是 put，是为了避免与父类的
     * put(K, StampedValue<V>) 方法产生类型擦除冲突。
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void putValue(K key, V value) {
        long version = generateVersion();
        putStamped(key, value, version);
    }

    /**
     * 写入缓存值（带自定义 TTL，自动生成版本戳）
     *
     * <p>将值写入缓存，自动生成新的版本戳，并指定过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     */
    public void putValue(K key, V value, Duration ttl) {
        long version = generateVersion();
        putStamped(key, value, version, ttl);
    }

    /**
     * 写入缓存（带自定义版本戳）
     *
     * <p>将值写入缓存，使用指定的版本戳。
     * 通常用于数据迁移或版本恢复场景。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param version 版本戳
     */
    public void putStamped(K key, V value, long version) {
        StampedValue<V> stamped = new StampedValue<>(value, version, Instant.now());
        super.put(key, stamped);
    }

    /**
     * 写入缓存（带自定义 TTL 和版本戳）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param version 版本戳
     * @param ttl 过期时间
     */
    public void putStamped(K key, V value, long version, Duration ttl) {
        StampedValue<V> stamped = new StampedValue<>(value, version, Instant.now());
        super.put(key, stamped, ttl);
    }

    /**
     * 乐观锁更新（仅在版本匹配时更新）
     *
     * <p>如果当前版本与期望版本匹配，则更新缓存并生成新版本。
     * 如果版本不匹配，返回 false，表示更新失败（可能被其他线程修改）。
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>乐观锁更新：防止并发更新冲突</li>
     *   <li>数据一致性检查：确保基于最新版本更新</li>
     * </ul>
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>获取当前版本戳</li>
     *   <li>比较当前版本与期望版本</li>
     *   <li>如果匹配，更新缓存并生成新版本</li>
     *   <li>如果不匹配，返回 false</li>
     * </ol>
     *
     * @param key 缓存键
     * @param value 新值
     * @param expectedVersion 期望的版本号（必须与当前版本匹配）
     * @return true 如果更新成功，false 如果版本不匹配
     */
    public boolean putIfVersion(K key, V value, long expectedVersion) {
        StampedValue<V> current = getStamped(key);

        // 如果缓存不存在，且期望版本为 -1，则允许创建
        if (current == null) {
            if (expectedVersion == -1) {
                putValue(key, value);
                return true;
            }
            return false;
        }

        // 检查版本是否匹配
        if (current.version() != expectedVersion) {
            log.debug("Version mismatch: key={}, expected={}, actual={}",
                    key, expectedVersion, current.version());
            return false;
        }

        // 版本匹配，更新缓存
        putValue(key, value);
        return true;
    }

    /**
     * 乐观锁更新（带自定义 TTL）
     *
     * @param key 缓存键
     * @param value 新值
     * @param expectedVersion 期望的版本号
     * @param ttl 过期时间
     * @return true 如果更新成功，false 如果版本不匹配
     */
    public boolean putIfVersion(K key, V value, long expectedVersion, Duration ttl) {
        StampedValue<V> current = getStamped(key);

        if (current == null) {
            if (expectedVersion == -1) {
                long version = generateVersion();
                putStamped(key, value, version, ttl);
                return true;
            }
            return false;
        }

        if (current.version() != expectedVersion) {
            log.debug("Version mismatch: key={}, expected={}, actual={}",
                    key, expectedVersion, current.version());
            return false;
        }

        long version = generateVersion();
        putStamped(key, value, version, ttl);
        return true;
    }

    /**
     * 检查版本是否变化
     *
     * <p>比较当前版本与指定版本，判断数据是否已变化。
     *
     * @param key 缓存键
     * @param version 要比较的版本号
     * @return true 如果版本已变化（当前版本 != 指定版本），false 如果版本未变化
     */
    public boolean isVersionChanged(K key, long version) {
        long currentVersion = getVersion(key);
        return currentVersion != version;
    }

    /**
     * 检查版本是否匹配
     *
     * <p>比较当前版本与指定版本，判断数据是否一致。
     *
     * @param key 缓存键
     * @param version 要比较的版本号
     * @return true 如果版本匹配，false 如果版本不匹配
     */
    public boolean isVersionMatch(K key, long version) {
        long currentVersion = getVersion(key);
        return currentVersion == version;
    }

    /**
     * 获取缓存值（带加载器，自动生成版本戳）
     *
     * <p>如果缓存不存在，调用加载器加载数据，并自动生成版本戳。
     *
     * @param key 缓存键
     * @param loader 值加载器
     * @return 缓存值或加载的值
     */
    public V getValue(K key, Callable<V> loader) {
        StampedValue<V> stamped = get(key, () -> {
            try {
                V value = loader.call();
                long version = generateVersion();
                return new StampedValue<>(value, version, Instant.now());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load value", e);
            }
        });
        return stamped != null ? stamped.value() : null;
    }

    /**
     * 批量获取缓存值（仅值，不包含版本戳）
     *
     * @param keys 缓存键集合
     * @return key-value 映射，只包含命中的 key
     */
    public Map<K, V> batchGetValues(Set<K> keys) {
        Map<K, StampedValue<V>> stampedMap = batchGet(keys);
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, StampedValue<V>> entry : stampedMap.entrySet()) {
            StampedValue<V> stamped = entry.getValue();
            if (stamped != null) {
                result.put(entry.getKey(), stamped.value());
            }
        }
        return result;
    }

    /**
     * 批量获取版本戳
     *
     * @param keys 缓存键集合
     * @return key-version 映射，只包含命中的 key
     */
    public Map<K, Long> batchGetVersions(Set<K> keys) {
        Map<K, StampedValue<V>> stampedMap = batchGet(keys);
        Map<K, Long> result = new HashMap<>();
        for (Map.Entry<K, StampedValue<V>> entry : stampedMap.entrySet()) {
            StampedValue<V> stamped = entry.getValue();
            if (stamped != null) {
                result.put(entry.getKey(), stamped.version());
            }
        }
        return result;
    }

    /**
     * 生成新的版本号
     *
     * <p>使用原子递增生成全局唯一的版本号。
     *
     * @return 新的版本号
     */
    protected long generateVersion() {
        return VERSION_GENERATOR.incrementAndGet();
    }

    /**
     * 带版本戳的缓存值
     *
     * <p>封装缓存值和版本信息，支持版本控制和变更检测。
     *
     * @param <V>       值类型
     * @param value     实际值
     * @param version   版本号（单调递增）
     * @param timestamp 创建时间戳
     */
        public record StampedValue<V>(V value, long version, Instant timestamp) {
            /**
             * 构造函数
             *
             * @param value     实际值
             * @param version   版本号
             * @param timestamp 创建时间戳
             */
            public StampedValue(V value, long version, Instant timestamp) {
                this.value = value;
                this.version = version;
                this.timestamp = timestamp != null ? timestamp : Instant.now();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                StampedValue<?> that = (StampedValue<?>) o;
                return version == that.version && Objects.equals(value, that.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(value, version);
            }
        }
}