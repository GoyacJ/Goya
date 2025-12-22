package com.ysmjjsy.goya.component.cache.warmup;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>缓存预热接口</p>
 * <p>提供缓存预热和刷新功能，优化系统启动和缓存重建场景的性能</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *     <li>系统启动时：批量加载热点数据到缓存</li>
 *     <li>缓存重建时：避免缓存雪崩导致的性能问题</li>
 *     <li>定时刷新：确保缓存数据的实时性</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 */
public interface ICacheWarmup {

    /**
     * <p>预热缓存（批量加载）</p>
     * <p>适用于系统启动或缓存重建时，批量加载热点数据</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * Map<String, User> hotUsers = loadHotUsersFromDB();
     * cacheService.warmup("users", hotUsers);
     * }</pre>
     *
     * @param cacheName 缓存名称
     * @param data      预热数据
     * @param <K>       键类型
     * @param <V>       值类型
     */
    <K, V> void warmup(String cacheName, Map<K, V> data);

    /**
     * <p>异步刷新缓存</p>
     * <p>在后台异步加载最新数据，不阻塞当前请求</p>
     * <p>适用于缓存即将过期或需要更新的场景</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * // 返回旧值给用户，后台异步加载新值
     * V value = cache.get("users", userId);
     * if (needsRefresh(value)) {
     *     cache.refreshAsync("users", userId, this::loadUserFromDB);
     * }
     * return value;
     * }</pre>
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param loader    数据加载函数
     * @param <K>       键类型
     * @param <V>       值类型
     */
    <K, V> void refreshAsync(String cacheName, K key, Function<K, V> loader);

    /**
     * <p>定时刷新缓存</p>
     * <p>按固定间隔定时刷新缓存数据，确保数据实时性</p>
     * <p>适用于需要定期更新的缓存数据（如配置、热榜等）</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * // 每 5 分钟刷新热榜数据
     * cacheService.scheduleRefresh(
     *     "hot-list",
     *     Duration.ofMinutes(5),
     *     this::loadHotListFromDB
     * );
     * }</pre>
     *
     * @param cacheName 缓存名称
     * @param interval  刷新间隔
     * @param loader    数据加载函数（返回批量数据）
     * @param <K>       键类型
     * @param <V>       值类型
     */
    <K, V> void scheduleRefresh(
            String cacheName,
            Duration interval,
            Supplier<Map<K, V>> loader
    );
}

