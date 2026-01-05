package com.ysmjjsy.goya.security.authentication.provider.authentication;

import com.ysmjjsy.goya.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private final ISecurityUserService securityUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public PasswordAuthenticationProvider(
            ISecurityUserService securityUserService,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator) {
        this.securityUserService = securityUserService;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordAuthenticationToken passwordToken = (PasswordAuthenticationToken) authentication;

        String username = passwordToken.getUsername();
        String password = passwordToken.getPassword();

        log.debug("[Goya] |- security [authentication] Authenticating user with password: {}", username);

        // 验证用户凭证
        SecurityUser user = validateUserCredentials(username, password);

        // 返回 Spring Security 标准认证对象
        // 关键：这里返回 UsernamePasswordAuthenticationToken，让 SAS 框架处理授权码生成
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,  // credentials 已擦除
                        user.getAuthorities()
                );

        log.debug("[Goya] |- security [authentication] Password authentication successful for user: {}, returning UsernamePasswordAuthenticationToken for Authorization Code flow",
                user.getUsername());

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证用户凭证
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     * @throws AuthenticationException 如果用户认证失败
     */
    private SecurityUser validateUserCredentials(String username, String password) {
        try {
            // 查找用户
            SecurityUser user = securityUserService.findUser(username);
            if (user == null) {
                log.warn("[Goya] |- security [authentication] User not found: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("[Goya] |- security [authentication] Password mismatch for user: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 检查账户状态
            if (!user.isEnabled()) {
                throw new BadCredentialsException("账户已被禁用");
            }
            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("账户已被锁定");
            }
            if (!user.isAccountNonExpired()) {
                throw new BadCredentialsException("账户已过期");
            }
            if (!user.isCredentialsNonExpired()) {
                throw new BadCredentialsException("凭证已过期");
            }

            return user;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Error validating user credentials for: {}", username, e);
            throw new BadCredentialsException("用户认证失败", e);
        }
    }
}

