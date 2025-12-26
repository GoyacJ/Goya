package com.ysmjjsy.goya.starter.redis.configuration;

import com.ysmjjsy.goya.component.cache.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.serializer.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.serializer.DefaultCacheKeySerializer;
import com.ysmjjsy.goya.starter.redis.codec.TypedJsonMapperCodec;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import com.ysmjjsy.goya.starter.redis.core.RedissonRemoteCache;
import com.ysmjjsy.goya.starter.redis.service.DefaultRedisService;
import com.ysmjjsy.goya.starter.redis.service.IRedisService;
import com.ysmjjsy.goya.starter.redis.subscriber.RedisCacheEvictionSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

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
 * @see RedisProperties
 * @since 2025/12/22
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({RedisProperties.class})
public class RedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- starter [redis] RedisAutoConfiguration auto configure.");
    }

    /**
     * 缓存键序列化器 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheKeySerializer cacheKeySerializer() {
        log.info("Creating CacheKeySerializer");
        return new DefaultCacheKeySerializer();
    }

    /**
     * TypedJsonMapperCodec Bean
     *
     * <p>基于 JsonMapper 的统一序列化 Codec，支持类型信息保存和恢复。
     */
    @Bean
    @ConditionalOnMissingBean
    public TypedJsonMapperCodec typedJsonMapperCodec(JsonMapper jsonMapper) {
        log.info("Creating TypedJsonMapperCodec with JsonMapper");
        return new TypedJsonMapperCodec(jsonMapper);
    }

    /**
     * 远程缓存工厂 Bean
     *
     * <p>使用 Redisson 和 TypedJsonMapperCodec 创建 RemoteCache 实例。
     */
    @Bean
    @ConditionalOnMissingBean(name = "remoteCacheFactory")
    public GoyaCacheManager.RemoteCacheFactory remoteCacheFactory(
            RedissonClient redissonClient,
            JsonMapper jsonMapper,
            CacheKeySerializer cacheKeySerializer) {
        log.info("Creating RemoteCacheFactory with Redisson, JsonMapper and CacheKeySerializer");
        // 创建统一序列化 Codec
        TypedJsonMapperCodec codec = new TypedJsonMapperCodec(jsonMapper);
        return (cacheName, spec) -> {
            log.debug("Creating RedissonRemoteCache: cacheName={}, codec=TypedJsonMapperCodec", cacheName);
            return new RedissonRemoteCache(cacheName, redissonClient, codec, spec, cacheKeySerializer);
        };
    }

    /**
     * Redis 缓存失效订阅器 Bean
     *
     * <p>订阅 Spring 事件并转发到 Redis Pub/Sub。
     * 需要 GoyaCacheManager 用于 L2 失效检查。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(GoyaCacheManager.class)
    public RedisCacheEvictionSubscriber redisCacheEvictionSubscriber(
            RedissonClient redissonClient,
            ApplicationEventPublisher eventPublisher,
            GoyaCacheManager cacheManager) {
        log.info("Creating RedisCacheEvictionSubscriber");
        return new RedisCacheEvictionSubscriber(redissonClient, eventPublisher, cacheManager);
    }

    /**
     * Redis 服务 Bean
     *
     * <p>提供 Redis 特有和高级功能。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public IRedisService redisService(RedissonClient redissonClient) {
        log.info("Creating IRedisService");
        return new DefaultRedisService(redissonClient);
    }
}
