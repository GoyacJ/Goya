package com.ysmjjsy.goya.component.cache.redis.configuration.properties;

import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>Redis 配置属性</p>
 * <p>支持配置分布式锁超时时间、等待时间等属性</p>
 *
 * @author goya
 * @since 2025/12/22 00:00
 * @see CacheConst
 */
@Schema(description = "Redis 配置属性")
@ConfigurationProperties(prefix = CacheConst.PROPERTY_REDIS)
public record GoyaRedisProperties(

        /*
          Redis Key 前缀
         */
        @DefaultValue(CacheConst.CACHE_PREFIX)
        String keyPrefix,

        /*
          分布式锁超时时间
          获取锁后，如果在此时间内未主动释放，则自动释放
         */
        @DefaultValue("PT30S")
        Duration lockTimeout,

        /*
          分布式锁等待时间
          尝试获取锁时的最大等待时间
         */
        @DefaultValue("PT10S")
        Duration lockWaitTime,

        /*
          看门狗超时时间
          Redisson 的看门狗机制会定期续约锁，此参数设置看门狗的超时时间
         */
        @DefaultValue("PT30S")
        Duration watchdogTimeout,

        /*
          是否启用 Redis 统计
         */
        @DefaultValue("false")
        Boolean enableStats
) {
}

