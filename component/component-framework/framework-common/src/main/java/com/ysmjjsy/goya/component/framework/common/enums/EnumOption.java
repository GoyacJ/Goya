package com.ysmjjsy.goya.component.framework.common.enums;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>枚举选项输出结构（用于前端下拉框/字典接口）</p>
 *
 * @param code code
 * @param label 展示文案（可为 i18n 后的结果）
 *
 * @author goya
 * @since 2026/1/24 15:42
 */
public record EnumOption(
        Serializable code,
        String label
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建选项。
     *
     * @param code code
     * @param label label
     * @return EnumOption
     */
    public static EnumOption of(Serializable code, String label) {
        return new EnumOption(code, label);
    }
}