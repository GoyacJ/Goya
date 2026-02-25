package com.ysmjjsy.goya.component.security.oauth2.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.*;

/**
 * <p>预认证码授权请求转换器</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PreAuthCodeGrantAuthenticationConverter implements AuthenticationConverter {

    private static final String GRANT_TYPE = "grant_type";
    private static final String PRE_AUTH_CODE = "pre_auth_code";
    private static final String SCOPE = "scope";

    private final AuthorizationGrantType preAuthCodeGrantType;

    public PreAuthCodeGrantAuthenticationConverter(AuthorizationGrantType preAuthCodeGrantType) {
        this.preAuthCodeGrantType = preAuthCodeGrantType;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(GRANT_TYPE);
        if (!preAuthCodeGrantType.getValue().equals(grantType)) {
            return null;
        }

        String preAuthCode = request.getParameter(PRE_AUTH_CODE);
        if (StringUtils.isBlank(preAuthCode)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        Set<String> requestedScopes = resolveScopes(request.getParameter(SCOPE));

        Map<String, Object> additionalParameters = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (!GRANT_TYPE.equals(key) && !SCOPE.equals(key)) {
                additionalParameters.put(key, value != null && value.length == 1 ? value[0] : value);
            }
        });

        return new PreAuthCodeGrantAuthenticationToken(
                preAuthCodeGrantType,
                clientPrincipal,
                preAuthCode,
                requestedScopes,
                additionalParameters
        );
    }

    private Set<String> resolveScopes(String rawScope) {
        if (StringUtils.isBlank(rawScope)) {
            return Collections.emptySet();
        }
        String[] split = rawScope.trim().split("\\s+");
        Set<String> scopes = new LinkedHashSet<>();
        for (String scope : split) {
            if (StringUtils.isNotBlank(scope)) {
                scopes.add(scope);
            }
        }
        return scopes;
    }
}
