package com.ysmjjsy.goya.component.common.utils;

import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/9/24 22:12
 */
@UtilityClass
public class PropertyResolverUtils {

    /**
     * 从环境信息中获取配置信息
     *
     * @param environment Spring Boot Environment {@link Environment}
     * @param property    配置名称
     * @return 配置属性值
     */
    public static String getProperty(Environment environment, String property) {
        return environment.getProperty(property);
    }

    /**
     * 从环境信息中获取配置信息
     *
     * @param environment  Spring Boot Environment {@link Environment}
     * @param property     配置名称
     * @param defaultValue 默认值
     * @return 配置属性值
     */
    public static String getProperty(Environment environment, String property, String defaultValue) {
        return environment.getProperty(property, defaultValue);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @return 配置属性值
     */
    public static String getProperty(ConditionContext conditionContext, String property) {
        return getProperty(conditionContext.getEnvironment(), property);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param defaultValue     默认值
     * @return 配置属性值
     */
    public static String getProperty(ConditionContext conditionContext, String property, String defaultValue) {
        return getProperty(conditionContext.getEnvironment(), property, defaultValue);
    }

    public static <T> T getProperty(Environment environment, String property, Class<T> targetType) {
        return environment.getProperty(property, targetType);
    }

    public static <T> T getProperty(Environment environment, String property, Class<T> targetType, T defaultValue) {
        return environment.getProperty(property, targetType, defaultValue);
    }

    public static <T> T getProperty(ConditionContext conditionContext, String property, Class<T> targetType) {
        return getProperty(conditionContext.getEnvironment(), property, targetType);
    }

    public static <T> T getProperty(ConditionContext conditionContext, String property, Class<T> targetType, T defaultValue) {
        return getProperty(conditionContext.getEnvironment(), property, targetType, defaultValue);
    }

    public static boolean contains(Environment environment, String property) {
        return environment.containsProperty(property);
    }

    public static boolean contains(ConditionContext conditionContext, String property) {
        return contains(conditionContext.getEnvironment(), property);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param environment  Spring Boot ConditionContext {@link ConditionContext}
     * @param property     配置名称
     * @param defaultValue 默认值
     * @return 配置属性值
     */
    public static boolean getBoolean(Environment environment, String property, boolean defaultValue) {
        return getProperty(environment, property, Boolean.class, defaultValue);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param environment Spring Boot ConditionContext {@link ConditionContext}
     * @param property    配置名称
     * @return 配置属性值
     */
    public static boolean getBoolean(Environment environment, String property) {
        return getProperty(environment, property, Boolean.class, false);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @return 配置属性值
     */
    public static boolean getBoolean(ConditionContext conditionContext, String property) {
        return getProperty(conditionContext, property, Boolean.class, false);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param defaultValue     默认值
     * @return 配置属性值
     */
    public static boolean getBoolean(ConditionContext conditionContext, String property, boolean defaultValue) {
        return getProperty(conditionContext, property, Boolean.class, defaultValue);
    }
}
