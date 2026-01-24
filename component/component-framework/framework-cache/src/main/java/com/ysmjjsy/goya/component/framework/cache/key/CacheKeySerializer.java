package com.ysmjjsy.goya.component.framework.cache.key;

/**
 * <p>缓存key序列化器</p>
 *
 * @author goya
 * @since 2026/1/15 11:48
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
    <K> byte[] serialize(K key);

    /**
     * 将缓存键序列化为字符串
     *
     * @param key 缓存键
     * @param <K> 缓存键类型
     * @return 序列化后的字符串
     */
    <K> String serializeToString(K key);

    /**
     * build key
     *
     * @param keyPrefix 缓存前缀
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param <K> 缓存键类型
     * @return 序列化后的字符串
     */
    <K> String buildKey(String keyPrefix, String cacheName, K key);
}
