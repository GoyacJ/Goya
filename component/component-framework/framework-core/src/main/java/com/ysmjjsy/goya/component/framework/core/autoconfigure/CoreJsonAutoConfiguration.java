package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.ysmjjsy.goya.component.framework.common.constants.DefaultConst.*;

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
        log.trace("[Goya] |- component [framework] CoreJsonAutoConfiguration |- bean [jacksonJsonMapper] register.");
        return builder.build();
    }

    @Bean
    public JacksonJsonHttpMessageConverter mappingJacksonHttpMessageConverter() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD)));
        simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT_HHMMSS)));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD)));
        simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        JsonMapper jsonMapper = JsonMapper.builder().addModule(simpleModule).build();
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        log.trace("[Goya] |- component [framework] CoreJsonAutoConfiguration |- bean [mappingJacksonHttpMessageConverter] register.");
        return converter;
    }

    @Bean
    @Lazy(false)
    public GoyaJson goyaJson() {
        GoyaJson goyaJson = new GoyaJson();
        log.trace("[Goya] |- component [framework] CoreJsonAutoConfiguration |- bean [goyaJson] register.");
        return goyaJson;
    }
}
