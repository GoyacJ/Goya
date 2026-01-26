package com.ysmjjsy.goya.component.framework.servlet.secure;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.web.annotation.AccessLimited;
import com.ysmjjsy.goya.component.web.cache.AccessLimitedCacheManager;
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
 * <p>访问防刷拦截器</p>
 *
 * @author goya
 * @since 2025/10/9 16:20
 */
@Slf4j
@RequiredArgsConstructor
public class AccessLimitedInterceptor extends AbstractHandlerInterceptor {

    private final AccessLimitedCacheManager accessLimitedCacheManager;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        log.trace("[Goya] |- AccessLimitedInterceptor preHandle postProcess.");

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();

        AccessLimited accessLimited = method.getAnnotation(AccessLimited.class);
        if (ObjectUtils.isNotEmpty(accessLimited)) {
            String key = generateRequestKey(request);
            return handle(key, accessLimited, request.getRequestURI());
        }

        return true;
    }

    public boolean handle(String key, AccessLimited accessLimited, String url) {

        if (StringUtils.isNotBlank(key)) {

            int maxTimes = accessLimitedCacheManager.getAccessLimited().maxTimes();
            Duration expireDuration = Duration.ZERO;

            int annotationMaxTimes = accessLimited.maxTimes();
            if (annotationMaxTimes != 0) {
                maxTimes = annotationMaxTimes;
            }

            String annotationDuration = accessLimited.duration();
            if (StringUtils.isNotBlank(annotationDuration)) {
                try {
                    expireDuration = Duration.parse(annotationDuration);
                } catch (DateTimeParseException e) {
                    log.warn("[Goya] |- AccessLimited duration value is incorrect, on api [{}].", url);
                }
            }

            String expireKey = key + "_expire";
            Long times = accessLimitedCacheManager.get(key);

            if (ObjectUtils.isEmpty(times) || times == 0L) {
                if (!expireDuration.isZero()) {
                    // 如果注解上配置了Duration且没有配置错可以正常解析，那么使用注解上的配置值
                    accessLimitedCacheManager.put(key, expireDuration);
                    accessLimitedCacheManager.put(expireKey, System.currentTimeMillis(), expireDuration);
                } else {
                    // 如果注解上没有配置Duration或者配置错无法正常解析，那么使用StampProperties的配置值
                    accessLimitedCacheManager.put(key);
                    accessLimitedCacheManager.put(expireKey, System.currentTimeMillis());
                }
                return true;
            } else {
                log.debug("[Goya] |- AccessLimited request [{}] times.", times);

                if (times <= maxTimes) {
                    Duration newDuration = accessLimitedCacheManager.calculateRemainingTime(expireDuration, expireKey);
                    // 不管是注解上配置Duration值还是StampProperties中配置的Duration值，是不会变的
                    // 所以第一次存入expireKey对应的System.currentTimeMillis()时间后，这个值也不应该变化。
                    // 因此，这里只更新访问次数的标记值
                    accessLimitedCacheManager.put(key, times + 1L, newDuration);
                    return true;
                } else {
                    throw new CommonException("Requests are too frequent. Please try again later!");
                }
            }
        }

        return true;
    }
}
