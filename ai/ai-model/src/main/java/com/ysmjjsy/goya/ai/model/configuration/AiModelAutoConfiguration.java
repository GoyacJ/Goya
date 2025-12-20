package com.ysmjjsy.goya.ai.model.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 21:59
 */
@Slf4j
@AutoConfiguration
public class AiModelAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- ai [model] AiModelAutoConfiguration auto configure.");
    }
}
