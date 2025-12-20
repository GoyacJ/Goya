package com.ysmjjsy.goya.ai.mcp.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/16 11:11
 */
@Slf4j
@AutoConfiguration
public class AiMcpAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- ai [mcp] AiMcpAutoConfiguration auto configure.");
    }
}
