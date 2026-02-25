package com.ysmjjsy.goya.component.security.core.enums;

/**
 * <p>MFA 多因素认证类型</p>
 *
 * @author goya
 * @since 2026/2/4
 */
public enum MfaType {
    /**
     * TOTP（Time-based One-Time Password）
     * 基于时间的一次性密码，使用 Google Authenticator 等应用
     */
    TOTP,

    /**
     * SMS OTP
     * 短信验证码
     */
    SMS
}
