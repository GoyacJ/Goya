package com.ysmjjsy.goya.security.authentication.userinfo;

import com.ysmjjsy.goya.security.core.constants.IStandardClaimNamesConstants;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>UserInfo响应映射器</p>
 * 自定义UserInfo端点响应，从JWT或数据库获取用户信息
 *
 * @author goya
 * @since 2025/12/17 21:30
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserInfoMapper
        implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final ISecurityUserService securityUserService;

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {

        OidcUserInfoAuthenticationToken authentication =
                context.getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();

        String subject = jwt.getSubject();
        claims.put(StandardClaimNames.SUB, subject);

        String tenantId = jwt.getClaimAsString(IStandardClaimNamesConstants.TENANT_ID);
        if (StringUtils.isNotBlank(tenantId)) {
            claims.put(IStandardClaimNamesConstants.TENANT_ID, tenantId);
        }

        SecurityUser user = securityUserService.findUser(subject);
        if (user == null) {
            return new OidcUserInfo(claims);
        }

        Set<String> scopes = new HashSet<>(jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE));

        // ===== profile =====
        if (scopes.contains(OidcScopes.PROFILE)) {
            claims.put(StandardClaimNames.PREFERRED_USERNAME, user.getUsername());
            putIfNotBlank(claims, StandardClaimNames.NICKNAME, user.getNickname());
            putIfNotBlank(claims, StandardClaimNames.PICTURE, user.getAvatar());
        }

        // ===== email =====
        if (scopes.contains(OidcScopes.EMAIL) && StringUtils.isNotBlank(user.getEmail())) {
            claims.put(StandardClaimNames.EMAIL, user.getEmail());
            claims.put(StandardClaimNames.EMAIL_VERIFIED, true);
        }

        // ===== phone =====
        if (scopes.contains(OidcScopes.PHONE) && StringUtils.isNotBlank(user.getPhoneNumber())) {
            claims.put(StandardClaimNames.PHONE_NUMBER, user.getPhoneNumber());
            claims.put(StandardClaimNames.PHONE_NUMBER_VERIFIED, true);
        }

        // ===== 扩展（非 OIDC 标准）=====
        if (CollectionUtils.isNotEmpty(user.getRoles())) {
            claims.put(IStandardClaimNamesConstants.ROLES, user.getRoles());
        }

        if (CollectionUtils.isNotEmpty(user.getAuthorities())) {
            claims.put(
                    IStandardClaimNamesConstants.AUTHORITIES,
                    user.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet())
            );
        }

        return new OidcUserInfo(claims);
    }

    public static <K, V> void putIfNotBlank(Map<K, V> map, K name, V value) {
        if (ObjectUtils.isNotEmpty(value)) {
            map.put(name, value);
        }
    }
}
