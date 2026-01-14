package com.ysmjjsy.goya.component.framework.constants;

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

    /**
     * PROPERTY_PREFIX_PLATFORM
     */
    String PROPERTY_PLATFORM = "platform";
    String PROPERTY_CRYPTO = PROPERTY_PLATFORM + ".crypto";
    String PROPERTY_CACHE = PROPERTY_PLATFORM + ".cache";
    String PROPERTY_CAFFEINE = PROPERTY_CACHE + ".cache";
}
