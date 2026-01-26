package com.ysmjjsy.goya.component.framework.servlet.converter;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumKit;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 字符串到枚举转换器工厂
 * <p>
 * 支持将 HTTP 请求参数中的字符串（枚举的 code）自动转换为枚举类型
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@RequestParam UserStatus status - 自动将 "1" 转换为 UserStatus.ACTIVE</li>
 *   <li>@PathVariable OrderStatus status - 自动将 "PENDING" 转换为 OrderStatus.PENDING</li>
 *   <li>@ModelAttribute 中的枚举字段</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam UserStatus status) {
 *     // status 参数会自动从字符串转换为枚举
 *     // 例如：?status=1 会转换为 UserStatus.ACTIVE（如果 code 是 1）
 * }
 * }</pre>
 *
 * @author goya
 * @see CodeEnum
 * @see EnumKit
 * @since 2025/12/20
 */
@Slf4j
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NullMarked
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter(targetType);
    }

    /**
     * 字符串到枚举转换器
     */
    private record StringToEnumConverter<T extends Enum<T> & CodeEnum<C>, C extends Serializable>(Class<T> enumType)
            implements Converter<String, T> {

        @Override
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            String trimmed = source.trim();

            // 1. 如果枚举实现了 IEnum 接口，使用 code 进行转换
            if (CodeEnum.class.isAssignableFrom(enumType)) {
                try {
                    // 尝试通过 code 解析
                    return EnumKit.findByCode(enumType, parseCode(trimmed)).orElse(null);
                } catch (IllegalArgumentException e) {
                    log.debug("[Goya] |- 通过 code 解析枚举失败: {} -> {}, 尝试通过 name 解析",
                            trimmed, enumType.getSimpleName(), e);
                }
            }

            // 2. 尝试通过枚举名称解析（兼容性）
            try {
                return Enum.valueOf(enumType, trimmed);
            } catch (IllegalArgumentException e) {
                // 3. 尝试忽略大小写匹配
                T[] constants = enumType.getEnumConstants();
                for (T constant : constants) {
                    if (constant.name().equalsIgnoreCase(trimmed)) {
                        return constant;
                    }
                }

                // 4. 如果都失败，抛出异常
                log.warn("[Goya] |- 枚举转换失败: {} -> {}, 支持的枚举值: {}",
                        trimmed, enumType.getSimpleName(),
                        Arrays.toString(enumType.getEnumConstants()), e);
                throw new IllegalArgumentException(
                        String.format("无法将 '%s' 转换为枚举 %s，支持的枚举值: %s",
                                trimmed, enumType.getSimpleName(),
                                Arrays.toString(enumType.getEnumConstants()))
                );
            }
        }

        /**
         * 解析 code（支持字符串和数字）
         */
        @SuppressWarnings("unchecked")
        private C parseCode(String source) {
            // 尝试解析为数字
            try {
                if (source.matches("^-?\\d+$")) {
                    // 整数
                    if (source.length() <= 9) {
                        return (C) Integer.valueOf(source);
                    } else {
                        return (C) Long.valueOf(source);
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略，继续作为字符串处理
            }
            // 作为字符串返回
            return (C) source;
        }
    }
}