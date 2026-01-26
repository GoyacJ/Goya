package com.ysmjjsy.goya.component.framework.cache.key;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 13:36
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCacheKeySerializer implements CacheKeySerializer {

    private final GoyaContext goyaContext;

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
        String currentTenant = goyaContext.currentTenant();
        if (StringUtils.isBlank(currentTenant)) {
            currentTenant = DefaultConst.DEFAULT_TENANT_ID;
        }

        return keyPrefix +
                SymbolConst.COLON + normalize(SpringContext.getApplicationName()) +
                SymbolConst.COLON + currentTenant +
                SymbolConst.COLON + cacheName +
                SymbolConst.COLON + serializedKey;
    }

    private static String normalize(String applicationName) {
        if (applicationName == null || applicationName.isBlank()) {
            return applicationName;
        }

        return applicationName
                // 1. 将 - 替换为 _
                .replace('-', '_')
                // 2. 驼峰转下划线（MyAppName → My_App_Name）
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                // 3. 多个下划线压缩成一个（可选但推荐）
                .replaceAll("_+", "_")
                // 4. 转小写
                .toLowerCase();
    }
}
