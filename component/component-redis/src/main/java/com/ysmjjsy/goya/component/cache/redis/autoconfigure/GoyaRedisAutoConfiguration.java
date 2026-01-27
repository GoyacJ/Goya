package com.ysmjjsy.goya.component.cache.redis.autoconfigure;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.cache.RedissonCacheService;
import com.ysmjjsy.goya.component.cache.redis.key.RedisKeySupport;
import com.ysmjjsy.goya.component.cache.redis.support.*;
import com.ysmjjsy.goya.component.cache.redis.support.impl.*;
import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.constants.CacheConst;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

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
@EnableConfigurationProperties({GoyaRedisProperties.class})
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean({RedissonClient.class, CacheKeySerializer.class})
@ConditionalOnProperty(prefix = CacheConst.PROPERTY_REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoyaRedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [redis] GoyaRedisAutoConfiguration auto configure.");
    }

    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer(ObjectMapper objectMapper) {
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redissonCustomizer] register.");
        return config -> config.setCodec(new JsonJackson3Codec(objectMapper));
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheManager] register.");
        return cacheManager;
    }

    /**
     * 具体远程缓存实现（便于业务直接注入 RedissonCacheService）。
     *
     * @param redisson           redissonClient
     * @param props              配置项
     * @param cacheKeySerializer cacheKeySerializer
     * @return RedissonCacheService
     */
    @Bean
    @ConditionalOnMissingBean(RedissonCacheService.class)
    public RedissonCacheService redissonCacheService(RedissonClient redisson, GoyaRedisProperties props, CacheKeySerializer cacheKeySerializer) {
        RedissonCacheService redissonCacheService = new RedissonCacheService(redisson, props, cacheKeySerializer);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redissonCacheService] register.");
        return redissonCacheService;
    }

    /**
     * 远程缓存服务（固定 Bean 名：remoteCacheService）。
     *
     * <p>framework-cache 会按该名称识别并作为 L2 注入多级缓存。</p>
     *
     * @param impl 具体实现
     * @return CacheService（远程）
     */
    @Bean(name = "remoteCacheService")
    @ConditionalOnMissingBean(name = "remoteCacheService")
    public CacheService remoteCacheService(RedissonCacheService impl) {
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [remoteCacheService] register.");
        return impl;
    }

    /**
     * Redis key 支持（统一命名空间 + 租户隔离）。
     *
     * @param cacheKeySerializer key 序列化器
     * @param goyaRedisProperties Redis 配置（提供 keyPrefix）
     * @return RedisKeySupport
     */
    @Bean
    public RedisKeySupport redisKeySupport(CacheKeySerializer cacheKeySerializer, GoyaRedisProperties goyaRedisProperties) {
        RedisKeySupport redisKeySupport = new RedisKeySupport(cacheKeySerializer, goyaRedisProperties);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisKeySupport] register.");
        return redisKeySupport;
    }

    /**
     * Topic 服务。
     *
     * @param redisson RedissonClient
     * @param keys key 支持
     * @return RedisTopicService
     */
    @Bean
    @ConditionalOnMissingBean(RedisTopicService.class)
    public RedisTopicService redisTopicService(RedissonClient redisson, RedisKeySupport keys) {
        RedissonTopicService redissonTopicService = new RedissonTopicService(redisson, keys);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisTopicService] register.");
        return redissonTopicService;
    }

    /**
     * 延迟队列服务。
     *
     * @param redisson RedissonClient
     * @param keys key 支持
     * @return RedisDelayedQueueService
     */
    @Bean
    @ConditionalOnMissingBean(RedisDelayedQueueService.class)
    public RedisDelayedQueueService redisDelayedQueueService(RedissonClient redisson, RedisKeySupport keys) {
        RedissonDelayedQueueService redissonDelayedQueueService = new RedissonDelayedQueueService(redisson, keys);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisDelayedQueueService] register.");
        return redissonDelayedQueueService;
    }

    /**
     * 延迟队列服务（RReliableQueue）
     *
     * @param redissonClient Redisson 客户端
     * @param codec 统一 Codec
     * @return RedisDelayedQueueService
     */
    @Bean
    @ConditionalOnMissingBean(RedisReliableDelayedQueueService.class)
    public RedisReliableDelayedQueueService redisReliableDelayedQueueService(RedissonClient redissonClient, Codec codec) {
        RedissonReliableDelayedQueueService redissonReliableDelayedQueueService = new RedissonReliableDelayedQueueService(redissonClient, codec);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisReliableDelayedQueueService] register.");
        return redissonReliableDelayedQueueService;
    }

    /**
     * 布隆过滤器服务。
     *
     * @param redisson RedissonClient
     * @param keys key 支持
     * @return RedisBloomFilterService
     */
    @Bean
    @ConditionalOnMissingBean(RedisBloomFilterService.class)
    public RedisBloomFilterService redisBloomFilterService(RedissonClient redisson, RedisKeySupport keys) {
        RedissonBloomFilterService redissonBloomFilterService = new RedissonBloomFilterService(redisson, keys);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisBloomFilterService] register.");
        return redissonBloomFilterService;
    }

    /**
     * 原子计数服务。
     *
     * @param redisson RedissonClient
     * @param keys key 支持
     * @return RedisAtomicService
     */
    @Bean
    @ConditionalOnMissingBean(RedisAtomicService.class)
    public RedisAtomicService redisAtomicService(RedissonClient redisson, RedisKeySupport keys) {
        RedissonAtomicService redissonAtomicService = new RedissonAtomicService(redisson, keys);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisAtomicService] register.");
        return redissonAtomicService;
    }
}
