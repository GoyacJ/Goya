package com.ysmjjsy.goya.component.security.authentication.provider;

import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import com.ysmjjsy.goya.component.security.authentication.provider.login.PasswordAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SmsAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SocialAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.component.security.core.enums.LoginTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.EnumMap;
import java.util.Map;

/**
 * <p>登录认证转换器</p>
 * <p>根据登录类型选择具体转换器</p>
 *
 * @author goya
 * @since 2026/1/26
 */
@Slf4j
@RequiredArgsConstructor
public class LoginAuthenticationConverter implements AuthenticationConverter {

    private final Map<LoginTypeEnum, AuthenticationConverter> converters;

    public static LoginAuthenticationConverter create(PasswordAuthenticationConverter passwordConverter,
                                                      SmsAuthenticationConverter smsConverter,
                                                      SocialAuthenticationConverter socialConverter) {
        Map<LoginTypeEnum, AuthenticationConverter> mapping = new EnumMap<>(LoginTypeEnum.class);
        mapping.put(LoginTypeEnum.PASSWORD, passwordConverter);
        mapping.put(LoginTypeEnum.SMS, smsConverter);
        mapping.put(LoginTypeEnum.SOCIAL, socialConverter);
        return new LoginAuthenticationConverter(mapping);
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        LoginTypeEnum loginType = LoginTypeEnum.resolve(request);
        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);

        if (loginType != null) {
            AuthenticationConverter converter = converters.get(loginType);
            if (converter == null) {
                SecurityRequestUtils.throwParameterError(OAuth2ErrorCodes.INVALID_REQUEST, "login_type");
                return null;
            }
            log.debug("[Goya] |- security [authentication] Resolve login type: {}", loginType.getCode());
            return converter.convert(request);
        }

        if (hasParameter(parameters, SecurityAuthenticationConst.PARAM_USERNAME)
                && hasParameter(parameters, SecurityAuthenticationConst.PARAM_PASSWORD)) {
            return converters.get(LoginTypeEnum.PASSWORD).convert(request);
        }

        if (hasParameter(parameters, SecurityAuthenticationConst.PARAM_PHONE_NUMBER)
                && hasParameter(parameters, SecurityAuthenticationConst.PARAM_SMS_CODE)) {
            return converters.get(LoginTypeEnum.SMS).convert(request);
        }

        if (hasParameter(parameters, SecurityAuthenticationConst.PARAM_SOCIAL)
                && hasParameter(parameters, SecurityAuthenticationConst.PARAM_SOCIAL_SOURCE)) {
            return converters.get(LoginTypeEnum.SOCIAL).convert(request);
        }

        return null;
    }

    private boolean hasParameter(MultiValueMap<String, String> parameters, String name) {
        String value = parameters.getFirst(name);
        return StringUtils.hasText(value);
    }
}