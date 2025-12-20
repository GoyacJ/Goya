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
 * 统一响应返回实体
 * <p>
 * 提供统一的 API 响应格式，包含以下字段：
 * <ul>
 *   <li>code: 业务状态码</li>
 *   <li>message: 响应消息（支持国际化）</li>
 *   <li>isSuccess: 是否成功</li>
 *   <li>timestamp: 响应时间戳</li>
 *   <li>data: 响应数据</li>
 *   <li>traceId: 追踪ID（用于分布式追踪）</li>
 *   <li>path: 请求路径</li>
 *   <li>error: 错误详情（仅错误时返回）</li>
 *   <li>httpStatus: HTTP 状态码</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 成功响应
 * return Response.success(user);
 *
 * // 失败响应
 * return Response.builder()
 *     .code(ResponseCodeEnum.NOT_FOUND)
 *     .build()
 *     .toResponseEntity();
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20 22:08
 * @see IResponse
 * @see IResponseCode
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

    /**
     * 创建成功响应（无数据）
     */
    public static Response<Void> success() {
        return Response.<Void>builder().success(null).build();
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <D>  数据类型
     * @return 成功响应
     */
    public static <D> Response<D> success(D data) {
        return Response.<D>builder().success(data).build();
    }

    /**
     * 创建成功响应（带自定义消息和数据）
     * <p>
     * 注意：自定义消息不会进行国际化处理，如需国际化请使用 code 方式
     *
     * @param msg  自定义消息
     * @param data 响应数据
     * @param <D>  数据类型
     * @return 成功响应
     */
    public static <D> Response<D> success(String msg, D data) {
        return Response.<D>builder().success(msg, data).build();
    }

    /**
     * 创建失败响应（使用默认错误码）
     */
    public static Response<Void> failure() {
        return Response.<Void>builder().error().build();
    }

    /**
     * 创建失败响应（带自定义消息）
     * <p>
     * 注意：自定义消息不会进行国际化处理，如需国际化请使用 code 方式
     *
     * @param msg 自定义错误消息
     * @return 失败响应
     */
    public static Response<Void> failure(String msg) {
        return Response.<Void>builder().error(msg).build();
    }

    // -------------------- ResponseEntity --------------------

    /**
     * 转换为 ResponseEntity
     */
    public ResponseEntity<Response<D>> toResponseEntity() {
        return ResponseEntity
                .status(ObjectUtils.getIfNull(httpStatus, HttpStatus.OK))
                .body(this);
    }

    /**
     * 转换为 ResponseEntity（带自定义响应头）
     *
     * @param headers 响应头
     * @return ResponseEntity
     */
    public ResponseEntity<Response<D>> toResponseEntity(Map<String, String> headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity
                .status(ObjectUtils.getIfNull(httpStatus, HttpStatus.OK));
        if (headers != null) headers.forEach(builder::header);
        return builder.body(this);
    }

    /**
     * 转换为错误模型（用于错误页面展示）
     *
     * @return 错误模型 Map
     */
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

    /**
     * 创建 Builder 实例
     *
     * @param <D> 数据类型
     * @return Builder 实例
     */
    public static <D> Builder<D> builder() {
        return new Builder<>();
    }

    /**
     * Response Builder
     * <p>
     * 使用流式 API 构建 Response 对象
     */
    public static class Builder<D> {
        private IResponseCode code;
        private String message;
        private D data;
        private String traceId;
        private String path;
        private ErrorDetail error;
        private HttpStatus httpStatus;

        // ---------------- fluent methods ----------------

        /**
         * 设置响应码（会自动设置国际化消息和 HTTP 状态码）
         *
         * @param code 响应码
         * @return Builder
         */
        public Builder<D> code(IResponseCode code) {
            this.code = code;
            // 使用国际化消息
            this.message = getI18nMessage(code);
            this.httpStatus = code.getStatus();
            return this;
        }

        /**
         * 设置自定义消息（不会进行国际化处理）
         * <p>
         * 注意：如果已设置 code，此方法会覆盖 code 的国际化消息
         *
         * @param message 自定义消息
         * @return Builder
         */
        public Builder<D> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置响应数据
         *
         * @param data 响应数据
         * @return Builder
         */
        public Builder<D> data(D data) {
            this.data = data;
            return this;
        }

        /**
         * 设置追踪ID
         *
         * @param traceId 追踪ID
         * @return Builder
         */
        public Builder<D> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * 设置请求路径
         *
         * @param path 请求路径
         * @return Builder
         */
        public Builder<D> path(String path) {
            this.path = path;
            return this;
        }

        /**
         * 设置错误详情
         *
         * @param error 错误详情
         * @return Builder
         */
        public Builder<D> error(ErrorDetail error) {
            this.error = error;
            return this;
        }

        /**
         * 设置 HTTP 状态码
         *
         * @param httpStatus HTTP 状态码
         * @return Builder
         */
        public Builder<D> httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        // ---------------- success / error helpers ----------------

        /**
         * 构建成功响应
         *
         * @param data 响应数据
         * @return Builder
         */
        public Builder<D> success(D data) {
            this.code = ResponseCodeEnum.OK;
            // 使用国际化消息
            this.message = getI18nMessage(ResponseCodeEnum.OK);
            this.data = data;
            this.httpStatus = HttpStatus.OK;
            return this;
        }

        /**
         * 构建成功响应（带自定义消息）
         *
         * @param msg  自定义消息
         * @param data 响应数据
         * @return Builder
         */
        public Builder<D> success(String msg, D data) {
            this.code = ResponseCodeEnum.OK;
            this.message = msg;
            this.data = data;
            this.httpStatus = HttpStatus.OK;
            return this;
        }

        /**
         * 构建错误响应（使用默认错误码）
         *
         * @return Builder
         */
        public Builder<D> error() {
            this.code = ResponseCodeEnum.INTERNAL_SERVER_ERROR;
            // 使用国际化消息
            this.message = getI18nMessage(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
            // 修复：HTTP 状态码应该与 code 的 status 一致
            this.httpStatus = ResponseCodeEnum.INTERNAL_SERVER_ERROR.getStatus();
            return this;
        }

        /**
         * 构建错误响应（带自定义消息）
         *
         * @param msg 自定义错误消息
         * @return Builder
         */
        public Builder<D> error(String msg) {
            this.code = ResponseCodeEnum.INTERNAL_SERVER_ERROR;
            this.message = msg;
            // 修复：HTTP 状态码应该与 code 的 status 一致
            this.httpStatus = ResponseCodeEnum.INTERNAL_SERVER_ERROR.getStatus();
            return this;
        }

        // ---------------- build ----------------

        /**
         * 构建 Response 对象
         *
         * @return Response 对象
         */
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

        /**
         * 获取国际化消息
         * <p>
         * 优先使用 I18nResolver，如果失败则使用 code.getDescription()
         *
         * @param code 响应码
         * @return 国际化消息
         */
        private String getI18nMessage(IResponseCode code) {
            if (code == null) {
                return null;
            }
            try {
                return I18nResolver.resolveEnum(code);
            } catch (Exception e) {
                // 如果国际化失败，使用默认描述
                return code.getDescription();
            }
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