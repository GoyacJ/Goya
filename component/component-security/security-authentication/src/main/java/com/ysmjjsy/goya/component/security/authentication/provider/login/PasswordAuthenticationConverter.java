package com.ysmjjsy.goya.component.security.authentication.provider.login;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import com.ysmjjsy.goya.component.security.authentication.provider.AbstractAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.component.security.core.enums.LoginTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

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
    protected Authentication convertInternal(
            HttpServletRequest request,
            MultiValueMap<String, String> parameters,
            Map<String, Object> additionalParameters) {

        LoginTypeEnum loginType = LoginTypeEnum.resolve(request);
        String loginTypeParam = request.getParameter("login_type");
        if (loginType != null && loginType != LoginTypeEnum.PASSWORD) {
            return null;
        }
        if (loginType == null && StringUtils.hasText(loginTypeParam)) {
            return null;
        }

        SecurityRequestUtils.checkRequiredParameter(
                parameters, SecurityAuthenticationConst.PARAM_USERNAME);
        SecurityRequestUtils.checkRequiredParameter(
                parameters, SecurityAuthenticationConst.PARAM_PASSWORD);

        String username =
                (String) additionalParameters.get(
                        SecurityAuthenticationConst.PARAM_USERNAME);

        String password =
                (String) additionalParameters.get(
                        SecurityAuthenticationConst.PARAM_PASSWORD);

        log.debug("[Goya] |- security [authentication] Convert password login for user: {}", username);

        return new PasswordAuthenticationToken(
                username, password, additionalParameters);
    }

}
