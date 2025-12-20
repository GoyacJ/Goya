package com.ysmjjsy.goya.ai.rag.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/16 10:50
 */
@Slf4j
@AutoConfiguration
public class AiRagAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- ai [rag] AiRagAutoConfiguration auto configure.");
    }
}
