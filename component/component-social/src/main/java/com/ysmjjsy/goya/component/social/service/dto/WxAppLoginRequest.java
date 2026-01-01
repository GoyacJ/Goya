package com.ysmjjsy.goya.component.social.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>微信小程序登录</p>
 *
 * @author goya
 * @since 2026/1/1 23:05
 */
@Schema(defaultValue = "微信小程序登录")
public record WxAppLoginRequest(
        String code,
        String appId
) {
}
