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

    /**
     * 社交登录提供商ID参数名
     */
    private static final String PARAM_SOCIAL_PROVIDER_ID = "social_provider_id";

    public SocialAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        super(cryptoProcessor);
    }

    @Override
    public Authentication convert(@NonNull HttpServletRequest request) {
        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);

        // 检查grant_type是否为social（如果提供了grant_type）
        String grantType = parameters.getFirst("grant_type");
        if (grantType != null && !"social".equalsIgnoreCase(grantType)) {
            // 如果提供了grant_type但不是social，则此Converter不支持
            return null;
        }

        // 提取社交登录参数
        String socialProviderId = parameters.getFirst(PARAM_SOCIAL_PROVIDER_ID);
        // 社交登录的用户属性通常从OAuth2回调中获取，这里可能需要从session或其他地方获取
        // 为了简化，这里假设从additionalParams中获取
        Map<String, Object> additionalParams = getAdditionalParameters(request, parameters);
        SecurityRequestUtils.validateAndAddDPoPParametersIfAvailable(request, additionalParams);

        @SuppressWarnings("unchecked")
        Map<String, Object> socialUserAttributes = (Map<String, Object>) additionalParams.get("social_user_attributes");

        if (StringUtils.isBlank(socialProviderId)) {
            log.debug("[Goya] |- security [authentication] Missing social_provider_id for social login");
            return null;
        }

        log.debug("[Goya] |- security [authentication] Converting social authentication request for provider: {}", socialProviderId);

        return new SocialAuthenticationToken(socialProviderId, socialUserAttributes, additionalParams);
    }
}

