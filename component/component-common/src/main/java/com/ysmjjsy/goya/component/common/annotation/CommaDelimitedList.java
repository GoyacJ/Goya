package com.ysmjjsy.goya.component.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.ysmjjsy.goya.component.common.jackson.CommaDelimitedCollectionJacksonComponent;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>逗号分隔字符串与List集合互转注解</p>
 * <p>
 * 专门用于List&lt;String&gt;类型的字段，将List序列化为逗号分隔的字符串，或将逗号分隔的字符串反序列化为List。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class User {
 *     @CommaDelimitedList
 *     private List<String> tags; // 序列化为 "tag1,tag2,tag3"
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
@JsonSerialize(using = CommaDelimitedCollectionJacksonComponent.ListSerializer.class)
@JsonDeserialize(using = CommaDelimitedCollectionJacksonComponent.ListDeserializer.class)
public @interface CommaDelimitedList {
}
