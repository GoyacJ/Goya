package com.ysmjjsy.goya.component.cache.multilevel.configuration.properties;

import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p>多级缓存配置属性</p>
 * <p>支持全局配置和按 cacheName 配置不同的缓存策略</p>
 * <p>配置示例：</p>
 * <pre>{@code
 * goya:
 *   cache:
 *     multi-level:
 *       enabled: true
 *       default:
 *         localTtl: PT5M
 *         remoteTtl: PT30M
 *         localMaximumSize: 10000
 *         enableBloomFilter: true
 *         enableInvalidationNotify: true
 *         allowNullValues: false
 *       cacheConfigs:
 *         userCache:
 *           localTtl: PT10M
 *           remoteTtl: PT1H
 *           localMaximumSize: 5000
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15 11:38
 */
@ConfigurationProperties(prefix = CacheConst.PROPERTY_MULTI_LEVEL)
public record MultiLevelProperties(
        /*
          是否启用多级缓存
         */
        @DefaultValue("true")
        Boolean enabled,

        /*
          默认配置
         */
        MultiLevelCacheConfig defaultConfig,

        /*
          按 cacheName 配置不同的缓存策略
         */
        Map<String, MultiLevelCacheConfig> cacheConfigs
) {

    /**
     * 多级缓存配置
     *
     * @param localTtl                L1 本地缓存 TTL
     * @param remoteTtl               L2 远程缓存 TTL
     * @param localMaximumSize        L1 本地缓存最大容量
     * @param enableBloomFilter       是否启用布隆过滤器
     * @param enableInvalidationNotify 是否启用失效通知
     * @param allowNullValues         是否允许 null 值
     */
    public record MultiLevelCacheConfig(
            /*
              L1 本地缓存 TTL
             */
            @DefaultValue("PT5M")
            Duration localTtl,

            /*
              L2 远程缓存 TTL
             */
            @DefaultValue("PT30M")
            Duration remoteTtl,

            /*
              L1 本地缓存最大容量
             */
            @DefaultValue("10000")
            Long localMaximumSize,

            /*
              是否启用布隆过滤器
             */
            @DefaultValue("true")
            Boolean enableBloomFilter,

            /*
              是否启用失效通知
             */
            @DefaultValue("true")
            Boolean enableInvalidationNotify,

            /*
              是否允许 null 值
             */
            @DefaultValue("false")
            Boolean allowNullValues
    ) {
    }

    /**
     * 获取指定 cacheName 的配置
     *
     * @param cacheName 缓存名称
     * @return 缓存配置，如果不存在则返回默认配置
     */
    public MultiLevelCacheConfig getCacheConfig(String cacheName) {
        if (cacheConfigs != null && cacheConfigs.containsKey(cacheName)) {
            return cacheConfigs.get(cacheName);
        }
        return defaultConfig;
    }

    /**
     * 构建 MultiLevelCacheSpec
     *
     * @param cacheName 缓存名称
     * @return MultiLevelCacheSpec
     */
    public MultiLevelCacheSpec buildSpec(String cacheName) {
        MultiLevelCacheConfig config = getCacheConfig(cacheName);
        return new MultiLevelCacheSpec(
                cacheName,
                config.localTtl(),
                config.remoteTtl(),
                config.localMaximumSize(),
                config.enableBloomFilter(),
                config.enableInvalidationNotify(),
                config.allowNullValues()
        );
    }
}
