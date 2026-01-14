package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.CacheConst;
import com.ysmjjsy.goya.component.cache.enums.CacheLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p>缓存配置属性</p>
 * <p>支持配置键前缀、默认过期时间、本地缓存参数等</p>
 *
 * @author goya
 * @see CacheConst
 * @since 2025/12/22
 */
@Slf4j
@Schema(description = "缓存配置属性")
@ConfigurationProperties(prefix = CacheConst.PROPERTY_CACHE)
public record CacheProperties(
        /*
          缓存键前缀（全局配置）
          默认值："cache:"
         */
        @Schema(description = "缓存键前缀")
        @DefaultValue("cache:")
        String keyPrefix,

        /*
          默认配置
         */
        @Schema(description = "默认配置")
        @DefaultValue
        CacheConfig defaultConfig,

        /*
          按 cacheName 的特定配置
          Key: cacheName
          Value: CacheConfig
         */
        @Schema(description = "特定配置")
        @DefaultValue
        Map<String, CacheConfig> caches
) {

    /**
     * 获取指定 cacheName 的配置
     *
     * @param cacheName 缓存名称
     * @return 配置对象，如果不存在则返回默认配置
     */
    public CacheConfig getCacheConfig(String cacheName) {
        if (cacheName == null) {
            return defaultConfig;
        }
        CacheConfig specificConfig = caches.get(cacheName);
        return specificConfig != null ? specificConfig : defaultConfig;
    }

    /**
     * 单个缓存的配置
     */
    public record CacheConfig(
            /*
              L2 缓存的过期时间
             */
            @Schema(description = "L2缓存的过期时间")
            @DefaultValue("PT1H")
            Duration ttl,

            /*
              是否允许缓存 null 值
             */
            @Schema(description = "是否允许缓存null值")
            @DefaultValue("true")
            boolean allowNullValues,

            /*
              本地缓存最大容量（条目数）
             */
            @Schema(description = "本地缓存最大容量")
            @DefaultValue("10000")
            long localMaxSize,

            /*
              L1 TTL 策略配置
             */
            @Schema(description = "L1TTL策略配置")
            TtlStrategyConfig localTtlStrategy,

            /*
              缓存级别：L1_ONLY（仅本地）、L2_ONLY（仅远程）、L1_L2（多级缓存）
             */
            @Schema(description = "缓存级别")
            @DefaultValue("L1_L2")
            CacheLevel cacheLevel,

            /*
              是否启用布隆过滤器
             */
            @Schema(description = "是否启用布隆过滤器")
            @DefaultValue("false")
            boolean enableBloomFilter,

            /*
              布隆过滤器预期插入量
             */
            @Schema(description = "布隆过滤器预期插入量")
            @DefaultValue("1000000")
            long bloomFilterExpectedInsertions,

            /*
              布隆过滤器误判率
             */
            @Schema(description = "布隆过滤器误判率")
            @DefaultValue("0.03")
            double bloomFilterFalsePositiveRate,

            /*
              布隆过滤器是否持久化（未来扩展）
             */
            @Schema(description = "布隆过滤器是否持久化")
            @DefaultValue("false")
            boolean bloomFilterPersistent,

            /*
              降级策略类型
             */
            @Schema(description = "降级策略类型")
            @DefaultValue("degrade-to-l1")
            String fallbackStrategy
    ) {
    }

    /**
     * TTL 策略配置
     */
    public record TtlStrategyConfig(
            /*
              策略类型：fixed-ratio, fixed-duration, independent
             */
            @Schema(description = "策略类型")
            @DefaultValue("fixed-ratio")
            String type,

            /*
              固定比例（用于 fixed-ratio 策略）
             */
            @Schema(description = "固定比例")
            @DefaultValue("0.8")
            double ratio,

            /*
              固定时间（用于 fixed-duration 或 independent 策略）
             */
            @Schema(description = "固定时间")
            Duration duration
    ) {
    }
}
