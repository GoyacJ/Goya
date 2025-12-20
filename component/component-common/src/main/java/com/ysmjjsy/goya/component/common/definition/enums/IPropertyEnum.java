package com.ysmjjsy.goya.component.common.definition.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.env.Environment;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:56
 */
public interface IPropertyEnum extends IEnum<String>,IDescribableEnum {

    /**
     * 检测是否已经配置
     *
     * @param environment {@link Environment}
     * @return 否加密策略属性已经配置
     */
    boolean isActive(Environment environment);

    /**
     * 获取常量
     *
     * @return 常量
     */
    String getConstant();

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
        return StringUtils.isNotBlank(value) && Strings.CS.equals(value, getConstant());
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
        return StringUtils.isBlank(value) || Strings.CS.equals(value, getConstant());
    }

    @Override
    String getDescription();
}
