package com.ysmjjsy.goya.security.authentication.provider.sms;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * <p>短信验证码Grant Type认证转换器</p>
 * <p>从HTTP请求中提取手机号和短信验证码参数，转换为SmsGrantAuthenticationToken</p>
 * <p>这是OAuth2.1的非标准扩展，仅用于企业内部系统</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * POST /oauth2/token
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=sms
 * &phone=13800138000
 * &sms_code=123456
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see SmsGrantAuthenticationToken
 * @see SmsGrantAuthenticationProvider
 */
@Slf4j
public class SmsGrantAuthenticationConverter implements AuthenticationConverter {

    /**
     * Grant Type标识符
     * 注意：这是非标准扩展，OAuth2.1已移除Resource Owner Password Credentials Grant
     */
    private static final String GRANT_TYPE_SMS = "sms";

    /**
     * 手机号参数名
     */
    private static final String PARAM_PHONE = "phone";

    /**
     * 短信验证码参数名
     */
    private static final String PARAM_SMS_CODE = "sms_code";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 1. 检查grant_type参数是否为sms
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!GRANT_TYPE_SMS.equals(grantType)) {
            // 如果不是sms grant type，返回null，让其他转换器处理
            return null;
        }

        // 2. 提取客户端ID（从请求参数或认证头中获取）
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            log.debug("[Goya] |- security [authentication] SMS grant type requires client_id parameter.");
            return null;
        }

        // 3. 提取手机号
        String phone = request.getParameter(PARAM_PHONE);
        if (StringUtils.isBlank(phone)) {
            log.debug("[Goya] |- security [authentication] SMS grant type requires phone parameter.");
            return null;
        }

        // 4. 验证手机号格式（11位数字，以1开头）
        if (!isValidPhoneNumber(phone)) {
            log.debug("[Goya] |- security [authentication] Invalid phone number format: {}", phone);
            return null;
        }

        // 5. 提取短信验证码
        String smsCode = request.getParameter(PARAM_SMS_CODE);
        if (StringUtils.isBlank(smsCode)) {
            log.debug("[Goya] |- security [authentication] SMS grant type requires sms_code parameter.");
            return null;
        }

        // 6. 提取scope（可选）
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);

        log.debug("[Goya] |- security [authentication] Converting SMS grant authentication request for phone: {}", phone);

        // 7. 创建并返回SmsGrantAuthenticationToken
        return new SmsGrantAuthenticationToken(
                clientId,
                phone,
                smsCode,
                scope != null ? StringUtils.split(scope, " ") : null
        );
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

