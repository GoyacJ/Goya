package com.ysmjjsy.goya.starter.mybatisplus.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>mybatisplus configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class MybatisPlusAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- starter [mybatisplus] MybatisPlusAutoConfiguration auto configure.");
    }


}
