package com.ysmjjsy.goya.component.cache.redis.configuration;

import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.redis.configuration.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationPublisher;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationSubscriber;
import com.ysmjjsy.goya.component.cache.redis.service.DefaultRedisService;
import com.ysmjjsy.goya.component.cache.redis.service.RedisCacheService;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Redis 自动配置类</p>
 * <p>提供 Redis 作为 L2 分布式缓存的实现</p>
 * <p>装配逻辑：</p>
 * <ul>
 *     <li>当存在 RedissonClient 时，自动注册 RedisCacheService 为 CacheService 实现</li>
 *     <li>提供 Redis 特有功能服务（发布订阅、分布式锁等）</li>
 *     <li>提供缓存失效消息的发布功能</li>
 * </ul>
 *
 * @author goya
 * @see GoyaRedisProperties
 * @since 2025/12/22
 */
@Slf4j
@AutoConfiguration
@ConditionalOnBean(RedissonClient.class)
@EnableConfigurationProperties({GoyaRedisProperties.class})
public class GoyaRedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [redis] GoyaRedisAutoConfiguration auto configure.");
    }

    @Bean
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheManager] register.");
        return cacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(RedisService.class)
    public RedisService redisService(RedissonClient redissonClient, CacheKeySerializer cacheKeySerializer) {
        RedisService redisService = new DefaultRedisService(redissonClient, cacheKeySerializer);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisService] register.");
        return redisService;
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheService.class)
    public RedisCacheService redisCacheService(
            RedissonClient redissonClient,
            CacheKeySerializer cacheKeySerializer,
            GoyaRedisProperties redisProperties) {
        RedisCacheService cacheService = new RedisCacheService(redissonClient, cacheKeySerializer, redisProperties);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheService] register.");
        return cacheService;
    }

    @Bean
    @ConditionalOnMissingBean(RedisInvalidationPublisher.class)
    public RedisInvalidationPublisher redisCacheInvalidationPublisher(
            RedisService redisService,
            CacheKeySerializer cacheKeySerializer) {
        RedisInvalidationPublisher publisher = new RedisInvalidationPublisher(redisService, cacheKeySerializer);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheInvalidationPublisher] register.");
        return publisher;
    }

    @Bean
    @ConditionalOnMissingBean(RedisInvalidationSubscriber.class)
    public RedisInvalidationSubscriber redisCacheInvalidationSubscriber(RedisService redisService) {
        RedisInvalidationSubscriber subscriber = new RedisInvalidationSubscriber(redisService);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheInvalidationSubscriber] register.");
        return subscriber;
    }
}
