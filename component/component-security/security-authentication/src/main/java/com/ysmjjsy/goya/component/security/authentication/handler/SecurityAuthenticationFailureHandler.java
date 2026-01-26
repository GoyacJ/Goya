package com.ysmjjsy.goya.component.security.authentication.handler;

import com.ysmjjsy.goya.component.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.component.security.core.exception.SecurityExceptionHandler;
import com.ysmjjsy.goya.component.web.template.AbstractResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/22 23:09
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityAuthenticationFailureHandler extends AbstractResponseHandler implements AuthenticationFailureHandler {

    private final HttpMessageConverter<OAuth2Error> errorHttpMessageConverter = new OAuth2ErrorHttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        MultiValueMap<String, String> parameters = SecurityRequestUtils.getParameters(request);
        String deviceCode = parameters.getFirst(OAuth2ParameterNames.DEVICE_CODE);
        // 兼容 Device Grant 错误处理
        // Device Grant 需要 SAS 原始出错信息，如果采用原有 SecurityGlobalExceptionHandler 处理方式，将导致前端获取到错误的错误信息
        if (exception instanceof OAuth2AuthenticationException oauth2Exception && StringUtils.isNotBlank(deviceCode)) {
            OAuth2Error error = oauth2Exception.getError();
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
            this.errorHttpMessageConverter.write(error, MediaType.APPLICATION_JSON, httpResponse);
        } else {
            process(request, response, () -> SecurityExceptionHandler.handleAuthenticationException(exception, request));
        }
    }
}
