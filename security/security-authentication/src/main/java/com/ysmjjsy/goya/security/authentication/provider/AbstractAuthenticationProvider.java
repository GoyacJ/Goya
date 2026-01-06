package com.ysmjjsy.goya.security.authentication.provider;

import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.enums.LoginTypeEnum;
import com.ysmjjsy.goya.security.core.manager.SecurityUserManager;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/2 15:32
 */
@Slf4j
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    private final SecurityUserManager securityUserService;

    protected AbstractAuthenticationProvider(SecurityUserManager securityUserService) {
        this.securityUserService = securityUserService;
    }

    protected abstract LoginTypeEnum loginType();

    /**
     * 认证主流程模板（不可被覆盖）
     */
    @Override
    @NullMarked
    public final Authentication authenticate(Authentication authentication) throws AuthenticationException {
        assertSupports(authentication);

        beforeAuthentication(authentication);

        try {
            // 核心认证逻辑由子类实现
            Authentication result = doAuthenticate(authentication);

            // 统一账户状态检查
            checkUserStatus(result);

            // 认证成功统一封装
            return createSuccessAuthentication(authentication, result);

        } catch (AuthenticationException ex) {
            // 统一失败处理，方便日志、审计、链路追踪
            handleAuthenticationFailure(authentication, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("[AuthProvider] |- unexpected error during authentication for principal: {}", authentication.getPrincipal(), ex);
            throw new AuthenticationServiceException("认证服务异常", ex);
        } finally {
            afterAuthentication(authentication);
        }
    }

    public UserDetails retrieveUser(String param) {
        return retrieveUser(param, null);
    }

    public UserDetails retrieveUser(String param, SocialTypeEnum socialType) {
        SecurityUser user = securityUserService.findUser(loginType(), param, socialType);
        if (user == null) {
            log.warn("[Goya] |- user not found: param: {}, socialType:{}", param, socialType);
            throw new BadCredentialsException("用户名或密码错误");
        }
        return user;
    }

    /**
     * 子类实现实际认证逻辑
     *
     * @param authentication 原始 Authentication
     * @return 认证成功后的 Authentication
     * @throws AuthenticationException 认证失败
     */
    protected abstract Authentication doAuthenticate(Authentication authentication) throws AuthenticationException;

    /**
     * 校验是否支持该 Authentication 类型
     */
    protected abstract void assertSupports(Authentication authentication);

    /**
     * 认证前钩子，默认空实现
     */
    protected void beforeAuthentication(Authentication authentication) {
        // 可在子类或统一拦截中添加预处理，如审计日志、限流等
    }

    /**
     * 认证后钩子，默认空实现
     */
    protected void afterAuthentication(Authentication authentication) {
        // 可在子类或统一拦截中添加后处理，如日志追踪
    }

    /**
     * 统一账户状态检查
     */
    protected void checkUserStatus(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        if (authentication.getPrincipal() instanceof SecurityUser user) {
            if (!user.isEnabled()) {
                throw new DisabledException("账户已禁用");
            }
            if (!user.isAccountNonLocked()) {
                throw new LockedException("账户已锁定");
            }
            if (!user.isAccountNonExpired()) {
                throw new AccountExpiredException("账户已过期");
            }
            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("凭证已过期");
            }
        }
    }

    /**
     * 统一认证成功返回 Authentication
     */
    protected Authentication createSuccessAuthentication(Authentication source, Authentication result) {
        // 如果子类已返回可直接使用的 Authentication，则保持不变
        if (result instanceof UsernamePasswordAuthenticationToken) {
            return result;
        }

        Object principal = result.getPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }

        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        token.setAuthenticated(true);
        token.setDetails(result.getDetails() != null ? result.getDetails() : source.getDetails());
        return token;
    }

    /**
     * 统一认证失败处理
     */
    protected void handleAuthenticationFailure(Authentication authentication, AuthenticationException ex) {
        log.warn("[AuthProvider] |- authentication failed for principal: {}, reason: {}",
                authentication.getPrincipal(), ex.getMessage());
        // 可拓展：记录安全日志、审计、限流、告警
    }

    @Override
    @NullMarked
    public final boolean supports(Class<?> authentication) {
        return supportsAuthentication(authentication);
    }

    /**
     * 子类定义是否支持该类型 Authentication
     */
    protected abstract boolean supportsAuthentication(Class<?> authentication);
}
