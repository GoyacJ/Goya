package com.ysmjjsy.goya.component.security.authentication.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/2 00:10
 */
@UtilityClass
public class SecurityRequestUtils {

    public static final String ACCESS_TOKEN_REQUEST_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    public static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            for (String value : values) {
                parameters.add(key, value);
            }
        });
        return parameters;
    }

    public static void throwError(String errorCode, String description) {
        OAuth2Error error = new OAuth2Error(errorCode, description, ACCESS_TOKEN_REQUEST_ERROR_URI);
        throw new OAuth2AuthenticationException(error);
    }

    public static void throwParameterError(String errorCode, String parameterName) {
        throwParameterError(errorCode, parameterName, ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    public static void throwParameterError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        throw new OAuth2AuthenticationException(error);
    }

    private static boolean checkRequired(MultiValueMap<String, String> parameters, String parameterName, String parameterValue) {
        return !StringUtils.hasText(parameterValue) || parameters.get(parameterName).size() != 1;
    }

    private static boolean checkOptional(MultiValueMap<String, String> parameters, String parameterName, String parameterValue) {
        return StringUtils.hasText(parameterValue) && parameters.get(parameterName).size() != 1;
    }

    public static String checkParameter(MultiValueMap<String, String> parameters, String parameterName, boolean isRequired, String errorCode, String errorUri) {
        String value = parameters.getFirst(parameterName);
        if (isRequired) {
            if (checkRequired(parameters, parameterName, value)) {
                throwParameterError(errorCode, parameterName, errorUri);
            }
        } else {
            if (checkOptional(parameters, parameterName, value)) {
                throwParameterError(errorCode, parameterName, errorUri);
            }
        }

        return value;
    }

    public static String checkRequiredParameter(MultiValueMap<String, String> parameters, String parameterName, String errorCode, String errorUri) {
        return checkParameter(parameters, parameterName, true, errorCode, errorUri);
    }

    public static String checkRequiredParameter(MultiValueMap<String, String> parameters, String parameterName, String errorCode) {
        return checkRequiredParameter(parameters, parameterName, errorCode, ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    public static String checkRequiredParameter(MultiValueMap<String, String> parameters, String parameterName) {
        return checkRequiredParameter(parameters, parameterName, OAuth2ErrorCodes.INVALID_REQUEST);
    }

    public static String checkOptionalParameter(MultiValueMap<String, String> parameters, String parameterName, String errorCode, String errorUri) {
        return checkParameter(parameters, parameterName, false, errorCode, errorUri);
    }

    public static String checkOptionalParameter(MultiValueMap<String, String> parameters, String parameterName, String errorCode) {
        return checkOptionalParameter(parameters, parameterName, errorCode, ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    public static String checkOptionalParameter(MultiValueMap<String, String> parameters, String parameterName) {
        return checkOptionalParameter(parameters, parameterName, OAuth2ErrorCodes.INVALID_REQUEST);
    }

    public static void validateAndAddDPoPParametersIfAvailable(HttpServletRequest request,
                                                               Map<String, Object> additionalParameters) {
        final String dPoPProofHeaderName = OAuth2AccessToken.TokenType.DPOP.getValue();
        String dPoPProof = request.getHeader(dPoPProofHeaderName);
        if (StringUtils.hasText(dPoPProof)) {
            if (Collections.list(request.getHeaders(dPoPProofHeaderName)).size() != 1) {
                throwParameterError(OAuth2ErrorCodes.INVALID_REQUEST, dPoPProofHeaderName, ACCESS_TOKEN_REQUEST_ERROR_URI);
            } else {
                additionalParameters.put("dpop_proof", dPoPProof);
                additionalParameters.put("dpop_method", request.getMethod());
                additionalParameters.put("dpop_target_uri", request.getRequestURL().toString());
            }
        }
    }

    public static void checkPostRequest(@NonNull HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST,"unSupport other request method");
        }
    }
}
