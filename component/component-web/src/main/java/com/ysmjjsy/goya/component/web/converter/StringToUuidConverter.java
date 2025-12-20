package com.ysmjjsy.goya.component.web.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 字符串到 UUID 转换器
 * <p>
 * 支持标准 UUID 格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@PathVariable UUID id - 自动将路径参数转换为 UUID</li>
 *   <li>@RequestParam UUID userId - 自动转换查询参数</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * public User getUser(@PathVariable UUID id) {
 *     // id 参数会自动从字符串转换为 UUID
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20
 */
@Slf4j
public class StringToUuidConverter implements Converter<String, UUID> {

    @Override
    public UUID convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        try {
            return UUID.fromString(source.trim());
        } catch (IllegalArgumentException e) {
            log.warn("[Goya] |- UUID 转换失败: {}, 格式应为: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", source);
            throw new IllegalArgumentException(
                    "无法将 '" + source + "' 转换为 UUID，格式应为: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", e
            );
        }
    }
}