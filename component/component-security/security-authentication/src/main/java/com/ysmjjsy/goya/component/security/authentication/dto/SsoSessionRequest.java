package com.ysmjjsy.goya.component.security.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>SSO 会话建立请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "SSO会话建立请求")
public record SsoSessionRequest(
        String preAuthCode,
        String continueUri,
        String continueTo,
        String continueUrl
) {

    public String resolveContinueUri() {
        if (StringUtils.isNotBlank(continueUri)) {
            return continueUri;
        }
        if (StringUtils.isNotBlank(continueTo)) {
            return continueTo;
        }
        return continueUrl;
    }
}
