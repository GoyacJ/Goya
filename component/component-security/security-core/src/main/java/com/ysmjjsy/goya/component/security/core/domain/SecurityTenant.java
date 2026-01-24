package com.ysmjjsy.goya.component.security.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>租户基础信息</p>
 *
 * @param tenantId 租户ID
 * @param issuer   租户Issuer（可选，默认由请求推导）
 * @param jwkSetUri 租户JWK Set URI（可选）
 * @param enabled  是否启用
 */
@Schema(description = "租户基础信息")
public record SecurityTenant(
        String tenantId,
        String issuer,
        String jwkSetUri,
        boolean enabled
) {
}
