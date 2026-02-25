package com.ysmjjsy.goya.component.security.authentication.constants;

/**
 * <p>认证缓存命名空间</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public interface SecurityAuthCacheNames {

    String LOGIN_ATTEMPT = "goya:security:auth:attempt";
    String MFA_CHALLENGE = "goya:security:auth:mfa";
    String MFA_CHALLENGE_CONSUMED = "goya:security:auth:mfa:consumed";
    String MFA_CHALLENGE_PROCESSING = "goya:security:auth:mfa:processing";
    String PRE_AUTH_CODE = "goya:security:auth:precode";
    String PRE_AUTH_CODE_CONSUMED = "goya:security:auth:precode:consumed";
    String PRE_AUTH_CODE_PROCESSING = "goya:security:auth:precode:processing";
}
