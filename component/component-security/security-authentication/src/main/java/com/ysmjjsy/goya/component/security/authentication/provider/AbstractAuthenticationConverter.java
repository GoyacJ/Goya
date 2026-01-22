package com.ysmjjsy.goya.component.security.authentication.provider;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>抽象的认证 Converter</p>
 *
 * @author goya
 * @since 2026/1/2 00:12
 */
@Slf4j
public abstract class AbstractAuthenticationConverter implements AuthenticationConverter {

    private final CryptoProcessor cryptoProcessor;

    protected AbstractAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        this.cryptoProcessor = cryptoProcessor;
    }

    @Override
    @NullMarked
    public final @Nullable Authentication convert(HttpServletRequest request) {
        SecurityRequestUtils.checkPostRequest(request);

        MultiValueMap<String, String> parameters =
                SecurityRequestUtils.getParameters(request);

        Map<String, Object> additionalParameters =
                getAdditionalParameters(request, parameters);

        SecurityRequestUtils.validateAndAddDPoPParametersIfAvailable(
                request, additionalParameters
        );

        return convertInternal(request, parameters, additionalParameters);
    }

    protected abstract Authentication convertInternal(
            HttpServletRequest request,
            MultiValueMap<String, String> parameters,
            Map<String, Object> additionalParameters
    );

    protected Map<String, Object> getAdditionalParameters(
            HttpServletRequest request,
            MultiValueMap<String, String> parameters) {

        String requestId = WebUtils.getRequestId(request);
        Map<String, Object> additional = new HashMap<>();

        parameters.forEach((key, values) -> {
            if (!OAuth2ParameterNames.GRANT_TYPE.equals(key)
                    && !OAuth2ParameterNames.SCOPE.equals(key)) {

                if (values.size() == 1) {
                    additional.put(key,
                            decryptIfNecessary(request, requestId, values.getFirst()));
                } else {
                    additional.put(key,
                            values.stream()
                                    .map(v -> decryptIfNecessary(request, requestId, v))
                                    .toList());
                }
            }
        });

        return additional;
    }

    private String decryptIfNecessary(
            HttpServletRequest request,
            String requestId,
            String value) {

        if (WebUtils.isCryptoEnabled(request, requestId)
                && StringUtils.isNotBlank(value)) {
            try {
                return cryptoProcessor.decrypt(requestId, value);
            } catch (Exception e) {
                SecurityRequestUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, e.getMessage());
            }
        }
        return value;
    }
}
