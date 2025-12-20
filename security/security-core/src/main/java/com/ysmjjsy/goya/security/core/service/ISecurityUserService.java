package com.ysmjjsy.goya.security.core.service;

import com.ysmjjsy.goya.component.security.user.IUserService;
import com.ysmjjsy.goya.component.social.domain.AccessPrincipal;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
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
public interface ISecurityUserService extends IUserService, UserDetailsService {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     * @throws AuthenticationException 异常
     */
    SecurityUser findUser(String username) throws AuthenticationException;

    /**
     * 根据社交账号查询用户
     *
     * @param source          社交账号
     * @param accessPrincipal 访问凭证
     * @return 用户
     * @throws AuthenticationException 异常
     */
    SecurityUser findUser(String source, AccessPrincipal accessPrincipal) throws AuthenticationException;

    @Override
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUser(username);
    }
}
