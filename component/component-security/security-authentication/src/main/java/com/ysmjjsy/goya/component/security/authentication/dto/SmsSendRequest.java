package com.ysmjjsy.goya.component.security.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>短信验证码发送请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "短信验证码发送请求")
public record SmsSendRequest(
        String tenantId,
        String target
) {
}
