package com.ysmjjsy.goya.component.framework.cache.multi;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.api.MultiLevelCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 21:40
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMultiLevelCacheService implements MultiLevelCacheService {

    private final CacheService local;
    private final CacheService remote;

    @Override
    public CacheService local() {
        return local;
    }

    @Override
    public Optional<CacheService> remote() {
        return Optional.ofNullable(remote);
    }

    @Override
    public boolean evictLocal(String cacheName, Object key) {
        return local.evict(cacheName, key);
    }

    @Override
    public boolean evictRemote(String cacheName, Object key) {
        if (remote == null) {
            return false;
        }
        return remote.evict(cacheName, key);
    }

    @Override
    public <T> T get(String cacheName, Object key, Class<T> type) {
        T v = local.get(cacheName, key, type);
        if (v != null) {
            return v;
        }
        if (remote == null) {
            return null;
        }
        T rv = remote.get(cacheName, key, type);
        if (rv != null) {
            backfillLocal(cacheName, key, rv, null);
        }
        return rv;
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
        if (remote == null) {
            local.put(cacheName, key, value, ttl);
            return;
        }
        // 先写远程再写本地：保证跨实例可见性优先
        remote.put(cacheName, key, value, ttl);
        local.put(cacheName, key, value, ttl);
    }

    @Override
    public boolean evict(String cacheName, Object key) {
        boolean l1 = local.evict(cacheName, key);
        boolean l2 = remote != null && remote.evict(cacheName, key);
        return l1 || l2;
    }

    @Override
    public void clear(String cacheName) {
        local.clear(cacheName);
        if (remote != null) {
            remote.clear(cacheName);
        }
    }

    @Override
    public Map<Object, Object> getAll(String cacheName, Collection<?> keys) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<Object, Object> l1 = local.getAll(cacheName, keys);
        if (remote == null) {
            return l1;
        }

        // 计算未命中的 key
        Set<Object> miss = new LinkedHashSet<>();
        for (Object k : keys) {
            if (!l1.containsKey(k)) {
                miss.add(k);
            }
        }
        if (miss.isEmpty()) {
            return l1;
        }

        Map<Object, Object> l2 = remote.getAll(cacheName, miss);
        if (l2.isEmpty()) {
            return l1;
        }

        // 回填 L1
        for (Map.Entry<Object, Object> e : l2.entrySet()) {
            backfillLocal(cacheName, e.getKey(), e.getValue(), null);
        }

        Map<Object, Object> out = LinkedHashMap.newLinkedHashMap(l1.size() + l2.size());
        out.putAll(l1);
        out.putAll(l2);
        return out;
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Objects.requireNonNull(loader, "loader 不能为空");

        // 先走多级 get（会回填）
        T existed = get(cacheName, key, type);
        if (existed != null) {
            return existed;
        }

        // 有 L2：优先交给 L2（它可以做分布式防击穿）
        if (remote != null) {
            T loaded = remote.getOrLoad(cacheName, key, type, ttl, loader);
            if (loaded != null) {
                backfillLocal(cacheName, key, loaded, ttl);
            }
            return loaded;
        }

        // 无 L2：退化为本地
        return local.getOrLoad(cacheName, key, type, ttl, loader);
    }

    private void backfillLocal(String cacheName, Object key, Object value, Duration ttl) {
        local.put(cacheName, key, value, ttl);
    }
}
