package com.ysmjjsy.goya.component.web.advice;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import com.ysmjjsy.goya.component.web.annotation.Crypto;
import com.ysmjjsy.goya.component.web.processor.HttpCryptoProcessor;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <p>响应体加密Advice</p>
 *
 * @author goya
 * @since 2025/10/9 16:29
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final HttpCryptoProcessor httpCryptoProcessor;

    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        String methodName = methodParameter.getMethod().getName();
        Crypto crypto = methodParameter.getMethodAnnotation(Crypto.class);

        boolean isSupports = ObjectUtils.isNotEmpty(crypto) && crypto.responseEncrypt();

        log.trace("[GOYA] |- Is EncryptResponseBodyAdvice supports method [{}] ? Status is [{}].", methodName, isSupports);
        return isSupports;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter methodParameter, @NonNull MediaType selectedContentType, @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        String requestId = WebUtils.getRequestId(request);
        if (WebUtils.isCryptoEnabled(request, requestId)) {

            log.info("[GOYA] |- EncryptResponseBodyAdvice begin encrypt data.");

            String methodName = methodParameter.getMethod().getName();
            String className = methodParameter.getDeclaringClass().getName();

            try {
                String bodyString = JsonUtils.getJsonMapper().writeValueAsString(body);
                String result = httpCryptoProcessor.encrypt(requestId, bodyString);
                if (StringUtils.isNotBlank(result)) {
                    log.debug("[GOYA] |- Encrypt response body for rest method [{}] in [{}] finished.", methodName, className);
                    return result;
                } else {
                    return body;
                }
            } catch (CommonException e) {
                log.error("[GOYA] |- Session is expired for encrypt response body for rest method [{}] in [{}], skip encrypt operation.", methodName, className, e);
                return body;
            }
        } else {
            log.warn("[GOYA] |- Cannot find Goya Cloud custom session header. Use interface crypto function need add X-Goya-Request-ids to request header.");
            return body;
        }
    }
}