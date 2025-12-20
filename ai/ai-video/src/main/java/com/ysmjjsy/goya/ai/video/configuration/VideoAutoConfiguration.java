package com.ysmjjsy.goya.ai.video.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 21:55
 */
@Slf4j
@AutoConfiguration
public class VideoAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- ai [video] VideoAutoConfiguration auto configure.");
    }

}
