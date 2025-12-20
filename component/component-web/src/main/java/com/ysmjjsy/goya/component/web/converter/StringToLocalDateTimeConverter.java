package com.ysmjjsy.goya.component.web.converter;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * <p>String 到 LocalDateTime 转换器</p>
 *
 * @author goya
 * @since 2025/12/20 23:41
 */
@Slf4j
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(IBaseConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);

    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(source.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("[Goya] |- LocalDateTime 解析失败: {}, 格式应为: {}", source, IBaseConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
            throw new IllegalArgumentException("日期时间格式错误，应为: " + IBaseConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS, e);
        }
    }
}
