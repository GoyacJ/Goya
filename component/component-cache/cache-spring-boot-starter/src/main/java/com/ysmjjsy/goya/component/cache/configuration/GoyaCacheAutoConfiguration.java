package com.ysmjjsy.goya.component.cache.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>缓存配置</p>
 *
 * @author goya
 * @since 2026/1/15 11:50
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class GoyaCacheAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [cache] GoyaCacheAutoConfiguration auto configure.");
    }
}
