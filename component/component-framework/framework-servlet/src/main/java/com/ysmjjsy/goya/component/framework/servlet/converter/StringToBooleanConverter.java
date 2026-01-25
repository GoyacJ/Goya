package com.ysmjjsy.goya.component.framework.servlet.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 字符串到布尔值转换器
 * <p>
 * 支持多种布尔值表示方式：
 * <ul>
 *   <li>true/false（不区分大小写）</li>
 *   <li>1/0</li>
 *   <li>yes/no（不区分大小写）</li>
 *   <li>y/n（不区分大小写）</li>
 *   <li>on/off（不区分大小写）</li>
 * </ul>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@RequestParam Boolean enabled - 支持 "true", "1", "yes", "y", "on"</li>
 *   <li>@RequestParam boolean active - 支持多种格式</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam Boolean enabled) {
 *     // enabled 参数支持多种格式：?enabled=true, ?enabled=1, ?enabled=yes
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20
 */
@Slf4j
public class StringToBooleanConverter implements Converter<String, Boolean> {

    private static final Set<String> TRUE_VALUES = Set.of(
            "true", "1", "yes", "y", "on", "enabled", "active"
    );

    private static final Set<String> FALSE_VALUES = Set.of(
            "false", "0", "no", "n", "off", "disabled", "inactive"
    );

    @Override
    public Boolean convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        String trimmed = source.trim().toLowerCase();

        if (TRUE_VALUES.contains(trimmed)) {
            return Boolean.TRUE;
        } else if (FALSE_VALUES.contains(trimmed)) {
            return Boolean.FALSE;
        } else {
            log.warn("[Goya] |- 无法识别的布尔值: {}, 支持的格式: true/false, 1/0, yes/no, y/n, on/off", source);
            throw new IllegalArgumentException(
                    String.format("无法将 '%s' 转换为布尔值，支持的格式: true/false, 1/0, yes/no, y/n, on/off", source)
            );
        }
    }
}