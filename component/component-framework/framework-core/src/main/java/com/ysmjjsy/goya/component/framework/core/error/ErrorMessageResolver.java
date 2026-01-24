package com.ysmjjsy.goya.component.framework.core.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;

/**
 * <p>错误消息解析器</p>
 * <p>该组件用于在 Spring 环境中，将 {@link ErrorCode} 或 {@link GoyaException}
 * 解析为“可对外展示”的安全文案。</p>
 *
 * <h2>解析优先级建议</h2>
 * <ol>
 *   <li>异常的 userMessage（业务显式指定，最高优先级）</li>
 *   <li>MessageSource 通过 messageKey + args 解析（国际化/文案平台）</li>
 *   <li>ErrorCode.defaultMessage（兜底）</li>
 * </ol>
 *
 * @author goya
 * @since 2026/1/24 13:44
 */
public interface ErrorMessageResolver {

    /**
     * 解析异常对应的对外安全文案。
     *
     * @param ex GoyaException（不能为空）
     * @return 对外安全文案（非空）
     */
    String resolve(GoyaException ex);

    /**
     * 解析错误码对应的对外安全文案。
     *
     * @param code 错误码（不能为空）
     * @param args 消息参数（可为空）
     * @return 对外安全文案（非空）
     */
    String resolve(ErrorCode code, Object[] args);
}