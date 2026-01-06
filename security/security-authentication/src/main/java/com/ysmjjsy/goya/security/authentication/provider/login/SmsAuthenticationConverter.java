package com.ysmjjsy.goya.security.authentication.provider.login;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationConverter;
import com.ysmjjsy.goya.security.authentication.utils.SecurityRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * <p>短信登录认证转换器</p>
 * <p>从HTTP请求中提取短信登录信息</p>
 * <p>用于Authorization Code流程的用户登录认证</p>
 * <p>从登录页面（/login）的POST请求中提取短信登录参数</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * POST /login
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=sms
 * &phone=13800138000
 * &sms_code=123456
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class SmsAuthenticationConverter extends AbstractAuthenticationConverter {
    public SmsAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    protected Authentication convertInternal(
            HttpServletRequest request,
            MultiValueMap<String, String> parameters,
            Map<String, Object> additionalParameters) {

        SecurityRequestUtils.checkRequiredParameter(parameters, ISecurityAuthenticationConstants.PARAM_PHONE_NUMBER);
        SecurityRequestUtils.checkRequiredParameter(parameters, ISecurityAuthenticationConstants.PARAM_SMS_CODE);

        String phoneNumber = (String) additionalParameters.get(ISecurityAuthenticationConstants.PARAM_PHONE_NUMBER);
        String smsCode = (String) additionalParameters.get(ISecurityAuthenticationConstants.PARAM_SMS_CODE);

        log.debug("[Goya] |- security [authentication] Convert SMS login for phone: {}", phoneNumber);

        return new SmsAuthenticationToken(phoneNumber, smsCode, additionalParameters);
    }
}

