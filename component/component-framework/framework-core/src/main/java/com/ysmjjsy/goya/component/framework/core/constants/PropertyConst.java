package com.ysmjjsy.goya.component.framework.core.constants;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:43
 */
public interface PropertyConst {

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

    String PROPERTY_GOYA = "goya";
    String PROPERTY_GOYA_FRAMEWORK = PROPERTY_GOYA + ".framework";


    /**
     * PROPERTY_GOYA_FRAMEWORK
     */
    String PROPERTY_CRYPTO = PROPERTY_GOYA_FRAMEWORK + ".crypto";
    String PROPERTY_PROTOCOL = PROPERTY_GOYA_FRAMEWORK + ".protocol";
    String PROPERTY_ARCHITECTURE = PROPERTY_GOYA_FRAMEWORK + ".architecture";
    String PROPERTY_LOG = PROPERTY_GOYA_FRAMEWORK + ".log";
    String PROPERTY_MASKER = PROPERTY_GOYA_FRAMEWORK + ".masker";
    String PROPERTY_SERVLET = PROPERTY_GOYA_FRAMEWORK + ".servlet";
    String PROPERTY_ERROR = PROPERTY_GOYA_FRAMEWORK + ".error";
}
