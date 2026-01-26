package com.ysmjjsy.goya.component.framework.core.condition;

import com.ysmjjsy.goya.component.framework.core.enums.PropertyEnum;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:55
 */
public abstract class AbstractEnumSpringBootCondition<T extends Enum<T> & PropertyEnum> extends SpringBootCondition {

    /**
     * 获取注解类
     *
     * @return 注解类
     */
    protected abstract Class<? extends Annotation> getAnnotationClass();

    @SuppressWarnings("all")
    @Override
    @NullMarked
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(getAnnotationClass().getName());
        T enums = (T) attributes.get("value");
        return getMatchOutcome(context.getEnvironment(), enums);
    }

    /**
     * 获取匹配结果
     *
     * @param environment 环境
     * @param enums       枚举
     * @return 匹配结果
     */
    private ConditionOutcome getMatchOutcome(Environment environment, T enums) {
        String name = enums.getConstant();
        ConditionMessage.Builder message = ConditionMessage.forCondition(getAnnotationClass());
        if (enums.isActive(environment)) {
            return ConditionOutcome.match(message.foundExactly(name));
        } else {
            return ConditionOutcome.noMatch(message.didNotFind(name).atAll());
        }
    }
}

