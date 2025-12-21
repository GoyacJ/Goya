package com.ysmjjsy.goya.starter.redis.configuration;

import com.ysmjjsy.goya.component.cache.annotation.CacheType;
import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.enums.CacheTypeEnum;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import com.ysmjjsy.goya.starter.redis.service.IRedisService;
import com.ysmjjsy.goya.starter.redis.service.RedisCacheService;
import com.ysmjjsy.goya.starter.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * <p>Redis 自动配置类</p>
 * <p>根据配置和依赖自动注册 Redis 相关服务</p>
 * <ul>
 *     <li>当 Redisson 存在时，自动注册 RedisCacheService</li>
 *     <li>当配置 platform.cache.type=redis 时，使用 Redis 作为缓存实现</li>
 *     <li>提供 Redis 特有功能服务（发布订阅、分布式锁等）</li>
 * </ul>
 *
 * @author goya
 * @see RedisProperties
 * @see RedisCacheService
 * @see RedisService
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- starter [redis] RedisAutoConfiguration auto configure.");
    }

    /**
     * 注册 Redis 缓存服务
     * <p>当存在 RedissonClient 并且配置了 platform.cache.type=redis 时自动注册</p>
     * <p>此服务将替换默认的 Caffeine 缓存服务</p>
     *
     * @param redissonClient RedissonClient 实例
     * @return RedisCacheService 实例
     */
    @Bean
    @ConditionalOnMissingBean(ICacheService.class)
    @CacheType(CacheTypeEnum.REDIS)
    @Lazy(false)
    public RedisCacheService redisCacheService(CacheProperties cacheProperties,
                                               RedisProperties redisProperties,
                                               RedissonClient redissonClient) {
        RedisCacheService service = new RedisCacheService(cacheProperties,
                redisProperties,
                redissonClient
        );
        log.trace("[Goya] |- starter [redis] RedisAutoConfiguration |- bean [redisCacheService] register.");
        return service;
    }

    /**
     * 注册 Redis 特有功能服务
     * <p>提供 Redis 独有的分布式功能</p>
     * <ul>
     *     <li>原子计数器</li>
     *     <li>发布订阅</li>
     *     <li>分布式信号量</li>
     *     <li>分布式倒计时门闩</li>
     * </ul>
     *
     * @param redissonClient RedissonClient 实例
     * @return RedisService 实例
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean(IRedisService.class)
    public RedisService redisService(RedissonClient redissonClient) {
        RedisService service = new RedisService(redissonClient);
        log.trace("[Goya] |- starter [redis] RedisAutoConfiguration |- bean [redisService] register.");
        return service;
    }
}
