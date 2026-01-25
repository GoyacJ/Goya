package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * <p>基于 Spring CacheManager 的 CacheService 实现（Caffeine）</p>
 *
 * <p>复用同一底层缓存：CacheService 与 @Cacheable 用的是同一个 CacheManager。</p>
 *
 * @author goya
 * @since 2026/1/12 22:54
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineCacheService implements CacheService {

    /**
     * 本地版本号表：key = tenantId + ":" + cacheName
     */
    private static final ConcurrentMap<String, AtomicLong> LOCAL_VERSIONS = new ConcurrentHashMap<>();

    /**
     * Spring CacheManager（底层为 CaffeineCache / 你的 GoyaCaffeineCache）。
     */
    private final CacheManager cacheManager;

    /**
     * 缓存 key 序列化器（统一构建最终 key）。
     */
    private final CacheKeySerializer cacheKeySerializer;

    private final GoyaContext goyaContext;

    /**
     * 缓存 keyPrefix（例如：goya 或 goya:appName）。
     *
     * <p>建议与你 L2 的 keyPrefix 保持一致。</p>
     */
    private static final String KEY_PREFIX = "goya";

    @Override
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        String internalKey = buildInternalKey(cacheName, key);
        return cache.get(internalKey, type);
    }

    @Override
    public <T> Optional<T> getOptional(String cacheName, Object key, Class<T> type) {
        return Optional.ofNullable(get(cacheName, key, type));
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        put(cacheName, key, value, null);
    }

    @Override
    public void put(String cacheName, Object key, Object value, Duration ttl) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        String internalKey = buildInternalKey(cacheName, key);

        if (ttl != null && cache instanceof GoyaCaffeineCache gc) {
            gc.put(internalKey, value, ttl);
            return;
        }
        cache.put(internalKey, value);
    }

    @Override
    public boolean evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }
        String internalKey = buildInternalKey(cacheName, key);
        return cache.evictIfPresent(internalKey);
    }

    @Override
    public void clear(String cacheName) {
        // 只清当前租户：递增本地版本号即可
        incrementLocalVersion(cacheName);
    }

    @Override
    public Map<Object, Object> getAll(String cacheName, Collection<?> keys) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null || keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<Object, Object> out = new LinkedHashMap<>();
        for (Object k : keys) {
            if (k == null) {
                continue;
            }
            String internalKey = buildInternalKey(cacheName, k);
            Cache.ValueWrapper vw = cache.get(internalKey);
            if (vw != null) {
                // 返回给 MultiLevel 的 key 必须是“原始 key”，否则 miss 计算会错误
                out.put(k, vw.get());
            }
        }
        return out;
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Objects.requireNonNull(loader, "loader 不能为空");
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return loader.get();
        }

        String internalKey = buildInternalKey(cacheName, key);

        T existed = cache.get(internalKey, type);
        if (existed != null) {
            return existed;
        }

        // 简单实现：加载后写入（并发下可能重复加载；若你需要更强收敛，可基于 Caffeine 原生 get(key, mappingFunction) 优化）
        T loaded = loader.get();
        if (ttl != null && cache instanceof GoyaCaffeineCache gc) {
            gc.put(internalKey, loaded, ttl);
        } else {
            cache.put(internalKey, loaded);
        }
        return loaded;
    }

    /**
     * 构建本地缓存内部 key（租户隔离 + 版本号）。
     *
     * <p>内部 key = buildKey( tenantPrefix(keyPrefix), cacheName:v{localVer}, key )</p>
     *
     * @param cacheName 缓存名
     * @param key 业务 key
     * @return 内部 key（String）
     */
    private String buildInternalKey(String cacheName, Object key) {
        if (!StringUtils.hasText(cacheName)) {
            throw new IllegalArgumentException("cacheName 不能为空");
        }
        if (key == null) {
            throw new IllegalArgumentException("key 不能为空");
        }

        String tenantId = goyaContext.currentTenant();

        long ver = currentLocalVersion(cacheName, tenantId);
        String effectiveCacheName = cacheName + ":v" + ver;

        String internalKey = cacheKeySerializer.buildKey(KEY_PREFIX, effectiveCacheName, key);
        if (!StringUtils.hasText(internalKey)) {
            throw new IllegalStateException("本地缓存 key 构建失败，cacheName=" + cacheName + " keyType=" + key.getClass().getName());
        }
        return internalKey;
    }

    /**
     * 获取当前租户下某个 cacheName 的本地版本号。
     *
     * @param cacheName 缓存名
     * @param tenantId 租户
     * @return 版本号（默认 1）
     */
    private long currentLocalVersion(String cacheName, String tenantId) {
        AtomicLong c = LOCAL_VERSIONS.computeIfAbsent(tenantId + ":" + cacheName, k -> new AtomicLong(1L));
        long v = c.get();
        return (v <= 0) ? 1L : v;
    }

    /**
     * 递增当前租户下某个 cacheName 的本地版本号，实现“只清当前租户”。
     *
     * @param cacheName 缓存名
     */
    private void incrementLocalVersion(String cacheName) {
        String tenantId = goyaContext.currentTenant();
        LOCAL_VERSIONS.computeIfAbsent(tenantId + ":" + cacheName, k -> new AtomicLong(1L)).incrementAndGet();
    }
}
