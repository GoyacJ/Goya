package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>缓存配置属性</p>
 * <p>支持配置键前缀、默认过期时间、本地缓存参数等</p>
 *
 * @author goya
 * @see ICacheConstants
 * @since 2025/12/22
 */
@Slf4j
@Schema(description = "缓存配置属性")
@ConfigurationProperties(prefix = ICacheConstants.PROPERTY_CACHE)
public record CacheProperties(
        /*
          缓存键前缀
          用于区分不同应用的缓存数据
         */
        @Schema(description = "缓存键前缀", example = "goya:")
        @DefaultValue("goya:")
        String cachePrefix,

        @Schema(description = "默认配置")
        @DefaultValue
        CacheConfig defaultConfig,

        @Schema(description = "每个缓存名称的独立配置")
        @DefaultValue
        Map<String, CacheConfig> caches
) {

    public record CacheConfig(
            /*
              默认过期时间
              当未指定过期时间时使用此值
             */
            @Schema(description = "默认过期时间", example = "PT30M")
            @DefaultValue("PT30M")
            Duration defaultTtl,

            @Schema(description = "是否允许 null 值")
            @DefaultValue("true")
            Boolean allowNullValues,

            /*
              是否启用缓存统计
             */
            @Schema(description = "是否启用缓存统计", example = "false")
            @DefaultValue("false")
            Boolean enableStats,

            /*
              本地缓存（Caffeine）最大容量
             */
            @Schema(description = "本地缓存最大容量", example = "10000")
            @DefaultValue("10000")
            Integer caffeineMaxSize,

            @Schema(description = "是否开启缓存穿透保护")
            @DefaultValue("false")
            Boolean penetrationProtect,

            @Schema(description = "缓存穿透保护有效期")
            Duration penetrationProtectTimeout,

            /*
              缓存失效消息主题
             */
            @Schema(description = "缓存失效消息主题", example = "cache:invalidate")
            @DefaultValue("cache:invalidate")
            String invalidateTopic,

            /*
              布隆过滤器预期插入数
              用于初始化布隆过滤器，影响内存占用和误判率
             */
            @Schema(description = "布隆过滤器预期插入数", example = "100000")
            @DefaultValue("100000")
            Long bloomFilterExpectedInsertions,

            /*
              布隆过滤器误判率（False Positive Probability）
              范围：0.0 - 1.0，值越小误判率越低，但内存占用越大
             */
            @Schema(description = "布隆过滤器误判率", example = "0.01")
            @DefaultValue("0.01")
            Double bloomFilterFpp
    ) {

    }

    /**
     * <p>获取指定缓存名称的配置</p>
     * <p>直接使用 cacheName 作为 key 从 caches Map 中查找，不进行前缀匹配</p>
     *
     * @param cacheName 缓存名称（原始名称，如 "userCache"）
     * @return 缓存配置，如果不存在则返回 null
     */
    public CacheConfig getCacheConfig(String cacheName) {
        if (MapUtils.isNotEmpty(caches) && StringUtils.isNotBlank(cacheName)) {
            // 直接使用 cacheName 作为 key 查找，而不是 buildCachePrefix 后的结果
            return caches.get(cacheName);
        }
        return null;
    }

    public CacheConfig getCacheConfigByDefault(String cacheName) {
        CacheConfig cacheConfig = getCacheConfig(cacheName);
        if (Objects.isNull(cacheConfig)) {
            return defaultConfig;
        }
        return cacheConfig;
    }

    /**
     * <p>永不过期的 TTL 标识值</p>
     * <p>使用 100 年作为永不过期的标识（实际不会真的等100年，只是表示永不过期）</p>
     */
    public static final Duration ETERNAL = Duration.ofDays(36500);

    /**
     * <p>判断给定的 TTL 是否为永不过期</p>
     *
     * @param ttl TTL 值
     * @return true 表示永不过期
     */
    public static boolean isEternal(Duration ttl) {
        return ttl != null && ttl.compareTo(ETERNAL) >= 0;
    }


    /**
     * 获取 Caffeine 最大容量，提供默认值
     *
     * @return Caffeine 最大容量
     */
    public Integer caffeineMaxSize() {
        return defaultConfig.caffeineMaxSize != null ? defaultConfig.caffeineMaxSize : 10000;
    }

    /**
     * 获取失效消息主题，提供默认值
     *
     * @return 失效消息主题
     */
    public String invalidateTopic() {
        return defaultConfig.invalidateTopic != null ? defaultConfig.invalidateTopic : "cache:invalidate";
    }

    /**
     * 构建完整的缓存前缀
     * 格式: {cachePrefix}{cacheName}
     *
     * @param cacheName 缓存名称
     * @return 完整的缓存键
     */

    public String buildCachePrefix(String cacheName) {
        if (StringUtils.isBlank(cacheName)) {
            throw new CacheException("Cache name cannot be blank");
        }
        return ICacheConstants.CACHE_PREFIX + cachePrefix + cacheName + ICacheConstants.CACHE_SEPARATOR;
    }

    /**
     * 构建分布式锁的完整键
     * 格式: cache:{keyPrefix}lock:{cacheName}:{key}
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return 完整的锁键
     */
    public String buildLockKey(String cacheName, Object key) {
        if (key == null) {
            throw new CacheException("Cache key cannot be null");
        }

        return ICacheConstants.CACHE_PREFIX + cachePrefix + "lock"
                + ICacheConstants.CACHE_SEPARATOR + cacheName
                + ICacheConstants.CACHE_SEPARATOR + key;
    }
}
