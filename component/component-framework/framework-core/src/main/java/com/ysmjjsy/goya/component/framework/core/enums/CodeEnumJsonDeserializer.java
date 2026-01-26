package com.ysmjjsy.goya.component.framework.core.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumKit;
import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;

import java.io.Serializable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 15:44
 */
public class CodeEnumJsonDeserializer extends JsonDeserializer<Enum<?>>
        implements ContextualDeserializer {

    private Class<?> enumClass;

    /**
     * 默认构造。
     */
    public CodeEnumJsonDeserializer() {
    }

    private CodeEnumJsonDeserializer(Class<?> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * 创建上下文反序列化器，感知目标字段类型。
     *
     * @param ctxt     context
     * @param property property
     * @return deserializer
     * @throws JsonMappingException 映射异常
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        JavaType type = (property != null) ? property.getType() : ctxt.getContextualType();
        if (type == null) {
            return this;
        }
        Class<?> raw = type.getRawClass();
        return new CodeEnumJsonDeserializer(raw);
    }

    /**
     * 反序列化：根据 code 查找枚举常量。
     *
     * @param p    parser
     * @param ctxt context
     * @return Enum
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        if (enumClass == null) {
            return null;
        }

        String text;
        try {
            text = p.getValueAsString();
        } catch (Exception e) {
            throw new IllegalArgumentException("枚举反序列化失败：无法读取 code", e);
        }

        if (text == null) {
            return null;
        }

        Class target = enumClass;
        if (!target.isEnum() || !CodeEnum.class.isAssignableFrom(target)) {
            // 非 CodeEnum 枚举不处理
            return null;
        }

        // 统一按 String 读取，再由业务枚举自行定义 code 类型为 String/Integer 等
        // 这里选择将 code 作为 String 进行匹配，业务如需 Integer，可将 code() 定义为 String 更稳。
        try {
            return (Enum<?>) EnumKit.findByCode(target, (Serializable) text)
                    .orElseThrow(() -> new IllegalArgumentException("未找到枚举：" + target.getName() + ", code=" + text));
        } catch (Throwable e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }
}