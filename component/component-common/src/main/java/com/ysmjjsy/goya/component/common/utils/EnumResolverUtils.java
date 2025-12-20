package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
import lombok.experimental.UtilityClass;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:12
 */
@UtilityClass
public class EnumResolverUtils {

    /**
     * 根据业务编码反向解析枚举实例。
     *
     * <p>
     * 该方法是枚举体系的核心基础设施，用于将
     * <b>外部世界的值</b>（如：数据库字段、JSON 请求参数、URL 参数）
     * 转换为 <b>类型安全的枚举对象</b>。
     * </p>
     *
     * <p>
     * 设计约束：
     * <ul>
     *   <li>枚举必须实现 {@link IEnum} 接口，并提供稳定的 {@code code}</li>
     *   <li>禁止使用 {@code ordinal()} 参与任何业务或持久化逻辑</li>
     *   <li>{@code code} 必须具备唯一性与稳定性</li>
     * </ul>
     * </p>
     *
     * <p>
     * 使用场景包括但不限于：
     * <ul>
     *   <li>数据库字段反序列化为枚举（MyBatis / JPA）</li>
     *   <li>JSON 请求体中的枚举 code 转换</li>
     *   <li>URL / 表单参数到枚举的绑定</li>
     * </ul>
     * </p>
     *
     * <p>
     * 当 {@code code} 为 {@code null} 时，方法直接返回 {@code null}，
     * 以便与数据库可空字段、可选参数语义保持一致。
     * </p>
     *
     * <p>
     * 当 {@code code} 无法匹配任何枚举值时，将抛出 {@link IllegalArgumentException}，
     * 该异常通常应在全局异常处理器中统一转换为参数校验错误。
     * </p>
     *
     * @param enumClass 枚举类型的 {@link Class} 对象，不能为空
     * @param code      枚举的业务编码值，可能为 {@code null}
     * @param <E>       枚举类型，必须同时是 {@link Enum} 且实现 {@link IEnum}
     * @param <C>       枚举编码类型，必须可序列化
     * @return          与 {@code code} 对应的枚举实例；若 {@code code} 为 {@code null}，返回 {@code null}
     * @throws IllegalArgumentException 当 {@code code} 不存在于指定枚举类型中时抛出
     */
    public static <E extends Enum<E> & IEnum<C>, C extends Serializable>
    E fromCode(Class<E> enumClass, C code) {
        if (code == null) {
            return null;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown enum code: " + code + " for " + enumClass.getSimpleName()
                        ));
    }
}
