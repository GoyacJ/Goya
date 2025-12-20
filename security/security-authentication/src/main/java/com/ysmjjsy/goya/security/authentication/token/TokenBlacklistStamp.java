package com.ysmjjsy.goya.security.authentication.token;

import com.ysmjjsy.goya.component.cache.factory.JetCacheFactory;
import com.ysmjjsy.goya.component.cache.wrapper.AbstractStampCache;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Token黑名单签章</p>
 *
 * @author goya
 * @since 2025/12/17 22:41
 */
@Slf4j
public class TokenBlacklistStamp extends AbstractStampCache<String, String> {

    private final SecurityAuthenticationProperties.TokenBlackListConfig tokenBlackListConfig;

    protected TokenBlacklistStamp(JetCacheFactory jetCacheFactory, SecurityAuthenticationProperties.TokenBlackListConfig tokenBlackListConfig) {
        super(ISecurityAuthenticationConstants.CACHE_SECURITY_AUTHENTICATION_TOKEN_BLACK_LIST_PREFIX, tokenBlackListConfig.tokenBlackListExpire(), jetCacheFactory);
        this.tokenBlackListConfig = tokenBlackListConfig;
    }

    @Override
    public String nextStamp(String key) {
        return tokenBlackListConfig.defaultReason();
    }
}
