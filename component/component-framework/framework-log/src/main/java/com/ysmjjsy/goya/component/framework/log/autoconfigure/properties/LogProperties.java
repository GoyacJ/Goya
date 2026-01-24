package com.ysmjjsy.goya.component.framework.log.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * <p>配置项</p>
 *
 * <p>说明：</p>
 * <ul>
 *   <li>所有默认值使用 Spring Boot 的 {@link DefaultValue}</li>
 *   <li>嵌套配置同样使用 record</li>
 * </ul>
 *
 * @param enabled 总开关
 * @param slowThresholdMs 慢调用阈值（毫秒）
 * @param aop AOP 方法日志配置
 * @param mask 脱敏配置
 * @author goya
 * @since 2026/1/24 21:57
 */
@ConfigurationProperties(prefix = PropertyConst.PROPERTY_LOG)
public record LogProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("500") long slowThresholdMs,
        @DefaultValue Aop aop,
        @DefaultValue Mask mask,
        @DefaultValue("32000") Integer eventErrorMaxLen
) {

    /**
     * AOP 方法日志配置。
     *
     * @param enabled 是否启用 AOP
     * @param includePackages 需要拦截的包前缀（为空表示仅对 @Loggable 生效）
     * @param excludePackages 排除的包前缀（优先级高于 include）
     * @param logArgs 是否记录入参
     * @param logResult 是否记录返回值
     * @param maxResultLength 返回值最大字符串长度（防止日志爆炸）
     */
    public record Aop(
            @DefaultValue("true") boolean enabled,
            @DefaultValue List<String> includePackages,
            @DefaultValue List<String> excludePackages,
            @DefaultValue("true") boolean logArgs,
            @DefaultValue("false") boolean logResult,
            @DefaultValue("2000") int maxResultLength
    ) {
    }

    /**
     * 脱敏配置。
     *
     * @param enabled 是否启用脱敏
     * @param maxDepth 最大递归深度
     * @param maxStringLength 单个字符串最大长度（超过截断）
     * @param expandBeanOnSensitive 当对象存在 @Sensitive 字段/record 组件时，是否展开对象并对该字段脱敏。
     * @param maxBeanFields 展开对象时最大字段数量，防止日志过大。
     */
    public record Mask(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("6") int maxDepth,
            @DefaultValue("2000") int maxStringLength,
            @DefaultValue("true") boolean expandBeanOnSensitive,
            @DefaultValue("64") int maxBeanFields
    ) {
    }
}
