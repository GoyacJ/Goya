package com.ysmjjsy.goya.component.framework.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 11:53
 */
@Slf4j
@AutoConfiguration
public class GoyaFrameworkAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaFrameworkAutoConfiguration auto configure.");
    }
}
