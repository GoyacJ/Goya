package com.ysmjjsy.goya.starter.redis.factory;

import com.ysmjjsy.goya.component.cache.core.GoyaCacheManager;
import com.ysmjjsy.goya.component.cache.core.RemoteCache;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.serializer.CacheKeySerializer;
import com.ysmjjsy.goya.starter.redis.codec.TypedJsonMapperCodec;
import com.ysmjjsy.goya.starter.redis.core.RedissonRemoteCache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多集群远程缓存工厂
 *
 * <p>支持多个 Redis 集群的远程缓存工厂实现。根据 CacheSpecification 中的 clusterName
 * 选择对应的 RedissonClient 实例创建 RemoteCache。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>支持多 Redis 集群场景（如：主集群、从集群、不同业务集群）</li>
 *   <li>支持多租户场景（不同租户使用不同的 Redis 集群）</li>
 *   <li>保持向后兼容（clusterName 为 null 时使用默认集群）</li>
 * </ul>
 *
 * <p><b>工作原理：</b>
 * <ol>
 *   <li>注册多个 RedissonClient 实例（通过 clusterName 标识）</li>
 *   <li>创建 RemoteCache 时，从 CacheSpecification 读取 clusterName</li>
 *   <li>根据 clusterName 选择对应的 RedissonClient</li>
 *   <li>如果 clusterName 为 null，使用默认 RedissonClient</li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储客户端映射，线程安全</li>
 *   <li>支持并发注册和查询</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class MultiClusterRemoteCacheFactory implements GoyaCacheManager.RemoteCacheFactory {

    /**
     * 默认集群名称
     */
    private static final String DEFAULT_CLUSTER_NAME = "default";

    /**
     * RedissonClient 映射
     * Key: 集群名称
     * Value: RedissonClient 实例
     */
    private final Map<String, RedissonClient> clients = new ConcurrentHashMap<>();

    /**
     * 默认 RedissonClient（用于向后兼容）
     */
    private final RedissonClient defaultClient;

    /**
     * JsonMapper 实例
     */
    private final JsonMapper jsonMapper;

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer cacheKeySerializer;

    /**
     * 构造函数
     *
     * @param defaultClient 默认 RedissonClient（用于向后兼容）
     * @param jsonMapper JsonMapper 实例
     * @param cacheKeySerializer 缓存键序列化器
     * @throws IllegalArgumentException 如果 defaultClient、jsonMapper 或 cacheKeySerializer 为 null
     */
    public MultiClusterRemoteCacheFactory(
            RedissonClient defaultClient,
            JsonMapper jsonMapper,
            CacheKeySerializer cacheKeySerializer) {
        if (defaultClient == null) {
            throw new IllegalArgumentException("Default RedissonClient cannot be null");
        }
        if (jsonMapper == null) {
            throw new IllegalArgumentException("JsonMapper cannot be null");
        }
        if (cacheKeySerializer == null) {
            throw new IllegalArgumentException("CacheKeySerializer cannot be null");
        }
        this.defaultClient = defaultClient;
        this.jsonMapper = jsonMapper;
        this.cacheKeySerializer = cacheKeySerializer;
        // 注册默认集群
        this.clients.put(DEFAULT_CLUSTER_NAME, defaultClient);
    }

    /**
     * 注册 Redis 集群
     *
     * <p>将 RedissonClient 注册到指定的集群名称。如果集群名称已存在，将覆盖原有客户端。
     *
     * @param clusterName 集群名称
     * @param client RedissonClient 实例
     * @throws IllegalArgumentException 如果 clusterName 或 client 为 null
     */
    public void registerCluster(String clusterName, RedissonClient client) {
        if (clusterName == null || clusterName.isEmpty()) {
            throw new IllegalArgumentException("ClusterName cannot be null or empty");
        }
        if (client == null) {
            throw new IllegalArgumentException("RedissonClient cannot be null");
        }
        clients.put(clusterName, client);
        if (log.isInfoEnabled()) {
            log.info("Registered Redis cluster: clusterName={}", clusterName);
        }
    }

    /**
     * 注销 Redis 集群
     *
     * <p>从注册表中移除指定的集群。不允许注销默认集群。
     *
     * @param clusterName 集群名称
     * @throws IllegalArgumentException 如果尝试注销默认集群
     */
    public void unregisterCluster(String clusterName) {
        if (clusterName == null || clusterName.isEmpty()) {
            throw new IllegalArgumentException("ClusterName cannot be null or empty");
        }
        if (DEFAULT_CLUSTER_NAME.equals(clusterName)) {
            throw new IllegalArgumentException("Cannot unregister default cluster");
        }
        RedissonClient removed = clients.remove(clusterName);
        if (removed != null && log.isInfoEnabled()) {
            log.info("Unregistered Redis cluster: clusterName={}", clusterName);
        }
    }

    /**
     * 获取指定集群的 RedissonClient
     *
     * @param clusterName 集群名称（如果为 null，返回默认客户端）
     * @return RedissonClient 实例
     * @throws IllegalStateException 如果集群不存在
     */
    public RedissonClient getClient(String clusterName) {
        if (clusterName == null || clusterName.isEmpty()) {
            return defaultClient;
        }
        RedissonClient client = clients.get(clusterName);
        if (client == null) {
            throw new IllegalStateException("Redis cluster not found: " + clusterName);
        }
        return client;
    }

    @Override
    public RemoteCache create(String cacheName, CacheSpecification spec) {
        if (cacheName == null) {
            throw new IllegalArgumentException("CacheName cannot be null");
        }
        if (spec == null) {
            throw new IllegalArgumentException("CacheSpecification cannot be null");
        }

        // 从配置中读取集群名称
        String clusterName = spec.getClusterName();
        RedissonClient client = getClient(clusterName);

        // 创建统一序列化 Codec
        TypedJsonMapperCodec codec = new TypedJsonMapperCodec(jsonMapper);

        if (log.isDebugEnabled()) {
            log.debug("Creating RedissonRemoteCache: cacheName={}, clusterName={}, codec=TypedJsonMapperCodec",
                    cacheName, clusterName != null ? clusterName : DEFAULT_CLUSTER_NAME);
        }

        return new RedissonRemoteCache(cacheName, client, codec, spec, cacheKeySerializer);
    }

    /**
     * 获取已注册的集群数量
     *
     * @return 集群数量（包括默认集群）
     */
    public int getClusterCount() {
        return clients.size();
    }

    /**
     * 检查集群是否已注册
     *
     * @param clusterName 集群名称
     * @return true 如果集群已注册
     */
    public boolean hasCluster(String clusterName) {
        return clusterName != null && clients.containsKey(clusterName);
    }
}

