package com.ysmjjsy.goya.component.bus.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>bus configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class BusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [bus] BusAutoConfiguration auto configure.");
    }


}
