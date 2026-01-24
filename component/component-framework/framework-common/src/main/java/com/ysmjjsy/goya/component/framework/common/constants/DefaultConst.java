package com.ysmjjsy.goya.component.framework.common.constants;

/**
 * <p>系统默认常量集</p>
 *
 * @author goya
 * @since 2026/1/8 22:40
 */
public interface DefaultConst {

    /**
     * CHARSET_UTF8
     */
    String CHARSET_UTF8 = "UTF-8";

    /**
     * CHARSET_GBK
     */
    String CHARSET_GBK = "GBK";

    // ====================== 通用日期格式 ======================

    /**
     * DATE_FORMAT_YYYY_MM_DD
     */
    String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * DATE_FORMAT_YYYY_MM_DD_HH_MM_SS
     */
    String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * DATE_FORMAT_YYYYMMDD
     */
    String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";

    /**
     * DATE_FORMAT_YYYYMMDDHHMMSS
     */
    String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    String DATE_FORMAT_HHMMSS = "HH:mm:ss";

    /**
     * DEFAULT_TIME_ZONE_NAME
     */
    String DEFAULT_TIME_ZONE_NAME = "Asia/Shanghai";

    String DEFAULT_TIME_FORMAT = DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;

    // ====================== 默认常量 ======================

    /**
     * code
     */
    String STR_CODE = "code";

    /**
     * name
     */
    String STR_NAME = "name";

    /**
     * localhost ip
     */
    String LOCAL_HOST_IP = "127.0.0.1";

    /**
     * 默认树形结构根节点
     */
    String TREE_ROOT_ID = SymbolConst.ZERO;

    /**
     * DEFAULT_TENANT_ID
     */
    String DEFAULT_TENANT_ID = "public";

    String DEFAULT_USER = "default";

    String LOCALE = "locale";

    /**
     * BEARER_TYPE
     */
    String BEARER_TYPE = "Bearer";

    /**
     * BEARER_TOKEN
     */
    String BEARER_TOKEN = BEARER_TYPE + SymbolConst.SPACE;

    String X_TRACE_ID = "X-Trace-Id";
    String X_TENANT_ID = "X-Tenant-Id";
    String X_USER_ID = "X-User-Id";
    /**
     * X_REQUEST_ID
     */
    String X_REQUEST_ID = "X-Request-Id";
    /**
     * 客户端传入的关联 ID（可选）。
     *
     * <p>注意：它不是 tracing 的 traceId，仅用于兼容/关联检索。</p>
     */
    String CLIENT_TRACE_ID = "clientTraceId";

}
