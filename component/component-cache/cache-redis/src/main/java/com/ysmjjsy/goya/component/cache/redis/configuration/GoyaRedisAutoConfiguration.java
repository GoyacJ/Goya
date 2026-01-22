package com.ysmjjsy.goya.component.cache.redis.configuration;

import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.redis.codec.TypedJsonMapperCodec;
import com.ysmjjsy.goya.component.cache.redis.configuration.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.factory.MultiClusterRemoteCacheFactory;
import com.ysmjjsy.goya.component.cache.redis.service.DefaultRedisService;
import com.ysmjjsy.goya.component.cache.redis.service.IRedisService;
import com.ysmjjsy.goya.component.cache.redis.subscriber.RedisCacheEvictionSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

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
public class GoyaRedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [redis] GoyaRedisAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public RedissonSpringCacheManager redisCacheManager(RedissonClient redissonClient) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisCacheManager] register.");
        return cacheManager;
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
     * 远程缓存工厂 Bean（多集群支持，默认）
     *
     * <p>使用 MultiClusterRemoteCacheFactory 作为默认实现，支持多集群场景。
     * 向后兼容：如果 CacheSpecification 中未指定 clusterName，使用默认集群。
     *
     * <p><b>使用方式：</b>
     * <ul>
     *   <li>单集群场景：无需配置，自动使用默认 RedissonClient</li>
     *   <li>多集群场景：通过 MultiClusterRemoteCacheFactory.registerCluster() 注册额外集群</li>
     *   <li>在 CacheSpecification 中指定 clusterName 选择对应集群</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(GoyaCacheManager.RemoteCacheFactory.class)
    public GoyaCacheManager.RemoteCacheFactory remoteCacheFactory(
            RedissonClient redissonClient,
            JsonMapper jsonMapper,
            CacheKeySerializer cacheKeySerializer,
            IRedisService redisService) {
        log.info("Creating MultiClusterRemoteCacheFactory with default RedissonClient");
        MultiClusterRemoteCacheFactory factory = new MultiClusterRemoteCacheFactory(
                redissonClient, jsonMapper, cacheKeySerializer);
        // 注入 IRedisService（如果可用）
        if (redisService != null) {
            factory.setRedisService(redisService);
            log.debug("IRedisService injected into MultiClusterRemoteCacheFactory");
        }
        log.info("MultiClusterRemoteCacheFactory created. Use registerCluster() to add more clusters.");
        return factory;
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
    public IRedisService redisService(RedissonClient redissonClient, CacheKeySerializer cacheKeySerializer) {
        log.info("Creating IRedisService");
        return new DefaultRedisService(redissonClient, cacheKeySerializer);
    }
}
