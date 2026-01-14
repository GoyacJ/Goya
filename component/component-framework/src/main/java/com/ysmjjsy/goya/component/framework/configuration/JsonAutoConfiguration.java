package com.ysmjjsy.goya.component.framework.configuration;

import com.ysmjjsy.goya.component.framework.json.GoyaJson;
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
public class JsonAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- framework [framework] JsonAutoConfiguration auto configure.");
    }

    @Bean
    public JsonMapper jacksonJsonMapper(JsonMapper.Builder builder) {
        return builder.build();
    }

    @Bean
    @Lazy(false)
    public GoyaJson goyaJson(){
        GoyaJson goyaJson = new GoyaJson();
        log.trace("[Goya] |- component [common] CommonAutoConfiguration |- bean [goyaJson] register.");
        return goyaJson;
    }
}
