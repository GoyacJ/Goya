package com.ysmjjsy.goya.component.cache.template;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * <p>检查缓存</p>
 *
 * @author goya
 * @since 2025/12/29 16:01
 */
public abstract class AbstractCheckTemplate<K, V> extends AbstractCacheTemplate<K, V> {

    /**
     * 检查缓存
     *
     * @param key   key
     * @param value value
     * @return true or false
     */
    public boolean check(K key, V value) {
        if (ObjectUtils.isEmpty(key)) {
            throw new CommonException("Parameter key is null");
        }

        V v = this.get(key);
        if (ObjectUtils.isEmpty(v)) {
            throw new CommonException("Cache is empty!");
        }

        return Objects.equals(v, value);
    }

    public V put(K key) {
        V v = nextValue(key);
        this.put(key, v);
        return v;
    }

    public V put(K key, Duration expire) {
        V v = nextValue(key);
        this.put(key, v, expire);
        return v;
    }

    public Duration getExpire() {
        Duration ttl = getCacheSpecification().getTtl();
        if (Objects.isNull(ttl)) {
            return getCacheSpecification().getLocalTtl();
        }
        return ttl;
    }

    protected abstract V nextValue(K key);
}
