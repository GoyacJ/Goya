package com.ysmjjsy.goya.component.cache.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>Goya 缓存自动配置类</p>
 * <p>整合所有缓存模块，提供统一的多级缓存能力</p>
 * <p>装配逻辑：</p>
 * <ul>
 *     <li>根据配置和可用性自动判断缓存模式（单级/多级）</li>
 *     <li>注册工厂实现（CaffeineLocalCacheFactory、RedisRemoteCacheFactory）</li>
 *     <li>注册多级缓存相关 Bean（如果启用多级缓存）</li>
 *     <li>根据配置决定默认 CacheManager</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/15 11:50
 */
@Slf4j
@AutoConfiguration
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache] GoyaCacheAutoConfiguration auto configure.");
    }
}
