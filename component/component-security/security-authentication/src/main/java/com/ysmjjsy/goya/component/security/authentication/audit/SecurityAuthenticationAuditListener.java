package com.ysmjjsy.goya.component.security.authentication.audit;

import com.ysmjjsy.goya.component.security.authentication.login.LoginFailureCacheManger;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.web.utils.UserAgent;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import java.util.Objects;

/**
 * <p>认证审计事件监听器</p>
 * <p>监听Spring Security的认证事件，记录审计日志</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityAuthenticationAuditListener {

    private final SecurityUserManager securityUserManager;
    private final LoginFailureCacheManger loginFailureCacheManger;

    /**
     * 监听认证成功事件
     *
     * @param event 认证成功事件
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            HttpServletRequest request = WebUtils.getRequest();
            if (Objects.isNull(request)) {
                return;
            }
            String username = authentication.getName();
            String ipAddress = WebUtils.getClientIp(request);
            UserAgent userAgent = WebUtils.getUserAgent();
            String requestUri = request.getRequestURI();

            // 尝试从Authentication中提取用户信息
            Object principal = authentication.getPrincipal();
            String userId = null;
            String tenantId = null;

            if (principal instanceof SecurityUser securityUser) {
                // 如果是SecurityUser，可以提取更多信息
                try {
                    userId = securityUser.getUserId();
                    tenantId = securityUser.getTenantId();
                } catch (Exception _) {
                    // 忽略反射异常
                }
            }

            loginFailureCacheManger.clear(userId);
            // 记录登录成功审计日志
            securityUserManager.recordLoginSuccess(userId, username, tenantId, ipAddress, userAgent, requestUri);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to record authentication success audit log", e);
        }
    }

    /**
     * 监听认证失败事件
     *
     * @param event 认证失败事件
     */
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            HttpServletRequest request = WebUtils.getRequest();

            if (request == null) {
                return;
            }

            String username = authentication.getName();
            String ipAddress = WebUtils.getClientIp(request);
            UserAgent userAgent = WebUtils.getUserAgent();
            String requestUri = request.getRequestURI();
            String errorMessage = event.getException().getMessage();

            String userId = null;

            if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
                // 如果是SecurityUser，可以提取更多信息
                try {
                    userId = securityUser.getUserId();
                } catch (Exception _) {
                    // 忽略反射异常
                }
            }


            if (loginFailureCacheManger.checkErrorTimes(userId)) {
                securityUserManager.lockedUser(userId);
            }
            // 记录登录失败审计日志
            securityUserManager.recordLoginFailure(username, ipAddress, userAgent, requestUri, errorMessage);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to record authentication failure audit log", e);
        }
    }
}

