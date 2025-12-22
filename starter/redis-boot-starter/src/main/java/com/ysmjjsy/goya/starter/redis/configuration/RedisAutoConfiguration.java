package com.ysmjjsy.goya.starter.redis.configuration;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.listener.ICacheInvalidateListener;
import com.ysmjjsy.goya.component.cache.publisher.ICacheInvalidatePublisher;
import com.ysmjjsy.goya.component.cache.service.HybridCacheService;
import com.ysmjjsy.goya.component.cache.service.IL2Cache;
import com.ysmjjsy.goya.component.cache.service.LocalCacheService;
import com.ysmjjsy.goya.starter.redis.configuration.properties.RedisProperties;
import com.ysmjjsy.goya.starter.redis.listener.RedisCacheInvalidateListener;
import com.ysmjjsy.goya.starter.redis.publisher.RedisCacheInvalidatePublisher;
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
 * @since 2025/12/22
 * @see RedisProperties
 * @see RedisCacheService
 * @see IL2Cache
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
     * 注册 Redis L2 缓存服务
     * <p>作为 IL2Cache 接口的 Redis 实现</p>
     * <p>当存在 RedissonClient 时自动注册</p>
     *
     * @param cacheProperties  缓存配置
     * @param redisProperties  Redis 配置
     * @param redissonClient   RedissonClient 实例
     * @return RedisCacheService 实例（实现 IL2Cache）
     */
    @Bean("redisL2CacheService")
    @ConditionalOnMissingBean(IL2Cache.class)
    @Lazy(false)
    public RedisCacheService redisL2CacheService(
            CacheProperties cacheProperties,
            RedisProperties redisProperties,
            RedissonClient redissonClient) {
        RedisCacheService service = new RedisCacheService(
                cacheProperties, redisProperties, redissonClient);
        log.info("[Goya] |- starter [redis] RedisAutoConfiguration |- " +
                "bean [redisL2CacheService] register as IL2Cache implementation.");
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

    /**
     * 注册缓存失效消息发布器
     * <p>用于混合缓存中跨节点的 L1 缓存失效通知</p>
     *
     * @param redissonClient  RedissonClient 实例
     * @param cacheProperties 缓存配置
     * @return RedisCacheInvalidatePublisher 实例
     */
    @Bean
    @ConditionalOnMissingBean(ICacheInvalidatePublisher.class)
    @Lazy(false)
    public RedisCacheInvalidatePublisher redisCacheInvalidatePublisher(
            RedissonClient redissonClient,
            CacheProperties cacheProperties) {
        String topic = cacheProperties.invalidateTopic();
        RedisCacheInvalidatePublisher publisher = new RedisCacheInvalidatePublisher(redissonClient, topic);
        log.trace("[Goya] |- starter [redis] RedisAutoConfiguration |- " +
                "bean [redisCacheInvalidatePublisher] register, topic: {}", topic);
        return publisher;
    }

    /**
     * 注册缓存失效消息监听器
     * <p>监听 Redis Pub/Sub 消息，失效本地 L1 缓存</p>
     *
     * @param redissonClient      RedissonClient 实例
     * @param cacheProperties     缓存配置
     * @param hybridCacheService  混合缓存服务（用于获取 L1 和 nodeId）
     * @return RedisCacheInvalidateListener 实例
     */
    @Bean
    @ConditionalOnMissingBean(ICacheInvalidateListener.class)
    @Lazy(false)
    public RedisCacheInvalidateListener redisCacheInvalidateListener(
            RedissonClient redissonClient,
            CacheProperties cacheProperties,
            @Lazy HybridCacheService hybridCacheService) {
        String topic = cacheProperties.invalidateTopic();
        String nodeId = hybridCacheService.getNodeId();
        LocalCacheService l1Cache = hybridCacheService.getLocalCache();
        RedisCacheInvalidateListener listener = new RedisCacheInvalidateListener(
                redissonClient, topic, l1Cache, nodeId);
        listener.start();
        log.trace("[Goya] |- starter [redis] RedisAutoConfiguration |- " +
                "bean [redisCacheInvalidateListener] register, topic: {}, nodeId: {}", topic, nodeId);
        return listener;
    }
}
