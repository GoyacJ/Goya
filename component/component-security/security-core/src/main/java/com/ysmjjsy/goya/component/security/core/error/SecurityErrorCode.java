package com.ysmjjsy.goya.component.security.core.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCategory;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.Severity;

/**
 * <p>Security 模块错误码</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public enum SecurityErrorCode implements ErrorCode {

    AUTHENTICATION_FAILED("GOYA-SECURITY-AUTH-0001", "认证失败", ErrorCategory.AUTH, Severity.WARN, false),

    CAPTCHA_INVALID("GOYA-SECURITY-AUTH-0002", "验证码错误", ErrorCategory.AUTH, Severity.WARN, false),

    OTP_INVALID("GOYA-SECURITY-AUTH-0003", "动态验证码错误", ErrorCategory.AUTH, Severity.WARN, false),

    MFA_REQUIRED("GOYA-SECURITY-AUTH-0004", "需要多因素认证", ErrorCategory.AUTH, Severity.INFO, false),

    MFA_CHALLENGE_NOT_FOUND("GOYA-SECURITY-AUTH-0005", "MFA挑战不存在或已过期", ErrorCategory.AUTH, Severity.WARN, false),

    MFA_VERIFICATION_FAILED("GOYA-SECURITY-AUTH-0006", "MFA校验失败", ErrorCategory.AUTH, Severity.WARN, false),

    PRE_AUTH_CODE_INVALID("GOYA-SECURITY-AUTH-0007", "预认证码无效或已过期", ErrorCategory.AUTH, Severity.WARN, false),

    ACCOUNT_TEMP_LOCKED("GOYA-SECURITY-AUTH-0008", "账户临时锁定", ErrorCategory.AUTH, Severity.WARN, false),

    TENANT_MISMATCH("GOYA-SECURITY-AUTH-0009", "租户不匹配", ErrorCategory.AUTH, Severity.WARN, false),

    SUBJECT_MISMATCH("GOYA-SECURITY-AUTH-0014", "请求头主体与令牌主体不一致", ErrorCategory.AUTH, Severity.WARN, false),

    SOCIAL_LOGIN_FAILED("GOYA-SECURITY-AUTH-0010", "社交登录失败", ErrorCategory.AUTH, Severity.WARN, false),

    WX_MINI_LOGIN_FAILED("GOYA-SECURITY-AUTH-0011", "小程序登录失败", ErrorCategory.AUTH, Severity.WARN, false),

    RESOURCE_TOKEN_REVOKED("GOYA-SECURITY-AUTH-0012", "令牌已被撤销", ErrorCategory.AUTH, Severity.WARN, false),

    UNSUPPORTED_CLIENT_TYPE("GOYA-SECURITY-AUTH-0013", "不支持的客户端类型", ErrorCategory.VALIDATION, Severity.WARN, false),

    SECURITY_CONFIGURATION_ERROR("GOYA-SECURITY-INFRA-0001", "安全配置错误", ErrorCategory.INFRA, Severity.ERROR, false),

    SECURITY_SERVICE_UNAVAILABLE("GOYA-SECURITY-INFRA-0002", "安全服务不可用", ErrorCategory.INFRA, Severity.ERROR, true);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
    private final Severity severity;
    private final boolean retryable;

    SecurityErrorCode(String code,
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

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public boolean retryable() {
        return retryable;
    }

    @Override
    public Severity severity() {
        return severity;
    }
}
