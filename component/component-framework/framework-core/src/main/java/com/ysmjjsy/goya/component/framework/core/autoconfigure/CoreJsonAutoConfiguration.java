package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import tools.jackson.databind.json.JsonMapper;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:41
 */
@Slf4j
@AutoConfiguration
public class CoreJsonAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] CoreJsonAutoConfiguration auto configure.");
    }

    @Bean
    public JsonMapper jacksonJsonMapper(JsonMapper.Builder builder) {
        return builder.build();
    }

    @Bean
    @Lazy(false)
    public GoyaJson goyaJson(){
        GoyaJson goyaJson = new GoyaJson();
        log.trace("[Goya] |- component [framework] CoreJsonAutoConfiguration |- bean [goyaJson] register.");
        return goyaJson;
    }
}
