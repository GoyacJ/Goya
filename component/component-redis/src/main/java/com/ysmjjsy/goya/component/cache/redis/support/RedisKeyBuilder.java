package com.ysmjjsy.goya.component.cache.redis.support;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import lombok.RequiredArgsConstructor;

/**
 * <p>Redis Key 构建器。</p>
 *
 * <p>默认格式：</p>
 * <pre>
 * {keyPrefix}:{namespace}:{name}:{subKey...}
 * </pre>
 *
 * @author goya
 * @since 2026/1/25 21:51
 */
@RequiredArgsConstructor
public class RedisKeyBuilder {

    private final GoyaRedisProperties props;

    /**
     * 一个 cacheName 对应一个 RMapCache。
     *
     * @param cacheName 缓存名
     * @return mapCache 名称
     */
    public String cacheMapName(String cacheName) {
        return join(props.keyPrefix(), props.cachePrefix(), cacheName);
    }

    /**
     * 防击穿锁 key。
     *
     * @param cacheName 缓存名
     * @param key 业务 key
     * @return lock key
     */
    public String stampedeLockKey(String cacheName, Object key) {
        return join(props.keyPrefix(), props.cachePrefix(), cacheName, String.valueOf(key), "lock");
    }

    private String join(String... parts) {
        StringBuilder sb = new StringBuilder(64);
        for (String p : parts) {
            if (p == null || p.isBlank()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(':');
            }
            sb.append(p.trim());
        }
        return sb.toString();
    }

}
