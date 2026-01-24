package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCategory;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * <p>默认 HTTP 状态码映射器实现。</p>
 * <p>提供一套通用映射策略，并支持“业务异常是否返回 HTTP 200”的企业常见契约。</p>
 *
 * <h2>默认映射</h2>
 * <ul>
 *   <li>VALIDATION -> 400</li>
 *   <li>AUTH -> 403（具体 401/403 在异常处理器中基于错误码细分）</li>
 *   <li>NOT_FOUND -> 404</li>
 *   <li>CONFLICT -> 409</li>
 *   <li>RATE_LIMIT -> 429</li>
 *   <li>REMOTE/INFRA/SYSTEM -> 500</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 13:51
 */
public class DefaultHttpStatusMapper implements HttpStatusMapper {

    private final boolean bizUseHttp200;

    /**
     * 构造默认映射器。
     *
     * @param bizUseHttp200 是否让业务异常（BIZ）使用 HTTP 200（企业常见风格）
     */
    public DefaultHttpStatusMapper(boolean bizUseHttp200) {
        this.bizUseHttp200 = bizUseHttp200;
    }

    /** {@inheritDoc} */
    @Override
    public HttpStatus map(ErrorCategory category) {
        Objects.requireNonNull(category, "category 不能为空");
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case AUTH -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case BIZ -> bizUseHttp200 ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            case REMOTE, INFRA, SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
