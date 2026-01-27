package com.ysmjjsy.goya.component.framework.oss.annotation;

import com.ysmjjsy.goya.component.oss.core.enums.OssEnum;
import com.ysmjjsy.goya.component.oss.core.condition.OnOssCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/3 09:26
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnOssCondition.class)
public @interface ConditionalOnOssStrategy {

    /**
     * {@link OssEnum} 属性必须配置.
     *
     * @return 预期的算法
     */
    OssEnum value();
}
