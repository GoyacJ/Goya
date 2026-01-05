package com.ysmjjsy.goya.security.core.service;

import com.ysmjjsy.goya.component.auth.service.IAuthService;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>用户服务接口</p>
 *
 * @author goya
 * @since 2025/10/10 14:25
 */
public interface ISecurityUserService extends IAuthService, UserDetailsService {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     * @throws AuthenticationException 异常
     */
    @Override
    SecurityUser findUser(String username) throws AuthenticationException;


    @Override
    @NullMarked
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUser(username);
    }

    /**
     * 从社交登录创建用户
     * <p>根据第三方用户属性创建新用户</p>
     *
     * @param registrationId 第三方登录提供商ID（wechat、gitee、github等）
     * @param attributes 第三方用户属性
     * @param defaultRole 默认角色（可选）
     * @return 创建的用户
     * @throws AuthenticationException 如果创建失败
     */
    default SecurityUser createUserFromSocialLogin(String registrationId, java.util.Map<String, Object> attributes, String defaultRole) throws AuthenticationException {
        throw new UnsupportedOperationException("createUserFromSocialLogin not implemented");
    }
}
