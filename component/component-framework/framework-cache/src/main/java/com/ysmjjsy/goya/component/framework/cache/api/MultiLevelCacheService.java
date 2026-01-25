package com.ysmjjsy.goya.component.framework.cache.api;

import java.util.Optional;

/**
 * <p>多级缓存服务定义</p>
 * <p>多级缓存包含：</p>
 * <ul>
 *   <li>L1：本地缓存（默认 Caffeine）</li>
 *   <li>L2：远程缓存（由 component-xxx 提供，比如 component-redis）</li>
 * </ul>
 *
 * <p>当系统未引入任何远程缓存实现时，L2 为空，多级缓存自动退化为仅 L1。</p>
 *
 * @author goya
 * @since 2026/1/25 21:38
 */
public interface MultiLevelCacheService extends CacheService {

    /**
     * 获取本地缓存（L1）。
     *
     * @return L1 缓存服务
     */
    CacheService local();

    /**
     * 获取远程缓存（L2）。
     *
     * @return L2 缓存服务（可能为空）
     */
    Optional<CacheService> remote();

    /**
     * 仅清理本地缓存（L1）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @return 是否删除成功
     */
    boolean evictLocal(String cacheName, Object key);

    /**
     * 仅清理远程缓存（L2）。
     *
     * @param cacheName 缓存名
     * @param key 键
     * @return 是否删除成功（无 L2 时恒为 false）
     */
    boolean evictRemote(String cacheName, Object key);
}