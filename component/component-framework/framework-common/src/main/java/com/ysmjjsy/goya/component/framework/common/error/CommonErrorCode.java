package com.ysmjjsy.goya.component.framework.common.error;

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
public enum CommonErrorCode implements ErrorCode {

    /**
     * 成功（通常用于统一响应体场景；异常体系一般不会用到）。
     */
    OK("GOYA-FRAME-COMMON-0000", "成功", ErrorCategory.SYSTEM, Severity.INFO, false),

    /**
     * 参数不合法。
     */
    INVALID_PARAM("GOYA-FRAME-VALIDATION-0001", "参数不合法", ErrorCategory.VALIDATION, Severity.WARN, false),

    /**
     * 未认证（未登录或凭证无效）。
     */
    UNAUTHORIZED("GOYA-FRAME-AUTH-0001", "未认证", ErrorCategory.AUTH, Severity.WARN, false),

    /**
     * 无权限（已认证但权限不足）。
     */
    FORBIDDEN("GOYA-FRAME-AUTH-0002", "无权限", ErrorCategory.AUTH, Severity.WARN, false),

    /**
     * 资源不存在。
     */
    NOT_FOUND("GOYA-FRAME-NOTFOUND-0001", "资源不存在", ErrorCategory.NOT_FOUND, Severity.WARN, false),

    /**
     * 冲突（例如幂等冲突/版本冲突）。
     */
    CONFLICT("GOYA-FRAME-CONFLICT-0001", "请求冲突", ErrorCategory.CONFLICT, Severity.WARN, false),

    /**
     * 远程调用失败。
     */
    REMOTE_CALL_FAILED("GOYA-FRAME-REMOTE-0001", "远程调用失败", ErrorCategory.REMOTE, Severity.ERROR, true),

    /**
     * 基础设施错误（DB/IO/缓存等）。
     */
    INFRA_ERROR("GOYA-FRAME-INFRA-0001", "基础设施错误", ErrorCategory.INFRA, Severity.ERROR, true),

    /**
     * 系统错误（兜底）。
     */
    SYSTEM_ERROR("GOYA-FRAME-SYSTEM-0001", "系统错误", ErrorCategory.SYSTEM, Severity.ERROR, false),

    /**
     * Json错误
     */
    SYSTEM_JSON_ERROR("GOYA-FRAME-SYSTEM-0002", "Json错误", ErrorCategory.SYSTEM, Severity.ERROR, false)

    ;

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
    private final Severity severity;
    private final boolean retryable;

    CommonErrorCode(String code,
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
