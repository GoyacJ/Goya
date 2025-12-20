package com.ysmjjsy.goya.ai.spring.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/15 22:39
 */
@Slf4j
@AutoConfiguration
public class AiSpringAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- ai [spring] AiSpringAutoConfiguration auto configure.");
    }
}
