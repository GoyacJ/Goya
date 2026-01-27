package com.ysmjjsy.goya.component.framework.core.api;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.pojo.IResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>统一 API 响应体</p>
 * <p>该响应体属于 Web 适配层输出模型（HTTP/JSON）。</p>
 *
 * @param success     是否成功
 * @param code        稳定错误码/成功码（成功时固定 OK）
 * @param message     对外安全文案（成功可为空，失败建议有值）
 * @param traceId     链路追踪标识（可为空）
 * @param timestamp   响应时间（UTC）
 * @param data        成功数据（失败时通常为空）
 * @param path        请求地址
 * @param meta        扩展信息（非空，默认空 Map）
 * @param fieldErrors 字段级错误（非空，默认空 List）
 * @param <T>         数据类型
 * @author goya
 * @since 2026/1/24 13:49
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        String traceId,
        LocalDateTime timestamp,
        T data,
        String path,
        Map<String, Object> meta,
        List<ApiFieldError> fieldErrors
) implements IResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页 meta 的标准键名（保留字段）。
     *
     * <p>建议所有分页接口使用该键名承载 {@link PageMeta}，避免业务侧各自定义导致不统一。</p>
     */
    public static final String META_PAGE = "page";

    /**
     * 规范化构造：补齐 timestamp/meta/fieldErrors，并保证不可变。
     *
     * <p>同时强制成功码固定 OK。</p>
     */
    public ApiResponse {
        Objects.requireNonNull(code, "code 不能为空");

        // 成功码强约束
        if (success && !CommonErrorCode.OK.code().equals(code)) {
            throw new IllegalArgumentException("成功响应 code 必须固定为 CommonErrorCode.OK");
        }

        // 时间戳兜底
        timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();

        // meta / fieldErrors 兜底与不可变
        meta = (meta == null) ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(meta));
        fieldErrors = (fieldErrors == null) ? List.of() : List.copyOf(fieldErrors);
    }

    /**
     * 返回一个携带 traceId 的新响应（record 不可变）。
     *
     * <p>若入参 traceId 为空则返回当前对象。</p>
     *
     * @param traceId traceId
     * @return 新 ApiResponse
     */
    public ApiResponse<T> withTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return this;
        }
        if (this.traceId != null && !this.traceId.isBlank()) {
            // 已存在则不覆盖
            return this;
        }
        return new ApiResponse<>(
                this.success,
                this.code,
                this.message,
                traceId,
                this.timestamp,
                this.data,
                this.path,
                this.meta,
                this.fieldErrors
        );
    }

    /**
     * 返回一个替换 data 的新响应（record 不可变）。
     *
     * @param newData 新数据
     * @return 新 ApiResponse
     */
    @SuppressWarnings("unchecked")
    public ApiResponse<T> withData(Object newData) {
        return new ApiResponse<>(this.success,
                this.code,
                this.message,
                this.traceId,
                this.timestamp,
                (T) newData,
                this.path,
                this.meta,
                this.fieldErrors);
    }

    // =========================
    //  一、最常用静态工厂
    // =========================

    /**
     * 创建成功响应（无数据）。
     *
     * @return ApiResponse<Void>
     */
    public static ApiResponse<Void> ok() {
        return ok(null, null, null);
    }

    /**
     * 创建成功响应（带数据）。
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, null, null);
    }

    /**
     * 创建成功响应（带数据与 message）。
     *
     * @param data    数据
     * @param message 成功文案（可为空）
     * @param <T>     数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ok(data, message, null);
    }

    /**
     * 创建成功响应（带数据、message、traceId）。
     *
     * @param data    数据
     * @param message 成功文案（可为空）
     * @param traceId traceId（可为空）
     * @param <T>     数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> ok(T data, String message, String traceId) {
        return new ApiResponse<>(
                true,
                CommonErrorCode.OK.code(),
                message,
                traceId,
                LocalDateTime.now(),
                data,
                getCurrentRequestPath(),
                Map.of(),
                List.of()
        );
    }

    /**
     * 创建分页成功响应（标准化写入 meta.page）。
     *
     * @param data     数据列表（可为空）
     * @param pageMeta 分页元信息（不能为空）
     * @param <T>      数据类型
     * @return ApiResponse<List<T>>
     */
    public static <T> ApiResponse<List<T>> okPage(List<T> data, PageMeta pageMeta) {
        return okPage(data, pageMeta, null, null);
    }

    /**
     * 创建分页成功响应（标准化写入 meta.page），支持 message 与 traceId。
     *
     * @param data     数据列表（可为空）
     * @param pageMeta 分页元信息（不能为空）
     * @param message  成功文案（可为空）
     * @param traceId  traceId（可为空）
     * @param <T>      数据类型
     * @return ApiResponse<List<T>>
     */
    public static <T> ApiResponse<List<T>> okPage(List<T> data, PageMeta pageMeta, String message, String traceId) {
        Objects.requireNonNull(pageMeta, "pageMeta 不能为空");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(META_PAGE, pageMeta);

        return new ApiResponse<>(
                true,
                CommonErrorCode.OK.code(),
                message,
                traceId,
                LocalDateTime.now(),
                data,
                getCurrentRequestPath(),
                meta,
                List.of()
        );
    }

    /**
     * 创建失败响应（最简）。
     *
     * @param errorCode 错误码（不能为空）
     * @return ApiResponse<Void>
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return fail(errorCode, null, null);
    }

    /**
     * 创建失败响应（带对外 message）。
     *
     * @param errorCode 错误码（不能为空）
     * @param message   对外安全文案（可为空；为空时建议由上层解析器补齐）
     * @return ApiResponse<Void>
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return fail(errorCode, message, null);
    }

    /**
     * 创建失败响应（带对外 message 与 traceId）。
     *
     * @param errorCode 错误码（不能为空）
     * @param message   对外安全文案（可为空）
     * @param traceId   traceId（可为空）
     * @return ApiResponse<Void>
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode, String message, String traceId) {
        Objects.requireNonNull(errorCode, "errorCode 不能为空");
        return new ApiResponse<>(
                false,
                errorCode.code(),
                message,
                traceId,
                LocalDateTime.now(),
                null,
                getCurrentRequestPath(),
                Map.of(),
                List.of()
        );
    }

    public Map<String, Object> toErrorModel() {
        Map<String, Object> result = createModel();
        result.put("fieldErrors", fieldErrors());
        return result;
    }

    private Map<String, Object> createModel() {
        Map<String, Object> result = HashMap.newHashMap(8);
        result.put("code", code());
        result.put("message", message());
        result.put("path", path());
        result.put("timestamp", timestamp());
        result.put("traceId", traceId());
        result.put("meta", meta());
        return result;
    }

    // =========================
    //  二、Builder（覆盖真实业务场景）
    // =========================

    /**
     * 成功响应构建器入口。
     *
     * <p>示例：</p>
     * <pre>{@code
     * return ApiResponse.okBuilder()
     *     .message("查询成功")
     *     .meta("total", 100)
     *     .data(list);
     * }</pre>
     *
     * @return OkBuilder
     */
    public static OkBuilder okBuilder() {
        return new OkBuilder();
    }

    /**
     * 失败响应构建器入口。
     *
     * <p>示例：</p>
     * <pre>{@code
     * return ApiResponse.failBuilder(CommonErrorCode.INVALID_PARAM)
     *     .message("参数不合法")
     *     .fieldError("name", "不能为空")
     *     .build();
     * }</pre>
     *
     * @param errorCode 错误码（不能为空）
     * @return FailBuilder
     */
    public static FailBuilder failBuilder(ErrorCode errorCode) {
        return new FailBuilder(errorCode);
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    /**
     * 成功响应构建器（链式）。
     *
     * <p>该构建器通过最后一步 {@link #data(Object)} 决定响应泛型类型。</p>
     */
    public static final class OkBuilder {

        private String message;
        private String traceId;
        private final Map<String, Object> meta = new LinkedHashMap<>();

        private OkBuilder() {
        }

        /**
         * 设置成功文案。
         *
         * @param message 成功文案
         * @return this
         */
        public OkBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置 traceId。
         *
         * @param traceId traceId
         * @return this
         */
        public OkBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * 添加 meta 信息（用于分页/统计/扩展）。
         *
         * @param key   key（不能为空）
         * @param value value（可为空）
         * @return this
         */
        public OkBuilder meta(String key, Object value) {
            Objects.requireNonNull(key, "meta key 不能为空");
            this.meta.put(key, value);
            return this;
        }

        /**
         * 批量设置 meta。
         *
         * @param meta meta（可为空）
         * @return this
         */
        public OkBuilder meta(Map<String, Object> meta) {
            if (meta != null) {
                this.meta.putAll(meta);
            }
            return this;
        }

        /**
         * 构建成功响应（带数据）。
         *
         * @param data 数据
         * @param <T>  数据类型
         * @return ApiResponse<T>
         */
        public <T> ApiResponse<T> data(T data) {
            return new ApiResponse<>(
                    true,
                    CommonErrorCode.OK.code(),
                    message,
                    traceId,
                    LocalDateTime.now(),
                    data,
                    getCurrentRequestPath(),
                    meta,
                    List.of()
            );
        }

        /**
         * 构建成功响应（无数据）。
         *
         * @return ApiResponse<Void>
         */
        public ApiResponse<Void> build() {
            return data(null);
        }
    }

    /**
     * 失败响应构建器（链式）。
     */
    public static final class FailBuilder {

        private final ErrorCode errorCode;
        private String message;
        private String traceId;
        private final Map<String, Object> meta = new LinkedHashMap<>();
        private final List<ApiFieldError> fieldErrors = new ArrayList<>();

        private FailBuilder(ErrorCode errorCode) {
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode 不能为空");
        }

        /**
         * 设置对外安全文案。
         *
         * @param message 文案
         * @return this
         */
        public FailBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置 traceId。
         *
         * @param traceId traceId
         * @return this
         */
        public FailBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * 添加 meta 信息（上下文）。
         *
         * @param key   key（不能为空）
         * @param value value（可为空）
         * @return this
         */
        public FailBuilder meta(String key, Object value) {
            Objects.requireNonNull(key, "meta key 不能为空");
            this.meta.put(key, value);
            return this;
        }

        /**
         * 批量设置 meta。
         *
         * @param meta meta（可为空）
         * @return this
         */
        public FailBuilder meta(Map<String, Object> meta) {
            if (meta != null) {
                this.meta.putAll(meta);
            }
            return this;
        }

        /**
         * 添加字段错误（不带 rejectedValue）。
         *
         * @param field   字段名
         * @param message 错误信息
         * @return this
         */
        public FailBuilder fieldError(String field, String message) {
            this.fieldErrors.add(ApiFieldError.of(field, message));
            return this;
        }

        /**
         * 添加字段错误（带 rejectedValue）。
         *
         * <p>注意：rejectedValue 建议脱敏后再对外输出。</p>
         *
         * @param field         字段名
         * @param message       错误信息
         * @param rejectedValue 被拒绝值
         * @return this
         */
        public FailBuilder fieldError(String field, String message, Object rejectedValue) {
            this.fieldErrors.add(ApiFieldError.of(field, message, rejectedValue));
            return this;
        }

        /**
         * 批量添加字段错误。
         *
         * @param errors 错误列表（可为空）
         * @return this
         */
        public FailBuilder fieldErrors(List<ApiFieldError> errors) {
            if (errors != null) {
                this.fieldErrors.addAll(errors);
            }
            return this;
        }

        /**
         * 构建失败响应。
         *
         * @return ApiResponse<Void>
         */
        public ApiResponse<Void> build() {
            return new ApiResponse<>(
                    false,
                    errorCode.code(),
                    message,
                    traceId,
                    LocalDateTime.now(),
                    null,
                    getCurrentRequestPath(),
                    meta,
                    fieldErrors
            );
        }
    }

    private static String getCurrentRequestPath() {
        try {
            return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                    .map(attr -> ((ServletRequestAttributes) attr).getRequest().getRequestURI())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}