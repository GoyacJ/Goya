package com.ysmjjsy.goya.component.auth.service;

import com.ysmjjsy.goya.component.auth.user.IUserPrincipal;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p></p>
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
    IUserPrincipal currentUser(HttpServletRequest httpServletRequest);

    /**
     * 当前用户信息
     *
     * @return 当前用户
     */
    IUserPrincipal currentUser();

    /**
     * 通过token解析用户信息
     *
     * @param token token
     * @return 用户信息
     */
    IUserPrincipal resolve(String token);

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
    static IUserPrincipal getCurrentUser() {
        return SpringContext.getBean(IAuthService.class).currentUser();
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    IUserPrincipal findUser(String username);
}
