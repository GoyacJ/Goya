package com.ysmjjsy.goya.component.security.core.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>auth configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class AuthAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [auth] AuthAutoConfiguration auto configure.");
    }


}
