package com.ysmjjsy.goya.security.authentication.filter;

import com.ysmjjsy.goya.component.web.utils.UserAgent;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>设备管理过滤器</p>
 * <p>在登录时记录设备信息</p>
 * <p>注意：此过滤器需要在认证成功后执行</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class DeviceManagementFilter extends OncePerRequestFilter {

    private final UserDeviceService userDeviceService;

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 只在登录端点执行设备管理
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/login")) {
            // 在认证成功后记录设备信息
            // 注意：由于过滤器执行顺序，这里可能需要在认证成功处理器中调用
            // 或者使用事件监听器
            filterChain.doFilter(request, response);

            // 检查认证是否成功
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof SecurityUser securityUser) {
                try {
                    // 提取用户ID
                    String userId = securityUser.getUserId();

                    if (StringUtils.isNotBlank(userId)) {
                        // 生成设备指纹
                        String deviceId = WebUtils.generateDeviceId(request);
                        String deviceName = WebUtils.extractDeviceName(request);
                        String deviceType = WebUtils.identifyDeviceType(request);
                        String ipAddress = WebUtils.getClientIp(request);
                        UserAgent userAgent = WebUtils.getUserAgent(request);

                        // 注册或更新设备
                        com.ysmjjsy.goya.component.auth.domain.UserDevice device = userDeviceService.findByDeviceId(deviceId);
                        if (device == null) {
                            // 新设备，注册
                            userDeviceService.registerDevice(userId, deviceId, deviceName, deviceType, ipAddress, userAgent);
                            log.debug("[Goya] |- security [authentication] Device registered: {} | user: {}", deviceId, userId);
                        } else {
                            // 更新最后登录时间
                            userDeviceService.updateLastLoginTime(deviceId);
                            log.debug("[Goya] |- security [authentication] Device login time updated: {} | user: {}", deviceId, userId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[Goya] |- security [authentication] Failed to record device information", e);
                    // 设备管理失败不应影响认证流程
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

