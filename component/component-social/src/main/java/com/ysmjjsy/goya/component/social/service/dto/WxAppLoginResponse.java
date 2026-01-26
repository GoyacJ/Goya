package com.ysmjjsy.goya.component.social.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>微信小程序登录响应</p>
 *
 * @author goya
 * @since 2026/1/1 23:06
 */
@Schema(defaultValue = "微信小程序登录响应")
public record WxAppLoginResponse(
        String sessionKey,
        String openid,
        String unionid
) {
}
