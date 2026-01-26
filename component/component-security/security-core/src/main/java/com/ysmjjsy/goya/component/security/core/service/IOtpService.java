package com.ysmjjsy.goya.component.security.core.service;

import org.jspecify.annotations.Nullable;

/**
 * <p>OTP服务SPI（短信/邮箱验证码）</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IOtpService {

    /**
     * 校验OTP
     *
     * @param tenantId 租户ID（可为空）
     * @param target   目标（手机号或邮箱）
     * @param code     验证码
     * @return 是否通过
     */
    boolean verify(@Nullable String tenantId, String target, String code);

    /**
     * 发送OTP（可选）
     *
     * @param tenantId 租户ID
     * @param target   目标（手机号或邮箱）
     */
    default void send(@Nullable String tenantId, String target) {
        // default no-op
    }
}
