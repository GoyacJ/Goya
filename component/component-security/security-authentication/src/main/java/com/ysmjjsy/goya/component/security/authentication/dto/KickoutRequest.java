package com.ysmjjsy.goya.component.security.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>踢出请求</p>
 *
 * @author goya
 * @since 2026/2/25
 */
@Schema(defaultValue = "踢出请求")
public record KickoutRequest(
        String tenantId,
        String userId,
        String clientId,
        String reason
) {
}
