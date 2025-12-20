package com.ysmjjsy.goya.component.common.definition.constants;

/**
 * <p>base constant interface</p>
 *
 * @author goya
 * @since 2025/12/19 22:27
 */
public interface IBaseConstants {

    /**
     * CHARSET_UTF8
     */
    String CHARSET_UTF8 = "UTF-8";

    /**
     * CHARSET_GBK
     */
    String CHARSET_GBK = "GBK";

    // ====================== 属性常量 ======================

    /**
     * SPRING
     */
    String PROPERTY_SPRING = "spring";

    /**
     * SPRING_APPLICATION
     */
    String PROPERTY_SPRING_APPLICATION = PROPERTY_SPRING + ".application";

    /**
     * SPRING_APPLICATION_NAME
     */
    String PROPERTY_SPRING_APPLICATION_NAME = PROPERTY_SPRING_APPLICATION + ".name";

    String PROPERTY_SERVER = "server";
    String PROPERTY_SERVER_SERVLET = PROPERTY_SERVER + ".servlet";

    String PROPERTY_SERVER_CONTEXT_PATH = PROPERTY_SERVER_SERVLET + ".context-path";

    /**
     * PROPERTY_PREFIX_PLATFORM
     */
    String PROPERTY_PLATFORM = "platform";

    /**
     * PROPERTY_PREFIX_PLATFORM_ARCHITECTURE
     */
    String PROPERTY_PLATFORM_ARCHITECTURE = PROPERTY_PLATFORM + ".architecture";

    /**
     * PROPERTY_PLATFORM_LOCALE
     */
    String PROPERTY_PLATFORM_LOCALE = PROPERTY_PLATFORM + ".locale";

    /**
     * PROPERTY_PREFIX_PLATFORM_REMOTE_DATA_TYPE
     */
    String PROPERTY_PLATFORM_REMOTE_DATA_TYPE = PROPERTY_PLATFORM + ".remoteDataType";

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
    String TREE_ROOT_ID = ISymbolConstants.ZERO;
}
