package com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.cache.constants.CacheConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>Redis 配置属性</p>
 * <p>支持配置分布式锁超时时间、等待时间等属性</p>
 *
 * @param enabled 总开关
 * @param keyPrefix 全局 key 前缀（建议按环境/应用区分）
 * @param cachePrefix CacheService 的 key 前缀（隔离缓存数据与其它 redis 数据）
 * @param defaultTtl 默认 TTL（put 未指定 ttl 时使用）
 * @param allowNullValues 是否允许缓存 null
 * @param stampedeLockEnabled getOrLoad 是否启用分布式防击穿锁
 * @param stampedeLockWait 防击穿锁等待时间
 * @param stampedeLockLease 防击穿锁租约时间（防止死锁）
 * @author goya
 * @see CacheConst
 * @since 2025/12/22 00:00
 */
@Schema(description = "Redis 配置属性")
@ConfigurationProperties(prefix = CacheConst.PROPERTY_REDIS)
public record GoyaRedisProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("goya") String keyPrefix,
        @DefaultValue("cache") String cachePrefix,
        @DefaultValue("PT10M") Duration defaultTtl,
        @DefaultValue("false") boolean allowNullValues,
        @DefaultValue("true") boolean stampedeLockEnabled,
        @DefaultValue("PT200MS") Duration stampedeLockWait,
        @DefaultValue("PT5S") Duration stampedeLockLease
) {
}

