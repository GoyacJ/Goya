package com.ysmjjsy.goya.component.cache.redis.configuration;

import com.ysmjjsy.goya.component.cache.redis.configuration.properties.GoyaRedisProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Redis 自动配置类</p>
 * <p>提供 Redis 作为 L2 分布式缓存的实现</p>
 * <p>装配逻辑：</p>
 * <ul>
 *     <li>当存在 RedissonClient 时，自动注册 RedisCacheService 为 IL2Cache 实现</li>
 *     <li>CacheAutoConfiguration 检测到 IL2Cache 后注册 HybridCacheService</li>
 *     <li>提供 Redis 特有功能服务（发布订阅、分布式锁等）</li>
 *     <li>提供缓存失效消息的发布和监听功能</li>
 * </ul>
 *
 * @author goya
 * @see GoyaRedisProperties
 * @since 2025/12/22
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({GoyaRedisProperties.class})
public class GoyaRedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [redis] RedisAutoConfiguration auto configure.");
    }

    @Bean
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient);
        log.trace("[Goya] |- component [redis] RedisAutoConfiguration |- bean [redisCacheManager] register.");
        return cacheManager;
    }
}
