package com.ysmjjsy.goya.component.framework.masker.annotation;

import com.ysmjjsy.goya.component.framework.masker.core.SensitiveType;

import java.lang.annotation.*;

/**
 * <p>参数/字段级脱敏注解</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>方法参数：标注在 Controller/Service 方法参数上，AOP 记录入参时优先按该注解脱敏</li>
 *   <li>字段/record 组件：标注在 DTO/VO/Record 的字段或组件上，Masker 反射展开时对该字段脱敏</li>
 * </ul>
 *
 * <p>优先级：</p>
 * <ol>
 *   <li>@Sensitive（最优先，精准）</li>
 *   <li>Map key 分类器（password/token 等）</li>
 *   <li>字符串/通用规则兜底</li>
 * </ol>
 * @author goya
 * @since 2026/1/24 22:19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Sensitive {

    /**
     * 敏感类型。
     *
     * @return SensitiveType
     */
    SensitiveType type() default SensitiveType.GENERIC;
}