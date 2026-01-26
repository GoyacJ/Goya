package com.ysmjjsy.goya.component.framework.servlet.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串到文件大小（字节数）转换器
 * <p>
 * 支持多种单位：
 * <ul>
 *   <li>B / bytes - 字节</li>
 *   <li>KB / kb - 千字节（1024 字节）</li>
 *   <li>MB / mb - 兆字节（1024 * 1024 字节）</li>
 *   <li>GB / gb - 千兆字节（1024 * 1024 * 1024 字节）</li>
 * </ul>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@RequestParam Long maxSize - 支持 "10MB", "1GB" 等格式</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/files")
 * public List<File> getFiles(@RequestParam Long maxSize) {
 *     // maxSize 参数支持: ?maxSize=10MB, ?maxSize=1GB
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20
 */
@Slf4j
public class StringToFileSizeConverter implements Converter<String, Long> {

    private static final Pattern SIZE_PATTERN = Pattern.compile(
            "^(\\d+(?:\\.\\d+)?)\\s*([KMGT]?B|bytes?)$", Pattern.CASE_INSENSITIVE
    );

    @Override
    public Long convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        String trimmed = source.trim().toUpperCase();

        // 尝试解析为纯数字（字节）
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            // 继续解析带单位的格式
        }

        // 解析带单位的格式
        Matcher matcher = SIZE_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            log.warn("[Goya] |- 文件大小格式错误: {}, 支持的格式: 数字+单位(B/KB/MB/GB)", source);
            throw new IllegalArgumentException(
                    "无法将 '" + source + "' 转换为文件大小，支持的格式: 数字+单位(B/KB/MB/GB)，例如: 10MB, 1GB"
            );
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        return switch (unit) {
            case "B", "BYTES", "BYTE" -> (long) value;
            case "KB" -> (long) (value * 1024);
            case "MB" -> (long) (value * 1024 * 1024);
            case "GB" -> (long) (value * 1024 * 1024 * 1024);
            case "TB" -> (long) (value * 1024L * 1024 * 1024 * 1024);
            default -> throw new IllegalArgumentException("不支持的单位: " + unit);
        };
    }
}