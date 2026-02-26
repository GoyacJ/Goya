package com.ysmjjsy.goya.component.admin.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCategory;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.Severity;

/**
 * <p>admin 错误码</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public enum AdminErrorCode implements ErrorCode {

    ADMIN_ENTITY_NOT_FOUND("GOYA-ADMIN-BIZ-0001", "资源不存在", ErrorCategory.NOT_FOUND, Severity.WARN, false),

    ADMIN_PARAM_INVALID("GOYA-ADMIN-VALIDATION-0001", "参数不合法", ErrorCategory.VALIDATION, Severity.WARN, false),

    ADMIN_UNIQUENESS_CONFLICT("GOYA-ADMIN-CONFLICT-0001", "唯一键冲突", ErrorCategory.CONFLICT, Severity.WARN, false),

    ADMIN_TREE_CYCLE("GOYA-ADMIN-BIZ-0002", "树结构存在循环引用", ErrorCategory.BIZ, Severity.WARN, false),

    ADMIN_BOOTSTRAP_PASSWORD_MISSING("GOYA-ADMIN-INFRA-0001", "管理员引导密码未配置", ErrorCategory.INFRA, Severity.WARN, false);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
    private final Severity severity;
    private final boolean retryable;

    AdminErrorCode(String code, String defaultMessage, ErrorCategory category, Severity severity, boolean retryable) {
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
