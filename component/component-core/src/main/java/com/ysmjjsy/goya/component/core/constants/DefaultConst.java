package com.ysmjjsy.goya.component.core.constants;

/**
 * <p></p>
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

    /**
     * BEARER_TYPE
     */
    String BEARER_TYPE = "Bearer";

    /**
     * BEARER_TOKEN
     */
    String BEARER_TOKEN = BEARER_TYPE + SymbolConst.SPACE;

}
