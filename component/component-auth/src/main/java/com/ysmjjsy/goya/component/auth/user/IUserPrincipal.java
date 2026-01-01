package com.ysmjjsy.goya.component.auth.user;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
public interface IUserPrincipal extends Principal, Serializable {

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    String getUserId();

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    String getUsername();

    @Override
    default String getName() {
        return getUsername();
    }

    /**
     * 获取OpenId
     *
     * @return OpenId
     */
    String getOpenId();

    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    String getTenantId();

    /**
     * 获取用户昵称
     *
     * @return 用户昵称
     */
    String getNickname();

    /**
     * 获取用户手机号
     *
     * @return 用户手机号
     */
    String getPhoneNumber();

    /**
     * 获取用户 Email
     *
     * @return 用户 Email
     */
    String getEmail();

    /**
     * 获取用户头像
     *
     * @return 用户头像
     */
    String getAvatar();

    /**
     * 获取角色编码
     *
     * @return 角色编码
     */
    Set<String> getRoles();
}

