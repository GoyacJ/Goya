package com.ysmjjsy.goya.component.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.pojo.IResponse;
import com.ysmjjsy.goya.component.common.i18n.I18nResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 22:08
 */

@Schema(name = "统一响应返回实体", description = "接口统一返回的实体定义")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<D>(

        @JsonProperty("code")
        String code,

        @JsonProperty("message")
        String message,

        @JsonProperty("isSuccess")
        boolean isSuccess,

        @JsonFormat(pattern = IBaseConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)
        LocalDateTime timestamp,

        @JsonInclude(JsonInclude.Include.ALWAYS)
        D data,

        String traceId,

        String path,

        ErrorDetail error,

        HttpStatus httpStatus

) implements IResponse {

    // -------------------- 静态入口 --------------------

    public static Response<Void> success() {
        return Response.<Void>builder().success(null).build();
    }

    public static <D> Response<D> success(D data) {
        return Response.<D>builder().success(data).build();
    }

    public static <D> Response<D> success(String msg, D data) {
        return Response.<D>builder().success(msg, data).build();
    }

    public static Response<Void> failure() {
        return Response.<Void>builder().error().build();
    }

    public static Response<Void> failure(String msg) {
        return Response.<Void>builder().error(msg).build();
    }

    // -------------------- ResponseEntity --------------------

    public ResponseEntity<Response<D>> toResponseEntity() {
        return ResponseEntity
                .status(ObjectUtils.getIfNull(httpStatus, HttpStatus.OK))
                .body(this);
    }

    public ResponseEntity<Response<D>> toResponseEntity(Map<String, String> headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity
                .status(ObjectUtils.getIfNull(httpStatus, HttpStatus.OK));
        if (headers != null) headers.forEach(builder::header);
        return builder.body(this);
    }


    public Map<String, Object> toErrorModel() {
        Map<String, Object> result = createModel();
        result.put("field", ObjectUtils.isNotEmpty(error()) ? error().field() : StringUtils.EMPTY);
        result.put("error", ObjectUtils.isNotEmpty(error()) ? error().detail() : StringUtils.EMPTY);
        result.put("stackTrace", ObjectUtils.isNotEmpty(error()) ? error().stackTrace() : StringUtils.EMPTY);
        return result;
    }

    private Map<String, Object> createModel() {
        Map<String, Object> result = HashMap.newHashMap(8);
        result.put("code", code());
        result.put("message", message());
        result.put("path", path());
        result.put("status", httpStatus());
        result.put("timestamp", timestamp());
        result.put("traceId", traceId());
        return result;
    }


    // -------------------- Builder --------------------

    public static <D> Builder<D> builder() {
        return new Builder<>();
    }

    public static class Builder<D> {
        private IResponseCode code;
        private String message;
        private D data;
        private String traceId;
        private String path;
        private ErrorDetail error;
        private HttpStatus httpStatus;

        // ---------------- fluent methods ----------------

        public Builder<D> code(IResponseCode code) {
            this.code = code;
            this.message = I18nResolver.resolveEnum(code);
            this.httpStatus = code.getStatus();
            return this;
        }

        public Builder<D> data(D data) {
            this.data = data;
            return this;
        }

        public Builder<D> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder<D> path(String path) {
            this.path = path;
            return this;
        }

        public Builder<D> error(ErrorDetail error) {
            this.error = error;
            return this;
        }

        // ---------------- success / error helpers ----------------

        public Builder<D> success(D data) {
            this.code = ResponseCodeEnum.OK;
            this.message = ResponseCodeEnum.OK.getDescription();
            this.data = data;
            this.httpStatus = HttpStatus.OK;
            return this;
        }

        public Builder<D> success(String msg, D data) {
            this.code = ResponseCodeEnum.OK;
            this.message = msg;
            this.data = data;
            this.httpStatus = HttpStatus.OK;
            return this;
        }

        public Builder<D> error() {
            this.code = ResponseCodeEnum.INTERNAL_SERVER_ERROR;
            this.message = ResponseCodeEnum.INTERNAL_SERVER_ERROR.getDescription();
            this.httpStatus = HttpStatus.BAD_REQUEST;
            return this;
        }

        public Builder<D> error(String msg) {
            this.code = ResponseCodeEnum.INTERNAL_SERVER_ERROR;
            this.message = msg;
            this.httpStatus = HttpStatus.BAD_REQUEST;
            return this;
        }

        // ---------------- build ----------------

        public Response<D> build() {
            String trace = ObjectUtils.getIfNull(traceId, getCurrentTraceId());
            String reqPath = ObjectUtils.getIfNull(path, getCurrentRequestPath());

            boolean ok = code != null && code == ResponseCodeEnum.OK;

            return new Response<>(
                    code != null ? code.getCode() : null,
                    StringUtils.defaultString(message),
                    ok,
                    LocalDateTime.now(),
                    data,
                    trace,
                    reqPath,
                    error,
                    ObjectUtils.getIfNull(httpStatus, HttpStatus.OK)
            );
        }

        private String getCurrentTraceId() {
            return MDC.get("traceId");
        }

        private String getCurrentRequestPath() {
            try {
                return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                        .map(attr -> ((ServletRequestAttributes) attr).getRequest().getRequestURI())
                        .orElse(null);
            } catch (Exception _) {
                return null;
            }
        }
    }
}
