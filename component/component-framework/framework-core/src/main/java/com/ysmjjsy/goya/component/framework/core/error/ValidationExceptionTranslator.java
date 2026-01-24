package com.ysmjjsy.goya.component.framework.core.error;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Jakarta Validation 异常转换器</p>
 * <p>该类将 {@link ConstraintViolationException} 转换为 {@link ValidationException}，
 * 用于非 Web 场景下统一抛出 Goya 异常体系（例如：定时任务、消息消费、RPC 服务实现）。</p>
 *
 * <p>Web 场景中的 {@code MethodArgumentNotValidException} 等属于 spring-web，
 * 必须在 framework-servlet 处理，不应出现在 core。</p>
 *
 * @author goya
 * @since 2026/1/24 13:46
 */
public class ValidationExceptionTranslator {

    /**
     * 将 ConstraintViolationException 转换为 ValidationException。
     *
     * <p>对外文案默认取第一个 violation 的 message，详细列表放入 metadata，便于日志排障。</p>
     *
     * @param ex ConstraintViolationException（不能为空）
     * @return ValidationException（非空）
     */
    public ValidationException translate(ConstraintViolationException ex) {
        Objects.requireNonNull(ex, "ex 不能为空");

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("violationCount", ex.getConstraintViolations().size());

        String userMessage = CommonErrorCode.INVALID_PARAM.defaultMessage();
        int i = 0;
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            if (i == 0 && v.getMessage() != null && !v.getMessage().isBlank()) {
                userMessage = v.getMessage();
            }
            metadata.put("v" + i, Map.of(
                    "path", String.valueOf(v.getPropertyPath()),
                    "invalidValue", v.getInvalidValue(),
                    "message", v.getMessage()
            ));
            i++;
        }

        return new ValidationException(CommonErrorCode.INVALID_PARAM, userMessage, null, metadata);
    }
}
