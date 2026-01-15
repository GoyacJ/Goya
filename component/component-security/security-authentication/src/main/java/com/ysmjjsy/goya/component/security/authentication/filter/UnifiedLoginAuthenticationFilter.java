package com.ysmjjsy.goya.component.security.authentication.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * <p>统一登录认证过滤器</p>
 * <p>支持多种登录方式（密码、短信、社交）</p>
 * <p>使用CompositeAuthenticationConverter将请求转换为对应的AuthenticationToken</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class UnifiedLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationConverter authenticationConverter;

    public UnifiedLoginAuthenticationFilter(
            AuthenticationConverter authenticationConverter,
            AuthenticationManager authenticationManager) {
        super(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, "/login"), authenticationManager);
        this.authenticationConverter = authenticationConverter;
        log.debug("[Goya] |- security [authentication] UnifiedLoginAuthenticationFilter initialized.");
    }

    @Override
    @NullMarked
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        log.debug("[Goya] |- security [authentication] Attempting authentication for request: {}", request.getRequestURI());

        // 使用CompositeAuthenticationConverter将请求转换为AuthenticationToken
        // CompositeAuthenticationConverter会按顺序尝试多个Converter（密码、短信、社交）
        Authentication authentication = authenticationConverter.convert(request);
        if (authentication == null) {
            log.debug("[Goya] |- security [authentication] Authentication converter returned null, authentication failed.");
            throw new org.springframework.security.authentication.BadCredentialsException("无法解析登录请求");
        }

        log.debug("[Goya] |- security [authentication] Authentication token created: {}", authentication.getClass().getSimpleName());

        // 委托给AuthenticationManager进行认证
        return getAuthenticationManager().authenticate(authentication);
    }
}

