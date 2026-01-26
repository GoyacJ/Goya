package com.ysmjjsy.goya.component.framework.servlet.secure;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.web.annotation.Idempotent;
import com.ysmjjsy.goya.component.web.cache.IdempotentCacheManager;
import com.ysmjjsy.goya.component.web.interceptor.AbstractHandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * <p>幂等拦截器</p>
 *
 * @author goya
 * @since 2025/10/9 16:22
 */
@Slf4j
@RequiredArgsConstructor
public class IdempotentInterceptor extends AbstractHandlerInterceptor {

    private final IdempotentCacheManager idempotentCacheManager;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();

        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        if (ObjectUtils.isNotEmpty(idempotent)) {
            String key = generateRequestKey(request);
            return handle(key, idempotent, request.getRequestURI());
        }

        return true;
    }

    public boolean handle(String key, Idempotent idempotent, String url) {
        // 幂等性校验, 根据缓存中是否存在Token进行校验。
        // 如果缓存中没有Token，通过放行, 同时在缓存中存入Token。
        // 如果缓存中有Token，意味着同一个操作反复操作，认为失败则抛出异常, 并通过统一异常处理返回友好提示
        if (StringUtils.isNotBlank(key)) {
            String token = idempotentCacheManager.get(key);
            if (StringUtils.isBlank(token)) {
                Duration configuredDuration = Duration.ZERO;
                String annotationExpire = idempotent.expire();
                if (StringUtils.isNotBlank(annotationExpire)) {
                    try {
                        configuredDuration = Duration.parse(annotationExpire);
                    } catch (DateTimeParseException e) {
                        log.warn("[Goya] |- Idempotent duration value is incorrect, on api [{}].", url);
                    }
                }

                if (!configuredDuration.isZero()) {
                    idempotentCacheManager.put(key, configuredDuration);
                } else {
                    idempotentCacheManager.put(key);
                }

                return true;
            } else {
                throw new CommonException("Don't Repeat Submission");
            }
        }
        return true;
    }
}
