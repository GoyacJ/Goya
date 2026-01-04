package com.ysmjjsy.goya.security.authentication.captcha;

import com.ysmjjsy.goya.security.authentication.enums.LoginGrantType;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

/**
 * <p>登录验证码策略接口</p>
 *
 * @author goya
 * @since 2026/1/4 11:36
 */
public interface LoginCaptchaStrategy {

    /**
     * 判断当前请求是否需要进行验证码校验
     *
     * @param request 当前 Http 请求
     * @param loginType 登录类型
     * @param tenantId 租户 ID，可为空
     * @return true 表示需要校验验证码
     */
    boolean shouldValidate(HttpServletRequest request, LoginGrantType loginType, @Nullable String tenantId);
}
