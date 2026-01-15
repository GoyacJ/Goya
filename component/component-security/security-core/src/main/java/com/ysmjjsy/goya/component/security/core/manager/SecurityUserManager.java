package com.ysmjjsy.goya.component.security.core.manager;

import com.ysmjjsy.goya.component.auth.domain.UserPrincipal;
import com.ysmjjsy.goya.component.auth.service.IUserService;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import com.ysmjjsy.goya.component.social.service.SocialService;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.enums.LoginTypeEnum;
import com.ysmjjsy.goya.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.security.core.mapper.UserPrincipalToSecurityUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class SecurityUserManager implements UserDetailsService {

    private final IUserService userService;
    private final UserPrincipalToSecurityUserMapper mapper;
    private final SocialService socialService;

    /**
     * 根据登录类型获取用户
     *
     * @param loginType  登录类型
     * @param param      参数
     * @param socialType 社交类型
     * @return 用户
     */
    public SecurityUser findUser(LoginTypeEnum loginType, String param, SocialTypeEnum socialType) {
        switch (loginType) {
            case PASSWORD -> {
                return findUserByUsername(param);
            }
            case SMS -> {
                return findUserByPhone(param);
            }
            case SOCIAL -> {
                return findUserByOpenId(param, socialType);
            }
            default -> throw new SecurityAuthenticationException("un Supported login type");
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     * @throws AuthenticationException 异常
     */
    public SecurityUser findUserByUsername(String username) throws AuthenticationException {
        UserPrincipal user = userService.findUserByUsername(username);
        return mapper.toTarget(user);
    }

    /**
     * 根据用户名查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户
     * @throws AuthenticationException 异常
     */
    public SecurityUser findUserByPhone(String phoneNumber) throws AuthenticationException {
        UserPrincipal user = userService.findUserByPhone(phoneNumber);
        return mapper.toTarget(user);
    }

    /**
     * 根据 openId 查询用户
     *
     * @param openId     openId
     * @param socialType 类型
     * @throws AuthenticationException 异常
     */
    public SecurityUser findUserByOpenId(String openId, SocialTypeEnum socialType) throws AuthenticationException {
        UserPrincipal user = userService.findUserByOpenId(openId, socialType);
        return mapper.toTarget(user);
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByUsername(username);
    }
}
