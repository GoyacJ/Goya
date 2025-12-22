package com.ysmjjsy.goya.component.cache.service;

/**
 * <p>L2 分布式缓存服务接口</p>
 * <p>用于标识分布式缓存实现，如 Redis、MongoDB 等</p>
 * <p>继承 ICacheService 以保持接口一致性</p>
 *
 * <p>实现要求：</p>
 * <ul>
 *     <li>必须支持分布式环境</li>
 *     <li>必须线程安全</li>
 *     <li>建议支持分布式锁</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // Redis 实现
 * public class RedisCacheService extends AbstractCacheService implements IL2Cache {
 *     @Override
 *     public String getCacheType() {
 *         return "redis";
 *     }
 * }
 *
 * // MongoDB 实现
 * public class MongodbCacheService extends AbstractCacheService implements IL2Cache {
 *     @Override
 *     public String getCacheType() {
 *         return "mongodb";
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/22
 * @see ICacheService
 * @see AbstractCacheService
 */
public interface IL2Cache extends ICacheService {

    /**
     * 获取 L2 缓存类型标识
     *
     * @return 缓存类型，如 "redis", "mongodb"
     */
    String getCacheType();

    /**
     * 检查 L2 缓存是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 清空所有缓存（可选实现）
     * <p>用于管理和维护场景</p>
     */
    default void clearAll() {
        // 默认空实现，子类可覆盖
    }
}

