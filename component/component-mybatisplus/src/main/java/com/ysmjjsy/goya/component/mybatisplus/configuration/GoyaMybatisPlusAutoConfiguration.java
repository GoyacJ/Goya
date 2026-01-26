package com.ysmjjsy.goya.component.mybatisplus.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 01:29
 */
@Slf4j
@AutoConfiguration
public class GoyaMybatisPlusAutoConfiguration {
    
    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [mybatis-plus] GoyaMybatisPlusAutoConfiguration auto configure.");
    }
}
