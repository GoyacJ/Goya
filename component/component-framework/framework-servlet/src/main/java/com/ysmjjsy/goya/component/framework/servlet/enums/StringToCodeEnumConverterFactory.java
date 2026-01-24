package com.ysmjjsy.goya.component.framework.servlet.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumKit;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.io.Serializable;

/**
 * <p>Spring MVC 枚举绑定转换器工厂：将请求中的 String code 转为实现 {@link CodeEnum} 的枚举</p>
 *
 * <p>适用场景：</p>
 * <ul>
 *   <li>@RequestParam 枚举参数</li>
 *   <li>@PathVariable 枚举参数</li>
 * </ul>
 *
 * <p>注意：仅对实现 {@link CodeEnum} 的枚举生效，其他普通枚举不处理。</p>
 *
 * @author goya
 * @since 2026/1/24 15:53
 */
public class StringToCodeEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NullMarked
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> {
            if (source.isBlank()) {
                return null;
            }
            if (!CodeEnum.class.isAssignableFrom(targetType)) {
                // 非 CodeEnum 枚举不处理
                throw new IllegalArgumentException("目标类型不是 CodeEnum 枚举：" + targetType.getName());
            }

            // 推断 code 类型并解析（String/Integer/Long）
            Serializable parsed = parseCodeByEnumType((Class) targetType, source.trim());
            return (T) EnumKit.requireByCode((Class) targetType, parsed);
        };
    }

    /**
     * 根据枚举 code 类型推断并解析输入字符串。
     *
     * @param enumType 枚举类型
     * @param raw      输入字符串
     * @return 解析后的 code
     */
    @SuppressWarnings({"rawtypes"})
    private Serializable parseCodeByEnumType(Class enumType, String raw) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return raw;
        }
        CodeEnum first = (CodeEnum) constants[0];
        Object code = first.code();
        if (code == null) {
            return raw;
        }
        Class<?> codeType = code.getClass();
        if (String.class.equals(codeType)) {
            return raw;
        }
        if (Integer.class.equals(codeType)) {
            return Integer.valueOf(raw);
        }
        if (Long.class.equals(codeType)) {
            return Long.valueOf(raw);
        }
        return raw;
    }
}
