package com.ysmjjsy.goya.component.security.authentication.provider.login;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.core.utils.GoyaStringUtils;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import com.ysmjjsy.goya.component.security.authentication.provider.AbstractAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils.ACCESS_TOKEN_REQUEST_ERROR_URI;

/**
 * <p>社交登录认证转换器</p>
 * <p>从HTTP请求中提取社交登录信息</p>
 * <p>用于Authorization Code流程的用户登录认证</p>
 * <p>从登录页面（/login）的POST请求中提取社交登录参数</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * POST /login
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=social
 * &social_provider_id=wechat
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class SocialAuthenticationConverter extends AbstractAuthenticationConverter {

    public SocialAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    protected Authentication convertInternal(HttpServletRequest request, MultiValueMap<String, String> parameters, Map<String, Object> additionalParameters) {
        String social = SecurityRequestUtils.checkRequiredParameter(parameters, SecurityAuthenticationConst.PARAM_SOCIAL);
        String socialSource = SecurityRequestUtils.checkRequiredParameter(parameters, SecurityAuthenticationConst.PARAM_SOCIAL_SOURCE);
        if (GoyaStringUtils.isNotBlank(social)) {
            SocialTypeEnum socialTypeEnum = SocialTypeEnum.findByCode(social);
            if (socialTypeEnum != null) {
                return new SocialAuthenticationToken(socialTypeEnum, socialSource, additionalParameters);
            }
        }
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "", ACCESS_TOKEN_REQUEST_ERROR_URI);
        throw new OAuth2AuthenticationException(error);
    }
}

