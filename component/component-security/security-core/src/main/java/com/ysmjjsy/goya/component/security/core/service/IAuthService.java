package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.auth.domain.UserPrincipal;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>认证服务</p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
public interface IAuthService {

    /**
     * 当前用户信息
     *
     * @return 当前用户
     */
    UserPrincipal currentUser(HttpServletRequest httpServletRequest);

    /**
     * 当前用户信息
     *
     * @return 当前用户
     */
    UserPrincipal currentUser();

    /**
     * 通过 token 解析用户信息
     *
     * @param token token
     * @return 用户信息
     */
    UserPrincipal resolve(String token);

    /**
     * 当前租户
     *
     * @return 当前租户
     */
    default String currentTenant() {
        return TenantContext.getTenantId();
    }

    /**
     * 当前用户信息
     *
     * @return 当前用户
     */
    static UserPrincipal getCurrentUser() {
        return SpringContext.getBean(IAuthService.class).currentUser();
    }
}
