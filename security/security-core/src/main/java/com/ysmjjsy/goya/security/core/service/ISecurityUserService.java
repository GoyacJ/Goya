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
}
