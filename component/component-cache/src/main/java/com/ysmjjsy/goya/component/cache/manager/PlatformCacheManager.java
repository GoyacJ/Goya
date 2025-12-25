package com.ysmjjsy.goya.component.cache.manager;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import com.ysmjjsy.goya.component.cache.model.CacheNullValue;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * <p>平台缓存管理器</p>
 * <p>实现 Spring Cache 的 CacheManager 接口，支持 Spring Cache 注解（@Cacheable、@CacheEvict 等）</p>
 * <p>特点：</p>
 * <ul>
 *     <li>支持每个缓存名称的独立配置</li>
 *     <li>自动创建 Cache 实例（按需创建）</li>
 *     <li>集成 ICacheService（HybridCacheService），支持多级缓存</li>
 *     <li>支持 allowNullValues、penetrationProtect 等配置项</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     @Cacheable(value = "userCache", key = "#id")
 *     public User getUser(Long id) {
 *         return userRepository.findById(id);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @see CacheManager
 * @see ICacheService
 * @since 2025/12/25 22:55
 */
@Slf4j
public class PlatformCacheManager implements CacheManager {

    private final ICacheService cacheService;
    private final CacheProperties cacheProperties;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    public PlatformCacheManager(ICacheService cacheService, CacheProperties cacheProperties) {
        this.cacheService = cacheService;
        this.cacheProperties = cacheProperties;
        log.debug("[Goya] |- Cache |- PlatformCacheManager initialized");
    }

    @Override
    public @Nullable Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            log.trace("[Goya] |- Cache |- Creating PlatformCache for cache name [{}]", cacheName);
            return new PlatformCache(cacheName, cacheService, cacheProperties);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    /**
     * <p>平台缓存实现</p>
     * <p>封装 ICacheService 调用，实现 Spring Cache 的 Cache 接口</p>
     */
    private static class PlatformCache implements Cache {
        private final String name;
        private final ICacheService cacheService;
        private final CacheProperties.CacheConfig config;

        public PlatformCache(String name, ICacheService cacheService, CacheProperties cacheProperties) {
            this.name = name;
            this.cacheService = cacheService;
            // 初始化时获取配置，避免每次操作都查询
            this.config = cacheProperties.getCacheConfigByDefault(name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return cacheService;
        }

        @Override
        public @Nullable ValueWrapper get(Object key) {
            // 1. 缓存穿透保护检查
            if (Boolean.TRUE.equals(config.penetrationProtect())) {
                try {
                    if (!cacheService.mightContain(name, key)) {
                        log.trace("[Goya] |- Cache |- Key [{}] not in bloom filter for cache [{}], skip query",
                                key, name);
                        return null; // 布隆过滤器判断一定不存在
                    }
                } catch (UnsupportedOperationException e) {
                    // 布隆过滤器未实现，继续查询
                    log.trace("[Goya] |- Cache |- Bloom filter not supported, skip check");
                }
            }

            // 2. 查询缓存
            Object value = cacheService.get(name, key);
            if (value == null) {
                return null;
            }

            // 3. 处理哨兵值
            if (CacheNullValue.isNullValue(value)) {
                return null; // 哨兵值转换为 null
            }

            return new SimpleValueWrapper(value);
        }

        @Override
        public <T> @Nullable T get(Object key, Class<T> type) {
            ValueWrapper wrapper = get(key);
            if (wrapper == null) {
                return null;
            }
            Object value = wrapper.get();
            if (type != null && !type.isInstance(value)) {
                throw new IllegalStateException(
                        String.format("Cached value is not of type %s: %s", type.getName(), value.getClass().getName())
                );
            }
            return type.cast(value);
        }

        @Override
        public <T> @Nullable T get(Object key, Callable<T> valueLoader) {
            // 先尝试从缓存获取
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return value;
            }

            // 缓存未命中，使用 loader 加载
            try {
                T value = valueLoader.call();
                put(key, value); // 写入缓存（会自动处理 allowNullValues）
                return value;
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            // 处理 allowNullValues
            if (value == null && !Boolean.TRUE.equals(config.allowNullValues())) {
                // 使用哨兵值，TTL 使用 penetrationProtectTimeout
                Duration ttl = config.penetrationProtectTimeout() != null
                        ? config.penetrationProtectTimeout()
                        : Duration.ofMinutes(1);
                cacheService.put(name, key, CacheNullValue.INSTANCE, ttl);
            } else {
                // 直接缓存（使用默认 TTL）
                cacheService.put(name, key, value);
            }
        }

        @Override
        public void evict(Object key) {
            cacheService.remove(name, key);
        }

        @Override
        public void clear() {
            // 清空指定缓存的所有数据
            // 注意：ICacheService 没有 clear 方法，这里使用批量删除的方式
            // 或者可以考虑添加 clear(String cacheName) 方法到 ICacheService
            log.warn("[Goya] |- Cache |- clear() is not fully supported, consider using evict() for specific keys");
        }
    }
}
