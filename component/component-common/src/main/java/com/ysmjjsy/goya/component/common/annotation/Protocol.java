package com.ysmjjsy.goya.component.common.annotation;

import com.ysmjjsy.goya.component.common.condiition.ProtocolCondition;
import com.ysmjjsy.goya.component.common.enums.ProtocolEnum;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p>协议类型</p>
 *
 * @author goya
 * @since 2025/12/19 23:58
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ProtocolCondition.class)
public @interface Protocol {

    /**
     * 协议类型
     * @return 协议类型
     */
    ProtocolEnum value();
}
