package com.ysmjjsy.goya.component.framework.common.enums;

import java.io.Serializable;

/**
 * <p>编码型枚举统一接口</p>
 *
 * @author goya
 * @since 2025/12/19 22:27
 */
public interface CodeEnum<C extends Serializable> {

    /**
     * 获取枚举稳定编码。
     *
     * <p>code 应当稳定，不建议随意变更；通常用于数据库存储、协议传输。</p>
     *
     * @return code（非空）
     */
    C code();

    /**
     * 获取枚举展示文案（默认语言）。
     *
     * <p>label 用于界面展示，建议为对外安全文案。</p>
     *
     * @return label（可为空；如启用 i18n 可通过 key 解析）
     */
    String label();

    /**
     * 获取国际化 key（可选）。
     *
     * <p>若返回非空，则可通过 MessageSource + Locale 解析多语言文案。</p>
     *
     * @return i18n key（可为空）
     */
    default String i18nKey() {
        return null;
    }
}
