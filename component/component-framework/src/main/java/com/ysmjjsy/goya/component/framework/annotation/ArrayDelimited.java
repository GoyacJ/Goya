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
 * <p>数组与逗号分隔字符串互转注解</p>
 * <p>
 * 专门用于String类型的字段，将数组反序列化为逗号分隔的字符串，或将逗号分隔的字符串序列化为数组。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * public class User {
 *     @ArrayToString
 *     private String params; // 前端传入 ["1", "2", "3"]，后端接收为 "1,2,3"
 *                            // 后端返回时，会将 "1,2,3" 序列化为 ["1", "2", "3"]
 * }
 * }</pre>
 * </p>
 * <p>
 * 功能说明：
 * <ul>
 *     <li>反序列化（前端 -> 后端）：支持数组 ["1", "2", "3"] 或字符串 "1,2,3"，统一转换为 "1,2,3"</li>
 *     <li>序列化（后端 -> 前端）：将字符串 "1,2,3" 转换为数组 ["1", "2", "3"]</li>
 *     <li>空数组或空字符串：反序列化为空字符串 ""，序列化为空数组 []</li>
 *     <li>自动过滤空值和空字符串元素</li>
 * </ul>
 * </p>
 *
 * @author goya
 * @since 2025/12/21 22:03
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = CommaDelimitedCollectionJacksonComponent.StringToArraySerializer.class)
@JsonDeserialize(using = CommaDelimitedCollectionJacksonComponent.ArrayToStringDeserializer.class)
public @interface ArrayDelimited {
}
