package com.ysmjjsy.goya.security.authentication.provider;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.common.utils.StreamUtils;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * <p>抽象的认证 Converter</p>
 *
 * @author goya
 * @since 2026/1/2 00:12
 */
public abstract class AbstractAuthenticationConverter implements AuthenticationConverter {

    private final CryptoProcessor cryptoProcessor;

    protected AbstractAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        this.cryptoProcessor = cryptoProcessor;
    }

    protected String[] decrypt(HttpServletRequest request, String sessionId, List<String> parameters) {
        if (WebUtils.isCryptoEnabled(request, sessionId) && CollectionUtils.isNotEmpty(parameters)) {
            List<String> result = parameters.stream().map(item -> decrypt(request, sessionId, item)).toList();
            return StreamUtils.toStringArray(result);
        }

        return StreamUtils.toStringArray(parameters);
    }

    protected String decrypt(HttpServletRequest request, String sessionId, String parameter) {
        if (WebUtils.isCryptoEnabled(request, sessionId) && StringUtils.isNotBlank(parameter)) {
            try {
                return cryptoProcessor.decrypt(sessionId, parameter);
            } catch (Exception e) {
                throw new OAuth2AuthenticationException(e.getMessage());
            }
        }
        return parameter;
    }

    protected Authentication getClientPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    protected Map<String, Object> getAdditionalParameters(HttpServletRequest request, MultiValueMap<String, String> parameters) {

        String requestId = WebUtils.getRequestId(request);

        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                    !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, (value.size() == 1) ? decrypt(request, requestId, value.getFirst()) : decrypt(request, requestId, value));
            }
        });

        return additionalParameters;
    }

    protected Set<String> getRequestedScopes(String scope) {

        Set<String> requestedScopes = null;
        if (org.springframework.util.StringUtils.hasText(scope)) {
            requestedScopes = new HashSet<>(
                    Arrays.asList(org.springframework.util.StringUtils.delimitedListToStringArray(scope, " ")));
        }

        return requestedScopes;
    }
}
