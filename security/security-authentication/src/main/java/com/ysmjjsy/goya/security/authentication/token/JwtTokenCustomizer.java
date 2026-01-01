package com.ysmjjsy.goya.security.authentication.token;

import com.ysmjjsy.goya.security.core.constants.IStandardClaimNamesConstants;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>JWT Token自定义器</p>
 *
 * @author goya
 * @since 2025/12/17 21:53
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        OAuth2TokenType tokenType = context.getTokenType();
        Authentication authentication = context.getPrincipal();

        if (!(authentication.getPrincipal() instanceof SecurityUser user)) {
            return;
        }

        JwtClaimsSet.Builder claims = context.getClaims();
        Set<String> scopes = context.getAuthorizedScopes();

        // ===== 通用：sub + tenant =====
        claims.subject(user.getUserId());
        claims.claim(IStandardClaimNamesConstants.TENANT_ID, user.getTenantId());

        // ===== Access Token =====
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {

            // issuer（多租户时合理）
            String issuer = AuthorizationServerContextHolder.getContext().getIssuer();
            claims.issuer(issuer);

            // roles / authorities（仅授权用途）
            if (CollectionUtils.isNotEmpty(user.getRoles())) {
                claims.claim(IStandardClaimNamesConstants.ROLES, user.getRoles());
            }

            if (CollectionUtils.isNotEmpty(user.getAuthorities())) {
                Set<String> authorities = user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                claims.claim(IStandardClaimNamesConstants.AUTHORITIES, authorities);
            }

            return;
        }

        // ===== ID Token（OIDC）=====
        if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {

            // profile scope
            if (scopes.contains(OidcScopes.PROFILE)) {
                putIfNotBlank(claims, StandardClaimNames.PREFERRED_USERNAME, user.getUsername());
                putIfNotBlank(claims, StandardClaimNames.NICKNAME, user.getNickname());
                putIfNotBlank(claims, StandardClaimNames.NAME, user.getNickname());
                putIfNotBlank(claims, StandardClaimNames.PICTURE, user.getAvatar());
            }

            // email scope
            if (scopes.contains(OidcScopes.EMAIL) && StringUtils.isNotBlank(user.getEmail())) {
                claims.claim(StandardClaimNames.EMAIL, user.getEmail());
                claims.claim(StandardClaimNames.EMAIL_VERIFIED, true);
            }

            // phone scope
            if (scopes.contains(OidcScopes.PHONE) && StringUtils.isNotBlank(user.getPhoneNumber())) {
                claims.claim(StandardClaimNames.PHONE_NUMBER, user.getPhoneNumber());
                claims.claim(StandardClaimNames.PHONE_NUMBER_VERIFIED, true);
            }
        }
    }

    private void putIfNotBlank(JwtClaimsSet.Builder claims, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            claims.claim(name, value);
        }
    }
}