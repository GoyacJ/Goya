package com.ysmjjsy.goya.component.ddd.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>ddd configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class DomainAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [ddd] DomainAutoConfiguration auto configure.");
    }


}
