package com.ysmjjsy.goya.component.framework.masker.core;

/**
 * <p>脱敏模式</p>
 *
 * <p>不同模式下对“无法保持类型不变”的对象处理策略不同：</p>
 * <ul>
 *   <li>API：优先保持输出结构与类型不变，必要时跳过脱敏（不破坏接口契约）</li>
 *   <li>LOG：可退化为 Map/toString，优先保证不泄漏</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 23:15
 */
public enum MaskingMode {

    /**
     * API 响应模式。
     */
    API,

    /**
     * 日志模式。
     */
    LOG
}