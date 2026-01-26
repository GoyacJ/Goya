package com.ysmjjsy.goya.component.framework.common.enums;

import lombok.experimental.UtilityClass;

import java.io.Serializable;
import java.util.*;

/**
 * <p>枚举工具类，提供按 code 查询、导出选项列表等能力</p>
 *
 * @author goya
 * @since 2026/1/24 15:43
 */
@UtilityClass
public class EnumKit {

    /**
     * 按 code 查找枚举常量。
     *
     * @param enumClass 枚举类型
     * @param code code
     * @param <E> 枚举类型
     * @param <C> code 类型
     * @return Optional
     */
    public static <E extends Enum<E> & CodeEnum<C>, C extends Serializable>
    Optional<E> findByCode(Class<E> enumClass, C code) {
        if (enumClass == null || code == null) {
            return Optional.empty();
        }
        for (E e : enumClass.getEnumConstants()) {
            if (Objects.equals(e.code(), code)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    /**
     * 按 code 获取枚举常量，不存在则抛异常。
     *
     * @param enumClass 枚举类型
     * @param code code
     * @param <E> 枚举类型
     * @param <C> code 类型
     * @return 枚举常量
     */
    public static <E extends Enum<E> & CodeEnum<C>, C extends Serializable>
    E requireByCode(Class<E> enumClass, C code) {
        return findByCode(enumClass, code)
                .orElseThrow(() -> new IllegalArgumentException("未找到枚举：" + enumClass.getName() + ", code=" + code));
    }

    /**
     * 导出枚举选项列表（使用 label）。
     *
     * @param enumClass 枚举类型
     * @param <E> 枚举类型
     * @return 选项列表
     */
    public static <E extends Enum<E> & CodeEnum<? extends Serializable>>
    List<EnumOption> options(Class<E> enumClass) {
        if (enumClass == null) {
            return List.of();
        }
        List<EnumOption> list = new ArrayList<>();
        for (E e : enumClass.getEnumConstants()) {
            list.add(EnumOption.of(e.code(), e.label()));
        }
        return Collections.unmodifiableList(list);
    }
}
