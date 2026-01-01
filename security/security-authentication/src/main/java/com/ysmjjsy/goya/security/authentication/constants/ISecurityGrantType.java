package com.ysmjjsy.goya.security.authentication.constants;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * <p>自定义 Grant Type 类型</p>
 *
 * @author goya
 * @since 2026/1/2 00:06
 */
public interface ISecurityGrantType {

    /**
     * 密码授权
     */
    AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");

    /**
     * 短信验证码授权
     */
    AuthorizationGrantType SMS = new AuthorizationGrantType("sms");

    /**
     * 社交授权
     */
    AuthorizationGrantType SOCIAL = new AuthorizationGrantType("social");
}
