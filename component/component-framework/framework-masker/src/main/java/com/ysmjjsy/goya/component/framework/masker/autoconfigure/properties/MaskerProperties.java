package com.ysmjjsy.goya.component.framework.masker.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * <p>配置项</p>
 * <p>配置前缀：{@code goya.masker}</p>
 *
 * @param enabled 总开关
 * @param maxDepth 最大递归深度
 * @param maxStringLength 最大字符串长度（截断）
 * @param expandBeanOnSensitive 对象存在 @Sensitive 时是否展开
 * @param maxBeanFields 展开对象时最大字段数
 * @param extraSensitiveKeys 额外敏感 key（会被 key 分类器识别为 GENERIC）
 * @param beanCopyEnabled 是否启用 JavaBean 同类型拷贝式脱敏（用于 API 场景保持结构不变）。
 * @param beanCopyUseFieldAccess 当属性没有 setter 时，是否允许用反射直接写字段（更兼容，但侵入更强）。
 * @author goya
 * @since 2026/1/24 22:54
 */
@ConfigurationProperties(prefix = PropertyConst.PROPERTY_MASKER)
public record MaskerProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("6") int maxDepth,
        @DefaultValue("2000") int maxStringLength,
        @DefaultValue("true") boolean expandBeanOnSensitive,
        @DefaultValue("64") int maxBeanFields,
        @DefaultValue List<String> extraSensitiveKeys,
        @DefaultValue("true") boolean beanCopyEnabled,
        @DefaultValue("true") boolean beanCopyUseFieldAccess
) {
}