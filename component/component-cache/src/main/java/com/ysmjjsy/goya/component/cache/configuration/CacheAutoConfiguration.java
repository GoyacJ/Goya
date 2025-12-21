package com.ysmjjsy.goya.component.cache.configuration;

import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * <p>cache configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache] CacheAutoConfiguration auto configure.");
    }


}
