package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>异常构建器，用于更便捷地创建带 metadata/args 的 {@link GoyaException} 及其子类。</p>
 * <p>该类用于减少业务侧构造异常的样板代码，提升一致性。</p>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * throw Exceptions.biz(UserErrorCode.USER_STATUS_INVALID)
 *     .userMessage("用户状态不允许执行该操作")
 *     .meta("userId", userId)
 *     .meta("status", status)
 *     .build();
 * }</pre>
 *
 * @author goya
 * @since 2026/1/24 13:28
 */
public final class Exceptions {

    private Exceptions() {
        // 工具类不允许实例化
    }

    /**
     * 创建业务异常构建器。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder biz(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.BIZ);
    }

    /**
     * 创建校验异常构建器。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder validation(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.VALIDATION);
    }

    /**
     * 创建远程异常构建器。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder remote(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.REMOTE);
    }

    /**
     * 创建基础设施异常构建器。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder infra(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.INFRA);
    }

    /**
     * 创建鉴权异常构建器。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder auth(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.AUTH);
    }

    /**
     * 创建通用异常构建器（返回 {@link GoyaException}）。
     *
     * @param errorCode 错误码（不能为空）
     * @return 构建器
     */
    public static Builder system(ErrorCode errorCode) {
        return new Builder(errorCode, Kind.SYSTEM);
    }

    /**
     * 构建器内部类型枚举。
     */
    private enum Kind {
        BIZ, VALIDATION, REMOTE, INFRA, AUTH, SYSTEM
    }

    /**
     * 异常构建器实现。
     *
     * <p>构建器不是线程安全的，请勿跨线程复用。</p>
     */
    public static final class Builder {

        private final ErrorCode errorCode;
        private final Kind kind;

        private String userMessage;
        private String debugMessage;
        private Object[] args;
        private final Map<String, Object> metadata = new HashMap<>();
        private Throwable cause;

        private Builder(ErrorCode errorCode, Kind kind) {
            this(errorCode, kind, null, null);
        }

        private Builder(ErrorCode errorCode, Kind kind, String userMessage) {
            this(errorCode, kind, userMessage, null);
        }

        private Builder(ErrorCode errorCode, Kind kind, Throwable cause) {
            this(errorCode, kind, null, cause);
        }

        private Builder(ErrorCode errorCode, Kind kind, String userMessage, Throwable cause) {
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode 不能为空");
            this.kind = kind;
            this.cause = cause;
            this.userMessage = userMessage;
        }

        /**
         * 设置对外安全文案。
         *
         * @param userMessage 对外文案（可为空）
         * @return 当前构建器
         */
        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        /**
         * 设置诊断文案（建议仅写日志）。
         *
         * @param debugMessage 诊断文案（可为空）
         * @return 当前构建器
         */
        public Builder debugMessage(String debugMessage) {
            this.debugMessage = debugMessage;
            return this;
        }

        /**
         * 设置消息参数（用于 i18n/模板格式化）。
         *
         * @param args 参数数组（可为空）
         * @return 当前构建器
         */
        public Builder args(Object... args) {
            this.args = args;
            return this;
        }

        /**
         * 添加结构化上下文键值对。
         *
         * @param key   键（不能为空）
         * @param value 值（可为空）
         * @return 当前构建器
         */
        public Builder meta(String key, Object value) {
            Objects.requireNonNull(key, "metadata key 不能为空");
            this.metadata.put(key, value);
            return this;
        }

        /**
         * 设置原因异常。
         *
         * @param cause 原因异常（可为空）
         * @return 当前构建器
         */
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * 构建异常对象。
         *
         * @return 异常对象（非空）
         */
        public GoyaException build() {
            Map<String, Object> meta = metadata.isEmpty() ? null : Map.copyOf(metadata);
            return switch (kind) {
                case BIZ -> new BizException(errorCode, userMessage, meta);
                case VALIDATION -> new ValidationException(errorCode, userMessage, args, meta);
                case REMOTE -> new RemoteException(errorCode, userMessage, debugMessage, meta, cause);
                case INFRA -> new InfraException(errorCode, userMessage, debugMessage, meta, cause);
                case AUTH -> new AuthException(errorCode, userMessage, meta);
                case SYSTEM -> new GoyaException(errorCode, userMessage, debugMessage, args, meta, cause);
            };
        }
    }
}