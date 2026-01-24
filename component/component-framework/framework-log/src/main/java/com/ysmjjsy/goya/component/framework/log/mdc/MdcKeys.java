package com.ysmjjsy.goya.component.framework.log.mdc;

/**
 * <p>MDC 键名定义</p>
 *
 * @author goya
 * @since 2026/1/24 22:04
 */
public final class MdcKeys {

    private MdcKeys() {
    }

    /**
     * 链路追踪 ID。
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 用户 ID（可选）。
     */
    public static final String USER_ID = "userId";

    /**
     * 租户 ID（可选）。
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * 语言标签（可选，例如 zh-CN）。
     */
    public static final String LOCALE = "locale";
}
