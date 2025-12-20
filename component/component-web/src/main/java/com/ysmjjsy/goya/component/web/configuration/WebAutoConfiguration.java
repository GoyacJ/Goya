package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.web.exception.GlobalExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * <p>web configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class WebAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [web] WebAutoConfiguration auto configure.");
    }


}
