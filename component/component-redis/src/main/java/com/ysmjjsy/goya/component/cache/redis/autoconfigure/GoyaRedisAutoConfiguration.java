package com.ysmjjsy.goya.component.cache.redis.autoconfigure;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.cache.RedissonCacheService;
import com.ysmjjsy.goya.component.cache.redis.codec.TypedJsonMapperCodec;
import com.ysmjjsy.goya.component.cache.redis.support.RedisKeyBuilder;
import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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

    /**
     * 全局 Codec（无类型信息）。
     *
     * @param jsonMapper 项目统一 JsonMapper
     * @return codec
     */
    @Bean
    @ConditionalOnMissingBean
    public TypedJsonMapperCodec typedJsonMapperCodec(JsonMapper jsonMapper) {
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [typedJsonMapperCodec] register.");
        return new TypedJsonMapperCodec(jsonMapper);
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
     * Redis KeyBuilder。
     *
     * @param props 配置项
     * @return keyBuilder
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisKeyBuilder redisKeyBuilder(GoyaRedisProperties props) {
        RedisKeyBuilder redisKeyBuilder = new RedisKeyBuilder(props);
        log.trace("[Goya] |- component [redis] GoyaRedisAutoConfiguration |- bean [redisKeyBuilder] register.");
        return redisKeyBuilder;
    }

    /**
     * 具体远程缓存实现（便于业务直接注入 RedissonCacheService）。
     *
     * @param redisson   redissonClient
     * @param props      配置项
     * @param keyBuilder keyBuilder
     * @return RedissonCacheService
     */
    @Bean
    @ConditionalOnMissingBean(RedissonCacheService.class)
    public RedissonCacheService redissonCacheService(RedissonClient redisson, GoyaRedisProperties props, RedisKeyBuilder keyBuilder) {
        RedissonCacheService redissonCacheService = new RedissonCacheService(redisson, props, keyBuilder);
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
}
