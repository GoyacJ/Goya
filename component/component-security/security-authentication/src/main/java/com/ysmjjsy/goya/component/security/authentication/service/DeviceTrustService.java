package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaMD5Utils;
import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserDevice;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>设备信任服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class DeviceTrustService {

    private final SecurityUserManager securityUserManager;
    private final SecurityAuthenticationProperties securityAuthenticationProperties;

    public DeviceTrustService(SecurityUserManager securityUserManager,
                              SecurityAuthenticationProperties securityAuthenticationProperties) {
        this.securityUserManager = securityUserManager;
        this.securityAuthenticationProperties = securityAuthenticationProperties;
    }

    public String resolveDeviceId(HttpServletRequest request, String requestDeviceId, String userId) {
        if (StringUtils.isNotBlank(requestDeviceId)) {
            return requestDeviceId;
        }

        String fromHeader = request.getHeader(securityAuthenticationProperties.deviceIdHeader());
        if (StringUtils.isNotBlank(fromHeader)) {
            return fromHeader;
        }

        String userAgent = request.getHeader("User-Agent");
        String ip = WebUtils.getIp(request);
        String raw = StringUtils.defaultString(userId) + ":" + StringUtils.defaultString(ip) + ":" + StringUtils.defaultString(userAgent);
        return GoyaMD5Utils.md5(raw);
    }

    public boolean isTrusted(SecurityUser securityUser, String deviceId) {
        if (securityUser == null || StringUtils.isBlank(deviceId)) {
            return false;
        }
        SecurityUserDevice userDevice = securityUserManager.findByDeviceId(deviceId);
        return userDevice != null
                && StringUtils.equals(userDevice.getUserId(), securityUser.getUserId())
                && Boolean.TRUE.equals(userDevice.getTrusted());
    }

    public void registerOrUpdate(SecurityUser securityUser, String deviceId, HttpServletRequest request) {
        if (securityUser == null || StringUtils.isBlank(deviceId)) {
            return;
        }

        SecurityUserDevice userDevice = securityUserManager.findByDeviceId(deviceId);
        if (userDevice == null) {
            UserAgent userAgent = UserAgent.userAgentParse(request);
            String deviceType = userAgent != null && userAgent.mobile() ? "MOBILE" : "DESKTOP";
            String deviceName = userAgent != null ? userAgent.osName() + "-" + userAgent.browserName() : "UNKNOWN";
            securityUserManager.registerDevice(
                    securityUser.getUserId(),
                    deviceId,
                    deviceName,
                    deviceType,
                    WebUtils.getIp(request),
                    userAgent
            );
        }

        securityUserManager.updateLastLoginTime(deviceId, LocalDateTime.now());
    }
}
