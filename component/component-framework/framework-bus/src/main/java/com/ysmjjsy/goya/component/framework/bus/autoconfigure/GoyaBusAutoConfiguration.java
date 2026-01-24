package com.ysmjjsy.goya.component.framework.bus.autoconfigure;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 00:30
 */
@Slf4j
@AutoConfiguration
public class GoyaBusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] GoyaBusAutoConfiguration auto configure.");
    }


}
