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
        return local.delete(cacheName, key);
    }

    @Override
    public boolean evictRemote(String cacheName, Object key) {
        if (remote == null) {
            return false;
        }
        return remote.delete(cacheName, key);
    }

    @Override
    public <T> T get(String cacheName, Object key) {
        return get(cacheName, key, null);
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
    public <T> Optional<T> getOptional(String cacheName, Object key) {
        return Optional.ofNullable(get(cacheName, key, null));
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
    public boolean delete(String cacheName, Object key) {
        boolean l1 = local.delete(cacheName, key);
        boolean l2 = remote != null && remote.delete(cacheName, key);
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
    public boolean exists(String cacheName, Object key) {
        return Objects.nonNull(get(cacheName, key));
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
    public <T> T getOrLoad(String cacheName, Object key, Supplier<T> loader) {
        return getOrLoad(cacheName, key, null, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Duration ttl, Supplier<T> loader) {
        return getOrLoad(cacheName, key, null, null, loader);
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

    @Override
    public <K, V> boolean putIfAbsent(String cacheName, K key, V value, Duration ttl) {
        // 有 L2：优先 L2，确保跨实例幂等语义
        if (remote != null) {
            boolean ok;
            try {
                ok = remote.putIfAbsent(cacheName, key, value, ttl);
            } catch (Exception e) {
                log.warn("L2 putIfAbsent failed, fallback to L1. cacheName={}, key={}", cacheName, key, e);
                ok = local.putIfAbsent(cacheName, key, value, ttl);
            }
            // 只有真正写入成功才回填 L1，避免把“已存在”的值覆盖/污染本地
            if (ok) {
                local.put(cacheName, key, value, ttl);
            }
            return ok;
        }

        // 无 L2：退化为本地幂等（仅单 JVM 原子）
        return local.putIfAbsent(cacheName, key, value, ttl);
    }

    @Override
    public <K> long incrByWithTtlOnCreate(String cacheName, K key, long delta, Duration ttlOnCreate) {
        // 有 L2：优先 L2，确保分布式原子计数语义
        if (remote != null) {
            long v;
            try {
                v = remote.incrByWithTtlOnCreate(cacheName, key, delta, ttlOnCreate);
            } catch (Exception e) {
                log.warn("L2 incrByWithTtlOnCreate failed, fallback to L1. cacheName={}, key={}", cacheName, key, e);
                v = local.incrByWithTtlOnCreate(cacheName, key, delta, ttlOnCreate);
            }
            // 回填 L1：只写数值，不强行同步 TTL（L1/L2 TTL 允许不同步；L1 作为热点加速层即可）
            local.put(cacheName, key, v, ttlOnCreate);
            return v;
        }

        // 无 L2：退化为本地计数（仅单 JVM 原子）
        return local.incrByWithTtlOnCreate(cacheName, key, delta, ttlOnCreate);
    }

    @Override
    public <K> Long getCounter(String cacheName, K key) {
        // 先读 L1
        Long v1 = local.getCounter(cacheName, key);
        if (v1 != null) {
            return v1;
        }
        if (remote == null) {
            return null;
        }

        // 再读 L2 并回填 L1
        Long v2;
        try {
            v2 = remote.getCounter(cacheName, key);
        } catch (Exception e) {
            log.warn("L2 getCounter failed. cacheName={}, key={}", cacheName, key, e);
            return null;
        }
        if (v2 != null) {
            // 回填时不掌握 L2 剩余 TTL，这里用 null 表示使用 L1 默认策略
            local.put(cacheName, key, v2, null);
        }
        return v2;
    }

    @Override
    public <K> void resetCounter(String cacheName, K key) {
        try {
            local.resetCounter(cacheName, key);
        } catch (Exception e) {
            log.warn("L1 resetCounter failed. cacheName={}, key={}", cacheName, key, e);
        }

        if (remote != null) {
            try {
                remote.resetCounter(cacheName, key);
            } catch (Exception e) {
                log.warn("L2 resetCounter failed. cacheName={}, key={}", cacheName, key, e);
            }
        }
    }

    private void backfillLocal(String cacheName, Object key, Object value, Duration ttl) {
        local.put(cacheName, key, value, ttl);
    }
}
