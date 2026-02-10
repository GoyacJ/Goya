package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;

import java.util.Map;

/**
 * <p>登录风险上下文</p>
 *
 * @param user       用户信息
 * @param tenantId   租户ID
 * @param clientType 客户端类型
 * @param deviceId   设备ID
 * @param ipAddress  客户端IP
 * @param userAgent  用户代理
 * @param attributes 扩展属性
 * @author goya
 * @since 2026/2/10
 */
public record SecurityRiskContext(
        SecurityUser user,
        String tenantId,
        ClientTypeEnum clientType,
        String deviceId,
        String ipAddress,
        UserAgent userAgent,
        Map<String, Object> attributes
) {
}
