package com.ysmjjsy.goya.component.framework.servlet.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * <p>String 到 LocalTime 转换器</p>
 *
 * @author goya
 * @since 2025/12/20 23:42
 */
@Slf4j
public class StringToLocalTimeConverter implements Converter<String, LocalTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public LocalTime convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(source.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("[Goya] |- LocalTime 解析失败: {}, 格式应为: HH:mm:ss", source);
            throw new IllegalArgumentException("时间格式错误，应为: HH:mm:ss", e);
        }
    }
}
