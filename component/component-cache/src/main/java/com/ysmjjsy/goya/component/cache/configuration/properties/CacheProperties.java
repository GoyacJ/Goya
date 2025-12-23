package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.cache.exception.CacheException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

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
        /**
         * 缓存键前缀
         * 用于区分不同应用的缓存数据
         */
        @Schema(description = "缓存键前缀", example = "goya:")
        @DefaultValue("goya:")
        String cachePrefix,

        /**
         * 默认过期时间
         * 当未指定过期时间时使用此值
         */
        @Schema(description = "默认过期时间", example = "PT30M")
        @DefaultValue("PT30M")
        Duration defaultTtl,

        /**
         * 是否启用缓存统计
         */
        @Schema(description = "是否启用缓存统计", example = "false")
        @DefaultValue("false")
        Boolean enableStats,

        /**
         * 本地缓存（Caffeine）最大容量
         */
        @Schema(description = "本地缓存最大容量", example = "10000")
        @DefaultValue("10000")
        Integer caffeineMaxSize,

        /**
         * 本地缓存（Caffeine）过期时间
         * 如果不设置，使用 defaultTtl
         */
        @Schema(description = "本地缓存过期时间", example = "PT5M")
        Duration caffeineTtl,

        /**
         * 缓存失效消息主题
         */
        @Schema(description = "缓存失效消息主题", example = "cache:invalidate")
        @DefaultValue("cache:invalidate")
        String invalidateTopic
) {

    /**
     * <p>规范化构造器：确保 L1 TTL ≤ L2 TTL</p>
     * <p>防止 L1 TTL 超过 L2 TTL，避免频繁回填导致性能下降</p>
     */
    public CacheProperties {
        // 如果 caffeineTtl 大于 defaultTtl，强制调整为 defaultTtl
        if (caffeineTtl != null && caffeineTtl.compareTo(defaultTtl) > 0) {
            log.warn("[Goya] |- Cache |- caffeineTtl ({}) > defaultTtl ({}), force adjust to defaultTtl",
                    caffeineTtl, defaultTtl);
            caffeineTtl = defaultTtl;
        }
    }

    /**
     * 获取 Caffeine 最大容量，提供默认值
     *
     * @return Caffeine 最大容量
     */
    public Integer caffeineMaxSize() {
        return caffeineMaxSize != null ? caffeineMaxSize : 10000;
    }

    /**
     * <p>获取 Caffeine TTL，确保不超过 L2 TTL</p>
     * <p>双重保证机制：构造器验证 + 访问器验证</p>
     *
     * @return Caffeine TTL
     */
    public Duration caffeineTtl() {
        if (caffeineTtl == null) {
            return defaultTtl;
        }
        // 双重保证：即使构造器验证失败，这里也会兜底
        return caffeineTtl.compareTo(defaultTtl) <= 0 ? caffeineTtl : defaultTtl;
    }

    /**
     * 获取失效消息主题，提供默认值
     *
     * @return 失效消息主题
     */
    public String invalidateTopic() {
        return invalidateTopic != null ? invalidateTopic : "cache:invalidate";
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
