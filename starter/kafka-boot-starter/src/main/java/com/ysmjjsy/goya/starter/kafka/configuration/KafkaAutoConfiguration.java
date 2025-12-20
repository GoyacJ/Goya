package com.ysmjjsy.goya.starter.kafka.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>kafka configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class KafkaAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- starter [kafka] KafkaAutoConfiguration auto configure.");
    }


}
