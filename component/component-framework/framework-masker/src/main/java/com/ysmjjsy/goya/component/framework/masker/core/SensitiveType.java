package com.ysmjjsy.goya.component.framework.masker.core;

/**
 * <p>常见敏感信息类型</p>
 *
 * @author goya
 * @since 2026/1/24 22:00
 */
public enum SensitiveType {

    /** 密码类。 */
    PASSWORD,

    /** Token/密钥类。 */
    TOKEN,

    /** 邮箱。 */
    EMAIL,

    /** 手机号。 */
    PHONE,

    /** 身份证/证件号。 */
    ID_CARD,

    /** 银行卡号。 */
    BANK_CARD,

    /** 认证头（Authorization/Cookie 等）。 */
    HEADER_CREDENTIAL,

    /** 通用脱敏。 */
    GENERIC
}