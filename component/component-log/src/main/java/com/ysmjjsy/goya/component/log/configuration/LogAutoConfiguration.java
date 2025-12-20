package com.ysmjjsy.goya.component.log.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 21:19
 */
@Slf4j
@AutoConfiguration
public class LogAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [log] LogAutoConfiguration auto configure.");
    }
}
