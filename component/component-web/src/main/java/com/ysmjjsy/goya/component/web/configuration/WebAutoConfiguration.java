package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.web.converter.*;
import com.ysmjjsy.goya.component.web.exception.GlobalExceptionHandler;
import com.ysmjjsy.goya.component.web.jackson.XssJacksonComponent;
import com.ysmjjsy.goya.component.web.template.ThymeleafTemplateHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

/**
 * <p>web configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@Import({GlobalExceptionHandler.class, XssJacksonComponent.class})
public class WebAutoConfiguration implements WebMvcConfigurer {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [web] WebAutoConfiguration auto configure.");
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        // ========== 日期时间格式化器 ==========
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern(IBaseConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS));
        registrar.setDateFormatter(DateTimeFormatter.ofPattern(IBaseConstants.DATE_FORMAT_YYYY_MM_DD));
        registrar.setTimeFormatter(DateTimeFormatter.ofPattern(IBaseConstants.DATE_FORMAT_HHMMSS));
        registrar.registerFormatters(registry);

        // ========== 日期时间转换器（补充）==========
        registry.addConverter(new StringToLocalDateTimeConverter());
        registry.addConverter(new StringToLocalDateConverter());
        registry.addConverter(new StringToLocalTimeConverter());

        // ========== 枚举转换器 ==========
        registry.addConverterFactory(new StringToEnumConverterFactory());

        // ========== 集合转换器 ==========
        registry.addConverterFactory(new StringToCollectionConverterFactory());

        // ========== 布尔值转换器 ==========
        registry.addConverter(new StringToBooleanConverter());

        // ========== 数字转换器 ==========
        registry.addConverter(new StringToNumberConverters.StringToIntegerConverter());
        registry.addConverter(new StringToNumberConverters.StringToLongConverter());
        registry.addConverter(new StringToNumberConverters.StringToDoubleConverter());
        registry.addConverter(new StringToNumberConverters.StringToBigDecimalConverter());

        // ========== UUID 转换器 ==========
        registry.addConverter(new StringToUuidConverter());

        // ========== 文件大小转换器 ==========
        registry.addConverter(new StringToFileSizeConverter());
    }

    @Bean
    @ConditionalOnMissingBean
    public ThymeleafTemplateHandler thymeleafTemplateHandler(SpringTemplateEngine springTemplateEngine) {
        ThymeleafTemplateHandler handler = new ThymeleafTemplateHandler(springTemplateEngine);
        log.trace("[GOYA] |- component [web] WebAutoConfiguration |- bean [thymeleafTemplateHandler] register.");
        return handler;
    }
}
