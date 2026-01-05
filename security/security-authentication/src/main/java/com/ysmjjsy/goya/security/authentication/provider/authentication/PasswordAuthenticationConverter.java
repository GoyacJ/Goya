package com.ysmjjsy.goya.security.authentication.provider.authentication;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationConverter;
import com.ysmjjsy.goya.security.authentication.utils.SecurityRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * <p>密码登录认证转换器</p>
 * <p>从HTTP请求中提取密码登录信息</p>
 * <p>用于Authorization Code流程的用户登录认证</p>
 * <p>从登录页面（/login）的POST请求中提取密码登录参数</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * POST /login
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
 * @since 2025/12/21
 */
@Slf4j
public class PasswordAuthenticationConverter extends AbstractAuthenticationConverter {

    public PasswordAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    public Authentication convert(@NonNull HttpServletRequest request) {
        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);

        // 检查grant_type是否为password（如果提供了grant_type）
        String grantType = parameters.getFirst("grant_type");
        if (grantType != null && !"password".equalsIgnoreCase(grantType)) {
            // 如果提供了grant_type但不是password，则此Converter不支持
            return null;
        }

        // 提取密码登录参数
        String username = parameters.getFirst(ISecurityAuthenticationConstants.USERNAME);
        String password = parameters.getFirst(ISecurityAuthenticationConstants.PASSWORD);
        String captcha = parameters.getFirst("captcha");
        String captchaKey = parameters.getFirst("captcha_key");

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            log.debug("[Goya] |- security [authentication] Missing username or password for password login");
            return null;
        }

        // 提取额外参数（包括DPoP参数）
        Map<String, Object> additionalParams = getAdditionalParameters(request, parameters);
        SecurityRequestUtils.validateAndAddDPoPParametersIfAvailable(request, additionalParams);

        log.debug("[Goya] |- security [authentication] Converting password authentication request for user: {}", username);

        return new PasswordAuthenticationToken(username, password, captcha, captchaKey, additionalParams);
    }
}

