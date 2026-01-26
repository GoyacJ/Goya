package com.ysmjjsy.goya.component.security.core.constants;

import com.ysmjjsy.goya.component.framework.constants.PropertyConst;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/11 17:44
 */
public interface SecurityConst {

    /**
     * PROPERTY_PREFIX_PLATFORM
     */
    String PROPERTY_PLATFORM_SECURITY = PropertyConst.PROPERTY_GOYA + ".security";
    String PROPERTY_PLATFORM_SECURITY_AUTHENTICATION = PROPERTY_PLATFORM_SECURITY + ".authentication";
    String PROPERTY_PLATFORM_SECURITY_RESOURCE = PROPERTY_PLATFORM_SECURITY + ".resource";

    String ACCOUNT_EXPIRED = "AccountExpiredException";
    String ACCOUNT_DISABLED = "DisabledException";
    String ACCOUNT_LOCKED = "LockedException";
    String ACCOUNT_ENDPOINT_LIMITED = "AccountEndpointLimitedException";
    String BAD_CREDENTIALS = "BadCredentialsException";
    String CREDENTIALS_EXPIRED = "CredentialsExpiredException";
    String USERNAME_NOT_FOUND = "UsernameNotFoundException";

    String SESSION_EXPIRED = "SessionExpiredException";
}
