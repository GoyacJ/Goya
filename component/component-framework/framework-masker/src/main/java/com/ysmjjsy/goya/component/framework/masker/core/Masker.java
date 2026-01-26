package com.ysmjjsy.goya.component.framework.masker.core;

/**
 * <p>脱敏器：将输入对象转换为可安全记录日志的结构或字符串</p>
 * <p>建议：所有入参/返回值/异常上下文输出前先调用该接口。</p>
 * @author goya
 * @since 2026/1/24 22:01
 */
public interface Masker {

    /**
     * 脱敏处理。
     *
     * @param input 输入对象（可为空）
     * @return 脱敏后的对象（可为空）
     */
    Object mask(Object input);

    /**
     * 按模式脱敏。
     *
     * <p>默认实现：直接调用 {@link #mask(Object)}，以保持兼容。</p>
     *
     * @param input 输入对象（可为空）
     * @param mode 模式（可为空，视为 LOG）
     * @return 脱敏后的对象（可为空）
     */
    default Object mask(Object input, MaskingMode mode) {
        return mask(input);
    }
}