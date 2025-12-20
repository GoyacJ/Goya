package com.ysmjjsy.goya.component.web.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 字符串到数字转换器（支持空字符串和 null）
 * <p>
 * 自动处理空字符串、null 值，转换为 null 或默认值
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@RequestParam(required = false) Integer pageSize - 空字符串自动转换为 null</li>
 *   <li>@RequestParam Long id - 自动处理空字符串</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/20
 */
@Slf4j
public class StringToNumberConverters {

    /**
     * String -> Integer（支持空字符串）
     */
    public static class StringToIntegerConverter implements Converter<String, Integer> {
        @Override
        public Integer convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            try {
                return Integer.valueOf(source.trim());
            } catch (NumberFormatException e) {
                log.warn("[Goya] |- Integer 转换失败: {}", source);
                throw new IllegalArgumentException("无法将 '" + source + "' 转换为 Integer", e);
            }
        }
    }

    /**
     * String -> Long（支持空字符串）
     */
    public static class StringToLongConverter implements Converter<String, Long> {
        @Override
        public Long convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            try {
                return Long.valueOf(source.trim());
            } catch (NumberFormatException e) {
                log.warn("[Goya] |- Long 转换失败: {}", source);
                throw new IllegalArgumentException("无法将 '" + source + "' 转换为 Long", e);
            }
        }
    }

    /**
     * String -> Double（支持空字符串）
     */
    public static class StringToDoubleConverter implements Converter<String, Double> {
        @Override
        public Double convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            try {
                return Double.valueOf(source.trim());
            } catch (NumberFormatException e) {
                log.warn("[Goya] |- Double 转换失败: {}", source);
                throw new IllegalArgumentException("无法将 '" + source + "' 转换为 Double", e);
            }
        }
    }

    /**
     * String -> BigDecimal（支持空字符串）
     */
    public static class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
        @Override
        public BigDecimal convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            try {
                return new BigDecimal(source.trim());
            } catch (NumberFormatException e) {
                log.warn("[Goya] |- BigDecimal 转换失败: {}", source);
                throw new IllegalArgumentException("无法将 '" + source + "' 转换为 BigDecimal", e);
            }
        }
    }
}