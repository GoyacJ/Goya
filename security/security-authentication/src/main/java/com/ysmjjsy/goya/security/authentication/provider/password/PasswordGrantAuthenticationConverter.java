package com.ysmjjsy.goya.security.authentication.provider.password;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
import com.ysmjjsy.goya.security.authentication.enums.LoginGrantType;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationConverter;
import com.ysmjjsy.goya.security.authentication.utils.SecurityRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * <p>密码Grant Type认证转换器</p>
 * <p>从HTTP请求中提取用户名、密码、验证码等参数，转换为PasswordGrantAuthenticationToken</p>
 * <p>这是OAuth2.1的非标准扩展，仅用于企业内部系统</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * POST /oauth2/token
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=password
 * &username=user@example.com
 * &password=password123
 * &captcha=1234
 * &captcha_key=uuid-key
 * }</pre>
 *
 * @author goya
 * @see PasswordGrantAuthenticationToken
 * @see PasswordGrantAuthenticationProvider
 * @since 2025/12/21
 */
@Slf4j
public class PasswordGrantAuthenticationConverter extends AbstractAuthenticationConverter {

    protected PasswordGrantAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    public Authentication convert(@NonNull HttpServletRequest request) {
        // 1. 检查grant_type参数是否为password
        if (!LoginGrantType.check(request,LoginGrantType.PASSWORD)) {
            // 如果不是password grant type，返回null，让其他转换器处理
            return null;
        }

        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);
        String scope = SecurityRequestUtils.checkOptionalParameter(parameters, OAuth2ParameterNames.SCOPE);

        // username (REQUIRED)
        SecurityRequestUtils.checkRequiredParameter(parameters, ISecurityAuthenticationConstants.USERNAME);

        // password (REQUIRED)
        SecurityRequestUtils.checkRequiredParameter(parameters, ISecurityAuthenticationConstants.PASSWORD);

        Map<String, Object> additionalParameters = getAdditionalParameters(request, parameters);
        // Validate DPoP Proof HTTP Header (if available)
        SecurityRequestUtils.validateAndAddDPoPParametersIfAvailable(request, additionalParameters);

        return new PasswordGrantAuthenticationToken(
                getClientPrincipal(),
                getRequestedScopes(scope),
                additionalParameters
        );
    }
}

