package com.ysmjjsy.goya.component.cache.serializer;

/**
 * 缓存键序列化器接口
 *
 * <p>用于将缓存键序列化为字节数组，以便在 Redis 和布隆过滤器中使用。
 * 提供统一的序列化策略，确保 key 的唯一性和可逆性。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>将 Object key 序列化为字节数组</li>
 *   <li>支持常见类型的优化序列化（String、Long 等）</li>
 *   <li>提供可靠的序列化策略，避免 key 冲突</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>Redis key 构建（RedissonRemoteCache）</li>
 *   <li>布隆过滤器 key 序列化（BloomFilterManager）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:31
 */
public interface CacheKeySerializer {

    /**
     * 将缓存键序列化为字节数组
     *
     * <p><b>序列化策略：</b>
     * <ul>
     *   <li>String 类型：直接使用 UTF-8 编码</li>
     *   <li>Long/Integer 类型：转换为字符串后编码</li>
     *   <li>其他类型：使用 JSON 序列化或 Base64 编码</li>
     * </ul>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果序列化失败，抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param key 缓存键
     * @return 序列化后的字节数组
     * @throws RuntimeException 如果序列化失败
     */
    byte[] serialize(Object key);
}