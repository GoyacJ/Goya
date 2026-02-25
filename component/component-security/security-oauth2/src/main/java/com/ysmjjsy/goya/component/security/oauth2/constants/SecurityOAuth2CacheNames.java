package com.ysmjjsy.goya.component.security.oauth2.constants;

/**
 * <p>OAuth2 会话索引缓存命名空间</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public interface SecurityOAuth2CacheNames {

    String SSO_SID_TOKEN_INDEX = "goya:security:sso:sid";
    String SSO_SID_AUTHORIZATION_INDEX = "goya:security:sso:sid:auth";
    String SSO_USER_AUTHORIZATION_INDEX = "goya:security:sso:user:auth";
    String SSO_USER_CLIENT_AUTHORIZATION_INDEX = "goya:security:sso:user-client:auth";
}
