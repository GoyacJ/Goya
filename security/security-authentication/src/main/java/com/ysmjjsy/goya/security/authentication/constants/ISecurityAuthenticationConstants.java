package com.ysmjjsy.goya.security.authentication.constants;

import static com.ysmjjsy.goya.component.cache.constants.CacheConst.CACHE_PREFIX;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/17 22:59
 */
public interface ISecurityAuthenticationConstants {


    String OAUTH2_CALLBACK_PATH = "/oauth2/callback";

    String CACHE_SECURITY_PREFIX = CACHE_PREFIX + "security:";
    String CACHE_SECURITY_AUTHENTICATION_PREFIX = CACHE_SECURITY_PREFIX + "authentication:";

    /**
     * token
     */
    String CACHE_SECURITY_AUTHENTICATION_TOKEN_PREFIX = CACHE_SECURITY_AUTHENTICATION_PREFIX + "token:";

    /**
     * token黑名单
     */
    String CACHE_SECURITY_AUTHENTICATION_TOKEN_BLACK_LIST_PREFIX = CACHE_SECURITY_AUTHENTICATION_PREFIX + "blacklist:";
    String CACHE_SECURITY_AUTHENTICATION_LOGIN_PREFIX = CACHE_SECURITY_AUTHENTICATION_PREFIX + "login:";
    String CACHE_SECURITY_AUTHENTICATION_LOGIN_FAILURE_PREFIX = CACHE_SECURITY_AUTHENTICATION_LOGIN_PREFIX + "failure:";

    String PARAM_USERNAME = "username";
    String PARAM_PASSWORD = "password";
    String PARAM_PHONE_NUMBER = "phone_number";
    String PARAM_SMS_CODE = "sms_code";
}
