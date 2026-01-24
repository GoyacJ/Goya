package com.ysmjjsy.goya.component.framework.log.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * <p>方法调用事件载荷</p>
 *
 * <p>该对象应尽量“稳定、轻量、可序列化”，便于后续持久化或发送到消息系统。</p>
 *
 * <p>建议：args/result 在发布前就完成脱敏与截断，避免监听器误落敏感信息。</p>
 *
 * @param occurredAt       事件发生时间（方法结束时发布）
 * @param appName          appName
 * @param serviceName      服务名
 * @param requestUri       请求 URI
 * @param httpMethod       HTTP 方法
 * @param userAgent        User-Agent
 * @param clientIp         客户端 IP
 * @param className        类名
 * @param methodName       方法名
 * @param methodSignature  方法签名（简化）
 * @param costMs           耗时毫秒
 * @param success          是否成功
 * @param args             入参（可空，已脱敏）
 * @param result           返回值（可空，已脱敏/截断）
 * @param exceptionType    异常类型（可空）
 * @param exceptionMessage 异常信息（可空）
 * @param exceptionStack   异常堆栈（可空）
 * @param mdc              MDC 快照（traceId/tenantId/userId/locale 等，供扩展与检索）
 * @author goya
 * @since 2026/1/24 23:29
 */
public record MethodInvokeEventPayload(
        Instant occurredAt,
        String appName,
        String serviceName,
        String requestUri,
        String httpMethod,
        String userAgent,
        String clientIp,
        String className,
        String methodName,
        String methodSignature,
        long costMs,
        boolean success,
        Object args,
        Object result,
        String exceptionType,
        String exceptionMessage,
        String exceptionStack,
        Map<String, String> mdc
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}