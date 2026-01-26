package com.ysmjjsy.goya.component.framework.masker.response;

import com.ysmjjsy.goya.component.framework.core.api.ApiResponse;
import com.ysmjjsy.goya.component.framework.masker.annotation.Mask;
import com.ysmjjsy.goya.component.framework.masker.autoconfigure.properties.MaskerProperties;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.masker.core.MaskingMode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 23:03
 */
@RequiredArgsConstructor
public class ApiResponseMaskingAdvice implements ResponseBodyAdvice<Object> {

    private final MaskerProperties properties;
    private final Masker masker;

    @Override
    @NullMarked
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    @NullMarked
    public Object beforeBodyWrite(@Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response) {

        if (!properties.enabled()) {
            return body;
        }
        if (!(body instanceof ApiResponse<?> api)) {
            return body;
        }

        if (!isMaskEnabled(returnType, api)) {
            return api;
        }

        Object data = api.data();
        if (data == null) {
            return api;
        }

        if (!isSafeForApiMasking(data)) {
            return api;
        }

        Object masked = masker.mask(data, MaskingMode.API);

        // record 不可变：返回一个新 ApiResponse。要求 ApiResponse 提供 withData(...) 方法（见下方第3节）。
        return api.withData(masked);
    }

    /**
     * 判断是否启用 @Mask。
     *
     * @param returnType 方法返回信息
     * @param api ApiResponse
     * @return 是否启用
     */
    private boolean isMaskEnabled(MethodParameter returnType, ApiResponse<?> api) {
        Method m = returnType.getMethod();
        if (m != null) {
            Mask ann = m.getAnnotation(Mask.class);
            if (ann != null) {
                return ann.value();
            }
        }

        Class<?> declaring = returnType.getDeclaringClass();
        Mask maskAnn = declaring.getAnnotation(Mask.class);
        if (maskAnn != null) {
            return maskAnn.value();
        }

        // 允许 DTO 类型上标注 @Mask
        Object data = api.data();
        if (data != null) {
            Mask ann = data.getClass().getAnnotation(Mask.class);
            if (ann != null) {
                return ann.value();
            }
        }
        return false;
    }

    /**
     * 判断 data 类型是否适合进行 API 脱敏（避免改变结构）。
     *
     * @param data data
     * @return 是否适合
     */
    private boolean isSafeForApiMasking(Object data) {
        Class<?> cls = data.getClass();
        switch (data) {
            case CharSequence _, Map _, Iterable _ -> {
                return true;
            }
            default -> {
            }
        }
        if (cls.isArray()) {
            return true;
        }
        return isRecord(cls);
    }

    /**
     * 判断是否为 record（兼容一些环境下的反射差异）。
     *
     * @param cls class
     * @return 是否 record
     */
    private boolean isRecord(Class<?> cls) {
        try {
            return cls.isRecord();
        } catch (Throwable _) {
            return false;
        }
    }
}
