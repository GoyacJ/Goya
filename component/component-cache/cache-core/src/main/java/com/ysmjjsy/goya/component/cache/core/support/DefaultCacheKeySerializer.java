package com.ysmjjsy.goya.component.cache.core.support;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 13:36
 */
@Slf4j
public class DefaultCacheKeySerializer implements CacheKeySerializer{

    @Override
    public <K> byte[] serialize(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            // 优化：常见类型直接序列化
            switch (key) {
                case String k -> {
                    return k.getBytes(StandardCharsets.UTF_8);
                }
                case Long k -> {
                    return String.valueOf(k).getBytes(StandardCharsets.UTF_8);
                }
                case Integer k -> {
                    return String.valueOf(k).getBytes(StandardCharsets.UTF_8);
                }
                default -> {
                    String keyString = GoyaJson.toJson(key);
                    return Base64.getEncoder().encode(keyString.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize cache key: key={}, type={}", key, key.getClass().getName(), e);
            // 降级到 toString()
            return GoyaJson.toJson(key).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public <K> String serializeToString(K key) {
        byte[] keyBytes = serialize(key);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @Override
    public <K> String buildKey(String keyPrefix, String cacheName, K key) {
        String serializedKey = serializeToString(key);
        return keyPrefix + cacheName + SymbolConst.COLON + serializedKey;
    }
}
