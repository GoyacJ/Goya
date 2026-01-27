package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
import org.springframework.core.env.Environment;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:56
 */
public interface PropertyEnum extends CodeEnum<String> {

    /**
     * 检测是否已经配置
     *
     * @param environment {@link Environment}
     * @return 否加密策略属性已经配置
     */
    default boolean isActive(Environment environment) {
        return isActive(environment, getPrefix());
    }

    /**
     * 获取前缀
     *
     * @return 前缀
     */
    String getPrefix();

    /**
     * 获取常量
     *
     * @return 常量
     */
    default String getConstant() {
        return getCode();
    }

    /**
     * 获取指定配置的 String 类型的值
     *
     * @param environment {@link Environment}
     * @param property    配置
     * @return 返回字符串
     */
    default String getString(Environment environment, String property) {
        return environment.getProperty(property, String.class);
    }

    /**
     * 属性是否已经配置。主要指设置了默认值以外的值。
     *
     * @param environment {@link Environment}
     * @param property    配置
     * @return 是否已经配置
     */
    default boolean isActive(Environment environment, String property) {
        String value = getString(environment, property);
        return GoyaStringUtils.equalsIgnoreCase(value, getConstant());
    }

    /**
     * 是否为默认值
     *
     * @param environment {@link Environment}
     * @param property    配置
     * @return 是否为默认值
     */
    default boolean isDefault(Environment environment, String property) {
        String value = getString(environment, property);
        return GoyaStringUtils.equalsIgnoreCase(value, getConstant());
    }
}
