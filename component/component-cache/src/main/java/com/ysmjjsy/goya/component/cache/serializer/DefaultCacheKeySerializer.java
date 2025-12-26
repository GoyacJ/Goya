package com.ysmjjsy.goya.component.cache.serializer;

import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 默认缓存键序列化器实现
 *
 * <p>提供可靠的 key 序列化策略，支持常见类型的优化序列化。
 *
 * <p><b>序列化策略：</b>
 * <ul>
 *   <li>String 类型：直接使用 UTF-8 编码</li>
 *   <li>Long/Integer 类型：转换为字符串后编码</li>
 *   <li>其他类型：使用 JSON 序列化 + Base64 编码</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:32
 */
public class DefaultCacheKeySerializer implements CacheKeySerializer {

    private static final Logger log = LoggerFactory.getLogger(DefaultCacheKeySerializer.class);

    @Override
    public byte[] serialize(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            // 优化：常见类型直接序列化
            if (key instanceof String k) {
                return k.getBytes(StandardCharsets.UTF_8);
            }

            if (key instanceof Long k) {
                return String.valueOf(k).getBytes(StandardCharsets.UTF_8);
            }

            if (key instanceof Integer k) {
                return String.valueOf(k).getBytes(StandardCharsets.UTF_8);
            }

            // 其他类型：使用 toString() + Base64 编码（避免引入 Jackson 依赖）
            // 未来可以扩展为支持自定义序列化器
            String keyString = JsonUtils.toJson(key);
            return Base64.getEncoder().encode(keyString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to serialize cache key: key={}, type={}", key, key.getClass().getName(), e);
            // 降级到 toString()
            return JsonUtils.toJson(key).getBytes(StandardCharsets.UTF_8);
        }
    }
}
