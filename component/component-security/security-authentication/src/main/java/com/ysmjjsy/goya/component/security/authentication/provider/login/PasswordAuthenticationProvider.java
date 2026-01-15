package com.ysmjjsy.goya.component.security.authentication.provider.login;

import com.ysmjjsy.goya.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationProvider;
import com.ysmjjsy.goya.security.core.enums.LoginTypeEnum;
import com.ysmjjsy.goya.security.core.manager.SecurityUserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * <p>密码登录认证提供者</p>
 * <p>处理用户名密码登录方式的认证</p>
 * <p>符合Spring Security标准模式，实现AuthenticationProvider接口</p>
 * <p>用于Authorization Code流程，返回UsernamePasswordAuthenticationToken让SAS框架处理授权码生成</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收PasswordAuthenticationToken（包含用户名和密码）</li>
 *   <li>验证用户凭证</li>
 *   <li>返回UsernamePasswordAuthenticationToken（让SAS框架处理授权码生成）</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class PasswordAuthenticationProvider extends AbstractAuthenticationProvider {

    private final PasswordPolicyValidator passwordPolicyValidator;

    public PasswordAuthenticationProvider(SecurityUserManager securityUserService, PasswordPolicyValidator passwordPolicyValidator) {
        super(securityUserService);
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    protected LoginTypeEnum loginType() {
        return LoginTypeEnum.PASSWORD;
    }

    @Override
    protected Authentication doAuthenticate(Authentication authentication) throws AuthenticationException {
        PasswordAuthenticationToken passwordAuthenticationToken = (PasswordAuthenticationToken) authentication;

        String username = (String) passwordAuthenticationToken.getPrincipal();
        String password = (String) passwordAuthenticationToken.getCredentials();

        UserDetails user = retrieveUser(username);
        if (!passwordPolicyValidator.matched(password, user.getPassword())) {
            log.warn("[Goya] |- password mismatch for user: {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 校验密码状态
        passwordPolicyValidator.validate(user.getPassword());

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    protected void assertSupports(Authentication authentication) {
        if (!(authentication instanceof PasswordAuthenticationToken)) {
            throw new AuthenticationServiceException("Unsupported authentication type: " + authentication.getClass());
        }
    }

    @Override
    protected boolean supportsAuthentication(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

