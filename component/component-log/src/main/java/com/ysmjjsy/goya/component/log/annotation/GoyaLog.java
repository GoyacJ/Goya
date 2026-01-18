package com.ysmjjsy.goya.component.log.annotation;

import com.ysmjjsy.goya.component.log.enums.OperatorTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 13:28
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GoyaLog {

    /**
     * 模块
     */
    String title() default "";

    /**
     * 操作类别
     */
    OperatorTypeEnum operatorType() default OperatorTypeEnum.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;


    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};
}
