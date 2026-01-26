package com.ysmjjsy.goya.component.framework.common.error;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * <p>错误载体对象，用于跨进程/跨协议传递错误信息</p>
 * <p>在微服务中不建议直接跨边界传递 Java 异常对象（包含堆栈、类信息等），
 * 而应将错误转换为结构化载体（例如 JSON）进行传递。该载体可用于：</p>
 * <ul>
 *   <li>HTTP/JSON 响应；</li>
 *   <li>RPC 返回体；</li>
 *   <li>消息队列消费失败的错误记录；</li>
 *   <li>异步任务失败的持久化记录。</li>
 * </ul>
 *
 * <h2>安全性</h2>
 * <p>{@code message} 字段应放置“可对外安全展示”的文案（例如 userMessage），
 * 严禁携带敏感信息（SQL、密钥、内部 IP、堆栈等）。诊断信息应走日志而非返回体。</p>
 *
 * <h2>线程安全</h2>
 * <p>该对象应视为不可变对象使用；{@code metadata} 建议由调用方提供不可变 Map。</p>
 *
 * @param code 错误码（稳定标识）
 * @param message 对外安全文案（建议）
 * @param messageKey 文案 key（i18n/文案平台）
 * @param category 错误分类
 * @param retryable 是否建议重试
 * @param severity 严重级别
 * @param timestamp 错误发生时间（UTC）
 * @param metadata 结构化上下文信息（可选）
 *
 * @author goya
 * @since 2026/1/24 13:25
 */
public record ErrorPayload(
        String code,
        String message,
        String messageKey,
        ErrorCategory category,
        boolean retryable,
        Severity severity,
        Instant timestamp,
        Map<String, Object> metadata
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造时若未提供时间戳，可由上层在转换时填充 {@link Instant#now()}。
     * 该 record 不主动填充默认值，以保持纯粹的数据承载语义。
     */
}