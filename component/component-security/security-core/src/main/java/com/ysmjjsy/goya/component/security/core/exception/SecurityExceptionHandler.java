package com.ysmjjsy.goya.component.security.core.exception;

import com.ysmjjsy.goya.component.core.exception.AbstractRuntimeException;
import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;
import com.ysmjjsy.goya.component.web.exception.GlobalExceptionHandler;
import com.ysmjjsy.goya.component.web.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/22 23:11
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    public static Response<Void> handleAuthenticationException(AuthenticationException authenticationException, HttpServletRequest request) {
        Throwable throwable = authenticationException.getCause();
        if (ObjectUtils.isNotEmpty(throwable)
                && AbstractRuntimeException.class.isAssignableFrom(throwable.getClass())) {
            AbstractRuntimeException abstractRuntimeException = (AbstractRuntimeException) throwable;
            ErrorCode errorCode = abstractRuntimeException.getErrorCode();
            String path = request.getRequestURI();
            return Response.<Void>builder().error((HttpErrorCode) errorCode).path(path).build();
        }

        return GlobalExceptionHandler.buildErrorResponse(authenticationException, request, HttpErrorCodeEnum.UNAUTHORIZED);
    }
}
