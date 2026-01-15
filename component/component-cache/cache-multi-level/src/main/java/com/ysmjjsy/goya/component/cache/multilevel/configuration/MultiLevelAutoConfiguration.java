package com.ysmjjsy.goya.component.cache.multilevel.configuration;

import com.ysmjjsy.goya.component.cache.multilevel.configuration.properties.MultiLevelProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * <p>多级缓存配置类</p>
 *
 * @author goya
 * @since 2026/1/15 11:38
 */
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(MultiLevelProperties.class)
public class MultiLevelAutoConfiguration {
    
    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [multi-level] MultiLevelAutoConfiguration auto configure.");
    }
}
