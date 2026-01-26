package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumKit;
import org.mapstruct.Named;
import org.mapstruct.TargetType;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>MapStruct 通用枚举映射器，用于在 DTO code 与 CodeEnum 之间自动转换。</p>
 *
 * <h2>设计目标</h2>
 * <ul>
 *   <li>DTO 层：建议使用 String/Integer 等基础类型存储 code</li>
 *   <li>领域层：使用实现 {@link CodeEnum} 的枚举类型</li>
 *   <li>MapStruct 自动调用此类方法完成互转</li>
 * </ul>
 *
 * <h2>使用方式</h2>
 * <p>在你的 Mapper 上引入：</p>
 * <pre>{@code
 * @Mapper(config = GoyaMapStructConfig.class)
 * public interface UserMapper { ... }
 * }</pre>
 *
 * <p>若字段类型能推断，通常无需额外 @Mapping；必要时可用 qualifiedByName 指定。</p>
 * @author goya
 * @since 2026/1/24 15:52
 */
public class CodeEnumMapStructMapper {

    /**
     * 枚举 -> code（用于输出到 DTO）。
     *
     * @param e 枚举（可为空）
     * @return code（可为空）
     */
    @Named("enumToCode")
    public Serializable enumToCode(CodeEnum<? extends Serializable> e) {
        return e == null ? null : e.getCode();
    }

    /**
     * code -> 枚举（用于 DTO 转领域对象）。
     *
     * <p>MapStruct 会自动传入目标枚举类型 {@code enumType}。</p>
     *
     * @param code code（可为空）
     * @param enumType 目标枚举类型
     * @param <E> 枚举类型
     * @return 枚举常量（可为空）
     */
    @Named("codeToEnum")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends Enum<E> & CodeEnum<?>> E codeToEnum(Object code, @TargetType Class<E> enumType) {
        if (code == null) {
            return null;
        }
        Objects.requireNonNull(enumType, "enumType 不能为空");

        // 仅支持实现 CodeEnum 的枚举
        if (!enumType.isEnum() || !CodeEnum.class.isAssignableFrom(enumType)) {
            throw new IllegalArgumentException("目标类型不是 CodeEnum 枚举：" + enumType.getName());
        }

        // 推断 code 类型（取第一个常量的 code() 类型）
        Serializable parsed = parseCodeByEnumType(enumType, String.valueOf(code));
        return (E) EnumKit.requireByCode((Class) enumType, parsed);
    }

    /**
     * 根据枚举 code 类型推断并解析输入字符串。
     *
     * <p>优先建议 code 类型为 String；若为 Integer/Long 则做数字解析。</p>
     *
     * @param enumType 枚举类型
     * @param raw 输入值（字符串）
     * @return 解析后的 code
     */
    @SuppressWarnings("rawtypes")
    private Serializable parseCodeByEnumType(Class enumType, String raw) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            // 空枚举理论上不应存在
            return raw;
        }
        CodeEnum first = (CodeEnum) constants[0];
        Object code = first.getCode();
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
        // 其他类型：退化为字符串匹配（不保证一定能匹配）
        return raw;
    }
}
