package com.ysmjjsy.goya.security.authentication.provider.authentication;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
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

    /**
     * 手机号参数名
     */
    private static final String PARAM_PHONE = "phone";

    /**
     * 短信验证码参数名
     */
    private static final String PARAM_SMS_CODE = "sms_code";

    public SmsAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    public Authentication convert(@NonNull HttpServletRequest request) {
        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);

        // 检查grant_type是否为sms（如果提供了grant_type）
        String grantType = parameters.getFirst("grant_type");
        if (grantType != null && !"sms".equalsIgnoreCase(grantType)) {
            // 如果提供了grant_type但不是sms，则此Converter不支持
            return null;
        }

        // 提取短信登录参数
        String phone = parameters.getFirst(PARAM_PHONE);
        String smsCode = parameters.getFirst(PARAM_SMS_CODE);

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(smsCode)) {
            log.debug("[Goya] |- security [authentication] Missing phone or sms_code for SMS login");
            return null;
        }

        // 验证手机号格式
        if (!isValidPhoneNumber(phone)) {
            log.debug("[Goya] |- security [authentication] Invalid phone number format: {}", phone);
            return null;
        }

        // 提取额外参数（包括DPoP参数）
        Map<String, Object> additionalParams = getAdditionalParameters(request, parameters);
        SecurityRequestUtils.validateAndAddDPoPParametersIfAvailable(request, additionalParams);

        log.debug("[Goya] |- security [authentication] Converting SMS authentication request for phone: {}", phone);

        return new SmsAuthenticationToken(phone, smsCode, additionalParams);
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return true如果格式正确，false否则
     */
    private boolean isValidPhoneNumber(String phone) {
        // 11位数字，以1开头，第二位为3-9
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
}

