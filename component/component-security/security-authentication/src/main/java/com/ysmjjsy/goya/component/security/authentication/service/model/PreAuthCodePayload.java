package com.ysmjjsy.goya.component.security.authentication.service.model;

import com.ysmjjsy.goya.component.security.core.domain.SecurityGrantedAuthority;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>预认证码负载</p>
 *
 * @param userId           用户ID
 * @param username         用户名
 * @param tenantId         租户ID
 * @param openId           OpenId
 * @param phoneNumber      手机号
 * @param email            邮箱
 * @param roles            角色
 * @param authorities      权限
 * @param clientType       客户端类型
 * @param clientId         OAuth2 客户端ID
 * @param mfaVerified      是否完成MFA
 * @param sid              会话ID
 * @param deviceId         设备ID
 * @param dpopJkt          DPoP指纹
 * @param issuedAtEpochSec 签发时间
 * @author goya
 * @since 2026/2/10
 */
public record PreAuthCodePayload(
        String userId,
        String username,
        String tenantId,
        String openId,
        String phoneNumber,
        String email,
        Set<String> roles,
        Set<String> authorities,
        ClientTypeEnum clientType,
        String clientId,
        boolean mfaVerified,
        String sid,
        String deviceId,
        String dpopJkt,
        long issuedAtEpochSec
) {

    public static PreAuthCodePayload fromUser(SecurityUser securityUser,
                                              String tenantId,
                                              ClientTypeEnum clientType,
                                              String clientId,
                                              String sid,
                                              String deviceId,
                                              boolean mfaVerified,
                                              String dpopJkt) {
        Set<String> authorities = securityUser.getAuthorities() == null
                ? Set.of()
                : securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> roles = securityUser.getRoles() == null ? Set.of() : new LinkedHashSet<>(securityUser.getRoles());

        return new PreAuthCodePayload(
                securityUser.getUserId(),
                securityUser.getUsername(),
                StringUtils.isNotBlank(tenantId) ? tenantId : securityUser.getTenantId(),
                securityUser.getOpenId(),
                securityUser.getPhoneNumber(),
                securityUser.getEmail(),
                roles,
                authorities,
                clientType,
                clientId,
                mfaVerified,
                sid,
                deviceId,
                dpopJkt,
                Instant.now().getEpochSecond()
        );
    }

    public PreAuthCodePayload withMfaVerified(boolean mfaVerified) {
        return new PreAuthCodePayload(
                userId,
                username,
                tenantId,
                openId,
                phoneNumber,
                email,
                roles,
                authorities,
                clientType,
                clientId,
                mfaVerified,
                sid,
                deviceId,
                dpopJkt,
                issuedAtEpochSec
        );
    }

    public PreAuthCodePayload withDeviceId(String deviceId) {
        return new PreAuthCodePayload(
                userId,
                username,
                tenantId,
                openId,
                phoneNumber,
                email,
                roles,
                authorities,
                clientType,
                clientId,
                mfaVerified,
                sid,
                deviceId,
                dpopJkt,
                issuedAtEpochSec
        );
    }

    public SecurityUser toSecurityUser() {
        LinkedHashSet<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
        if (authorities != null) {
            authorities.forEach(authority -> grantedAuthorities.add(new SecurityGrantedAuthority(authority)));
        }
        if (roles != null) {
            roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .forEach(role -> grantedAuthorities.add(new SecurityGrantedAuthority(role)));
        }

        return SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password(StringUtils.EMPTY)
                .openId(openId)
                .tenantId(tenantId)
                .phoneNumber(phoneNumber)
                .email(email)
                .roles(roles == null ? Set.of() : roles)
                .authorities(grantedAuthorities)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }
}
