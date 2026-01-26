package com.ysmjjsy.goya.component.framework.common.error;

/**
 * <p>错误码协议（接口），用于统一描述“错误的稳定标识”与“可治理属性”。</p>
 * <p>错误码是长期兼容契约：一旦对外发布，应避免改变同一 code 的语义。</p>
 *
 * <h2>字段说明</h2>
 * <ul>
 *   <li>{@link #code()}：稳定错误码（建议形如：SYS-MOD-SCENE-SEQ）。</li>
 *   <li>{@link #defaultMessage()}：默认文案（兜底用），建议保持语义稳定。</li>
 *   <li>{@link #messageKey()}：国际化/文案平台 key，默认等同于 code。</li>
 *   <li>{@link #category()}：错误分类，用于策略路由。</li>
 *   <li>{@link #retryable()}：是否建议重试（注意：只是建议，具体由上层策略决定）。</li>
 *   <li>{@link #severity()}：严重级别，用于告警分级。</li>
 * </ul>
 *
 * <p><b>注意：</b>该接口不包含 HTTP 状态码等 Web 语义；映射在 {@code framework-core} 完成。</p>
 *
 * @author goya
 * @since 2026/1/24 13:22
 */
public interface ErrorCode {

    /**
     * 返回稳定错误码。
     *
     * @return 稳定错误码，不能为空
     */
    String code();

    /**
     * 返回默认文案（兜底用）。
     *
     * <p>该文案通常用于：
     * <ul>
     *   <li>未配置国际化文案时的兜底展示；</li>
     *   <li>日志中快速定位；</li>
     *   <li>调试环境的简要提示。</li>
     * </ul>
     * </p>
     *
     * @return 默认文案，不能为空
     */
    String defaultMessage();

    /**
     * 返回文案 key（用于 i18n 或文案平台）。
     *
     * <p>默认返回 {@link #code()}，以保证 key 稳定。</p>
     *
     * @return 文案 key，不能为空
     */
    default String messageKey() {
        return code();
    }

    /**
     * 返回错误分类。
     *
     * @return 错误分类，不能为空
     */
    ErrorCategory category();

    /**
     * 是否建议重试。
     *
     * <p>示例：远程依赖超时可能建议重试；业务规则失败不建议重试。</p>
     *
     * @return {@code true} 表示建议可重试
     */
    default boolean retryable() {
        return false;
    }

    /**
     * 返回严重级别。
     *
     * @return 严重级别，默认为 {@link Severity#ERROR}
     */
    default Severity severity() {
        return Severity.ERROR;
    }
}
