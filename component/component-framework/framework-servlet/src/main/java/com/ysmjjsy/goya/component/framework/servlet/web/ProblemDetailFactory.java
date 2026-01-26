package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.core.error.ErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.Instant;
import java.util.Objects;

/**
 * <p>ProblemDetail 工厂，用于构建 RFC7807 标准错误响应。</p>
 *
 * <h2>扩展属性（BOTH 模式）</h2>
 * <p>当 responseStyle=BOTH 时，会在 ProblemDetail 中写入：</p>
 * <ul>
 *   <li>code：稳定错误码</li>
 *   <li>success：false</li>
 *   <li>traceId：链路追踪标识</li>
 *   <li>timestamp：UTC 时间</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 14:39
 */
public class ProblemDetailFactory {

    private final ErrorMessageResolver messageResolver;
    private final HttpStatusMapper statusMapper;
    private final ServletErrorProperties props;

    /**
     * 构造工厂。
     *
     * @param messageResolver 消息解析器（来自 framework-core）
     * @param statusMapper 状态码映射器
     * @param props 配置项
     */
    public ProblemDetailFactory(ErrorMessageResolver messageResolver,
                                HttpStatusMapper statusMapper,
                                ServletErrorProperties props) {
        this.messageResolver = Objects.requireNonNull(messageResolver, "messageResolver 不能为空");
        this.statusMapper = Objects.requireNonNull(statusMapper, "statusMapper 不能为空");
        this.props = Objects.requireNonNull(props, "props 不能为空");
    }

    /**
     * 构建 ProblemDetail。
     *
     * @param ex GoyaException
     * @param traceId traceId（可为空）
     * @return ProblemDetail
     */
    public ProblemDetail fromGoyaException(GoyaException ex, String traceId) {
        Objects.requireNonNull(ex, "ex 不能为空");
        ErrorCode code = ex.errorCode();

        HttpStatus status = resolveHttpStatus(code);
        String detail = messageResolver.resolve(ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());

        applyExtensions(pd, code, traceId);
        return pd;
    }

    /**
     * 构建 ProblemDetail（非 GoyaException 的场景）。
     *
     * @param code 错误码
     * @param detail 对外文案
     * @param traceId traceId
     * @return ProblemDetail
     */
    public ProblemDetail fromCode(ErrorCode code, String detail, String traceId) {
        Objects.requireNonNull(code, "code 不能为空");

        HttpStatus status = resolveHttpStatus(code);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());

        applyExtensions(pd, code, traceId);
        return pd;
    }

    /**
     * 处理 BOTH 风格扩展属性。
     *
     * @param pd ProblemDetail
     * @param code 错误码
     * @param traceId traceId
     */
    private void applyExtensions(ProblemDetail pd, ErrorCode code, String traceId) {
        if (props.responseStyle() == ServletErrorProperties.ResponseStyle.BOTH) {
            pd.setProperty("code", code.code());
            pd.setProperty("success", false);
            pd.setProperty("traceId", traceId);
            pd.setProperty("timestamp", Instant.now().toString());
        }
    }

    /**
     * 在 AUTH 分类下对 401/403 做细分处理（约定 CommonErrorCode.UNAUTHORIZED/FORBIDDEN）。
     *
     * <p>为避免 servlet 模块强依赖 common 的枚举比较，这里仅在 handler 层做特判更合适。
     * 本工厂仅做分类映射。</p>
     *
     * @param code 错误码
     * @return HttpStatus
     */
    private HttpStatus resolveHttpStatus(ErrorCode code) {
        return statusMapper.map(code.category());
    }
}
