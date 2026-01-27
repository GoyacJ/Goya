package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCategory;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.Severity;

/**
 * <p>框架通用错误码集合</p>
 * <p>该枚举仅包含框架层“跨业务通用”的错误码，例如参数错误、权限错误、系统错误等。
 * 各业务模块应自行维护自己的错误码枚举（例如 {@code UserErrorCode}、{@code OrderErrorCode}）。</p>
 *
 * <h2>错误码治理建议</h2>
 * <ul>
 *   <li>错误码一旦对外发布，不应复用或改变语义。</li>
 *   <li>默认文案可以润色，但不得改变语义。</li>
 *   <li>扩展错误码时建议按模块维度新增枚举，避免“大一统枚举”无限膨胀。</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 13:22
 */
public enum CaptchaErrorCode implements ErrorCode {

    /**
     * 验证码分类错误
     */
    CATEGORY_IS_INCORRECT("GOYA-COMPONENT-CAPTCHA-0001", "验证码分类错误", ErrorCategory.SYSTEM, Severity.ERROR, false),

    /**
     * 验证码处理器不存在
     */
    HANDLER_NOT_EXIST("GOYA-COMPONENT-CAPTCHA-0002", "验证码处理器不存在", ErrorCategory.SYSTEM, Severity.ERROR, false),

    /**
     * 验证码已过期
     */
    HAS_EXPIRED("GOYA-COMPONENT-CAPTCHA-0003", "验证码已过期", ErrorCategory.VALIDATION, Severity.WARN, false),

    /**
     * 验证码为空
     */
    IS_EMPTY("GOYA-COMPONENT-CAPTCHA-0004", "验证码为空", ErrorCategory.VALIDATION, Severity.WARN, false),

    /**
     * 验证码不匹配错误
     */
    MISMATCH("GOYA-COMPONENT-CAPTCHA-0005", "验证码不匹配错误", ErrorCategory.VALIDATION, Severity.WARN, false),

    /**
     * 验证码校验参数错误
     */
    PARAMETER_ILLEGAL("GOYA-COMPONENT-CAPTCHA-0006", "验证码校验参数错误", ErrorCategory.VALIDATION, Severity.WARN, false),

    ;

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
    private final Severity severity;
    private final boolean retryable;

    CaptchaErrorCode(String code,
                     String defaultMessage,
                     ErrorCategory category,
                     Severity severity,
                     boolean retryable) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.category = category;
        this.severity = severity;
        this.retryable = retryable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorCategory category() {
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retryable() {
        return retryable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Severity severity() {
        return severity;
    }
}
