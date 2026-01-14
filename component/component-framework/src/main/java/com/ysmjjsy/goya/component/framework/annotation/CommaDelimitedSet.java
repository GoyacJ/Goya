package com.ysmjjsy.goya.component.framework.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.ysmjjsy.goya.component.framework.json.CommaDelimitedCollectionJacksonComponent;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>逗号分隔字符串与Set集合互转注解</p>
 * <p>
 * 专门用于Set&lt;String&gt;类型的字段，将Set序列化为逗号分隔的字符串，或将逗号分隔的字符串反序列化为Set。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class User {
 *     @CommaDelimitedSet
 *     private Set<String> roles; // 序列化为 "role1,role2,role3"
 * }
 * }</pre>
 * </p>
 *
 * @author goya
 * @since 2025/12/21 22:05
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = CommaDelimitedCollectionJacksonComponent.SetSerializer.class)
@JsonDeserialize(using = CommaDelimitedCollectionJacksonComponent.SetDeserializer.class)
public @interface CommaDelimitedSet {
}
