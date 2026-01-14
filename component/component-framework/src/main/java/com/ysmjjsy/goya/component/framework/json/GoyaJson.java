package com.ysmjjsy.goya.component.framework.json;

import com.ysmjjsy.goya.component.core.enums.IEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.MissingNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.type.TypeFactory;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * <p>统一JSON工具类</p>
 *
 * @author goya
 * @since 2025/12/21 00:43
 */
@Slf4j
public class GoyaJson implements ApplicationContextAware {

    @Getter
    private static JsonMapper jsonMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jsonMapper = applicationContext.getBean(JsonMapper.class);
    }

    /**
     * 获取TypeFactory（用于构造复杂泛型类型）
     *
     * @return TypeFactory实例
     */
    public static TypeFactory getTypeFactory() {
        return getJsonMapper().getTypeFactory();
    }

    // -------------------- 基本序列化/反序列化 --------------------

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 待序列化对象
     * @return JSON 字符串，如果序列化失败返回 null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return getJsonMapper().writeValueAsString(obj);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON序列化错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象序列化为格式化的 JSON 字符串
     *
     * @param obj 待序列化对象
     * @return 格式化的 JSON 字符串，如果序列化失败返回 null
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return getJsonMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(obj);
    }

    /**
     * 判断字符串是否是合法 JSON
     *
     * @param text JSON 字符串
     * @return 是否是合法
     */
    public static boolean isJson(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            getJsonMapper().readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象
     *
     * @param content JSON 字符串
     * @param clazz   目标对象类型
     * @param <T>     泛型类型
     * @return 反序列化后的对象，如果 content 为空或解析失败返回 null
     */
    public static <T> T fromJson(String content, Class<T> clazz) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            return getJsonMapper().readValue(content, clazz);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化错误, class={}: {}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为带泛型的对象
     *
     * @param content       JSON 字符串
     * @param typeReference 泛型类型参考
     * @param <T>           泛型类型
     * @return 反序列化后的对象，如果 content 为空或解析失败返回 null
     */
    public static <T> T fromJson(String content, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            return getJsonMapper().readValue(content, typeReference);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化错误, typeReference={}: {}", typeReference, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为 JavaType 对象
     *
     * @param content  JSON 字符串
     * @param javaType Jackson 的 JavaType 对象
     * @param <T>      泛型类型
     * @return 反序列化后的对象，如果 content 为空或解析失败返回 null
     */
    public static <T> T fromJson(String content, JavaType javaType) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            return getJsonMapper().readValue(content, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化错误, javaType={}: {}", javaType, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为指定类型对象（适用于对象之间的转换）
     *
     * @param fromValue   源对象
     * @param toValueType 目标对象类型
     * @param <T>         泛型类型
     * @return 转换后的对象，如果转换失败返回 null
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        if (fromValue == null) {
            return null;
        }
        try {
            return getJsonMapper().convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            log.error("[JsonUtils] convertValue 错误, targetClass={}: {}", toValueType.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为指定泛型类型对象（适用于对象之间的转换）
     *
     * @param fromValue     源对象
     * @param typeReference 泛型类型参考
     * @param <T>           泛型类型
     * @return 转换后的对象，如果转换失败返回 null
     */
    public static <T> T convertValue(Object fromValue, TypeReference<T> typeReference) {
        if (fromValue == null) {
            return null;
        }
        try {
            return getJsonMapper().convertValue(fromValue, typeReference);
        } catch (IllegalArgumentException e) {
            log.error("[JsonUtils] convertValue 错误, typeReference={}: {}", typeReference, e.getMessage(), e);
            return null;
        }
    }

    // -------------------- 集合/数组处理 --------------------

    /**
     * 将 JSON 字符串反序列化为 List
     *
     * @param content JSON 字符串
     * @param clazz   List 元素类型
     * @param <T>     泛型类型
     * @return 反序列化后的 List，如果失败返回 null
     */
    public static <T> List<T> fromJsonList(String content, Class<T> clazz) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        try {
            JavaType javaType = getJsonMapper().getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return getJsonMapper().readValue(content, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化为List失败, class={}: {}",
                    clazz.getName(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 将 JSON 字符串反序列化为 Set
     *
     * @param content JSON 字符串
     * @param clazz   Set 元素类型
     * @param <T>     泛型类型
     * @return 反序列化后的 Set，如果失败返回 null
     */
    public static <T> Set<T> fromJsonSet(String content, Class<T> clazz) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptySet();
        }
        try {
            JavaType javaType = getJsonMapper().getTypeFactory()
                    .constructCollectionLikeType(Set.class, clazz);
            return getJsonMapper().readValue(content, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化为Set失败, class={}: {}",
                    clazz.getName(), e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 将 JSON 字符串反序列化为数组
     *
     * @param content JSON 字符串
     * @param clazz   数组元素类型
     * @param <T>     泛型类型
     * @return 反序列化后的数组，如果失败返回 null
     */
    public static <T> T[] fromJsonArray(String content, Class<T> clazz) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            JavaType javaType = getJsonMapper().getTypeFactory().constructArrayType(clazz);
            return getJsonMapper().readValue(content, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化为数组失败, class={}: {}",
                    clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为 Map
     *
     * @param content    JSON 字符串
     * @param keyClass   Map 键类型
     * @param valueClass Map 值类型
     * @param <K>        键泛型
     * @param <V>        值泛型
     * @return 反序列化后的 Map，如果失败返回 null
     */
    public static <K, V> Map<K, V> fromJsonMap(String content, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptyMap();
        }
        try {
            JavaType javaType = getJsonMapper().getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return getJsonMapper().readValue(content, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化为Map失败, keyClass={}, valueClass={}: {}",
                    keyClass.getName(), valueClass.getName(), e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 将 JSON 字符串反序列化为 Map<String, Object>
     *
     * @param content JSON 字符串
     * @return 反序列化后的 Map，如果失败返回 null
     */
    public static Map<String, Object> fromJsonMap(String content) {
        return fromJsonMap(content, String.class, Object.class);
    }

    /**
     * Object → Map<String, Object> 直接转换
     *
     * @param obj 对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return getJsonMapper().convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    // -------------------- InputStream 支持 --------------------

    /**
     * 将 InputStream 反序列化为指定类型对象
     *
     * @param inputStream InputStream
     * @param clazz       目标类型
     * @param <T>         泛型类型
     * @return 反序列化后的对象，如果失败返回 null
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            return null;
        }
        try {
            return getJsonMapper().readValue(inputStream, clazz);
        } catch (JacksonException e) {
            log.error("[JsonUtils] InputStream 反序列化错误, class={}: {}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 InputStream 反序列化为泛型对象
     *
     * @param inputStream   InputStream
     * @param typeReference 泛型类型参考
     * @param <T>           泛型类型
     * @return 反序列化后的对象，如果失败返回 null
     */
    public static <T> T fromJson(InputStream inputStream, TypeReference<T> typeReference) {
        if (inputStream == null) {
            return null;
        }
        try {
            return getJsonMapper().readValue(inputStream, typeReference);
        } catch (JacksonException e) {
            log.error("[JsonUtils] InputStream 反序列化错误, typeReference={}: {}", typeReference, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 InputStream 反序列化为 JavaType 对象
     *
     * @param inputStream InputStream
     * @param javaType    Jackson JavaType
     * @param <T>         泛型类型
     * @return 反序列化后的对象，如果失败返回 null
     */
    public static <T> T fromJson(InputStream inputStream, JavaType javaType) {
        if (inputStream == null) {
            return null;
        }
        try {
            return getJsonMapper().readValue(inputStream, javaType);
        } catch (JacksonException e) {
            log.error("[JsonUtils] InputStream 反序列化错误, javaType={}: {}", javaType, e.getMessage(), e);
            return null;
        }
    }

    // -------------------- JsonNode 支持 --------------------

    /**
     * 将 JSON 字符串解析为 JsonNode
     *
     * @param content JSON 字符串
     * @return JsonNode 对象，如果失败返回 null
     */
    public static JsonNode toJsonNode(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            return getJsonMapper().readTree(content);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON反序列化为JsonNode失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JsonParser 解析为 JsonNode
     *
     * @param parser JsonParser
     * @return JsonNode 对象，如果失败返回 null
     */
    public static JsonNode toJsonNode(JsonParser parser) {
        if (parser == null) {
            return null;
        }
        try {
            return getJsonMapper().readTree(parser);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JsonParser 反序列化为JsonNode失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JsonNode → String
     *
     * @param node JsonNode
     * @return String
     */
    public static String toString(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return getJsonMapper().writeValueAsString(node);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JsonNode 转 JSON 错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析字符串字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @return value
     */
    public static String findStringValue(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.get(fieldName);
        return (value != null && value.isString()) ? value.asString() : null;
    }

    /**
     * 解析 boolean 字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @return value
     */
    public static boolean findBooleanValue(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return false;
        }
        JsonNode value = jsonNode.get(fieldName);
        return value != null && value.isBoolean() && value.asBoolean();
    }

    /**
     * 解析 long 字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @return value
     */
    public static Long findLongValue(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.get(fieldName);
        return (value != null && value.isLong()) ? value.asLong() : null;
    }

    /**
     * 解析字段
     *
     * @param jsonNode           jsonNode
     * @param fieldName          fieldName
     * @param valueTypeReference valueTypeReference
     * @param mapper             mapper
     * @param <T>                T
     * @return T
     */
    public static <T> T findValue(JsonNode jsonNode, String fieldName, TypeReference<T> valueTypeReference, JsonMapper mapper) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.get(fieldName);
        return (value != null) ? mapper.convertValue(value, valueTypeReference) : null;
    }

    /**
     * 解析字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @return jsonNode
     */
    public static JsonNode findObjectNode(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null) {
            return null;
        }
        JsonNode value = jsonNode.get(fieldName);
        return (value != null && value.isObject()) ? value : null;
    }

    /**
     * 解析字段
     *
     * @param jsonNode jsonNode
     * @param field    field
     * @return
     */
    public static JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }

    /**
     * 支持解析枚举类型字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @param enumClass enumClass
     * @param <E>       enum
     * @return enum
     */
    public static <E extends Enum<E>> E findEnumValue(JsonNode jsonNode, String fieldName, Class<E> enumClass) {
        String text = findStringValue(jsonNode, fieldName);
        if (text == null) {
            return null;
        }
        try {
            // 尝试按 name 匹配
            return Enum.valueOf(enumClass, text);
        } catch (IllegalArgumentException e) {
            // 如果找不到 name，再尝试按自定义 getValue
            for (E eValue : enumClass.getEnumConstants()) {
                if (eValue instanceof IEnum<?> && ((IEnum<?>) eValue).getCode().toString().equals(text)) {
                    return eValue;
                }

            }
            return null;
        }
    }

    /**
     * 支持解析 LocalDateTime 字段
     *
     * @param jsonNode  jsonNode
     * @param fieldName fieldName
     * @return
     */
    public static LocalDateTime findLocalDateTime(JsonNode jsonNode, String fieldName) {
        JsonNode value = jsonNode.findValue(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isString()) {
            return LocalDateTime.parse(value.asString());
        }
        if (value.isNumber()) {
            Instant instant = Instant.ofEpochMilli(value.asLong());
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        return null;
    }

    // -------------------- 流式遍历 JSON --------------------

    /**
     * 递归遍历 JsonNode 节点，并对值节点执行指定操作
     *
     * @param node     待遍历节点
     * @param consumer 对值节点的操作
     */
    public static void loop(JsonNode node, Consumer<JsonNode> consumer) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                loop(entry.getValue(), consumer);
            }
        } else if (node.isArray()) {
            node.forEach(n -> loop(n, consumer));
        } else {
            consumer.accept(node);
        }
    }

    // -------------------- JsonParser 创建 --------------------

    /**
     * 创建 JsonParser
     *
     * @param content JSON 字符串
     * @return JsonParser 对象，如果失败返回 null
     */
    public static JsonParser createParser(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            return getJsonMapper().createParser(content);
        } catch (JacksonException e) {
            log.error("[JsonUtils] 创建 JsonParser 错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON 合并（ObjectNode 合并）
     *
     * @param main   主
     * @param update 待合并
     * @return JsonNode
     */
    public static JsonNode merge(JsonNode main, JsonNode update) {
        if (main == null) {
            return update;
        }
        if (update == null) {
            return main;
        }

        if (main.isObject() && update.isObject()) {
            ObjectNode mainObj = (ObjectNode) main;
            update.properties().forEach(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                JsonNode existing = mainObj.get(key);
                if (existing != null && existing.isObject() && value.isObject()) {
                    merge(existing, value);
                } else {
                    mainObj.set(key, value);
                }
            });
            return mainObj;
        }
        return update;
    }

    /**
     * 使用 JSON patch 更新对象（部分字段）
     *
     * @param patchJson JSON
     * @param target    目标
     * @param <T>       目标对象
     * @return 更新后对象
     */
    public static <T> T updateObject(String patchJson, T target) {
        if (StringUtils.isBlank(patchJson) || target == null) {
            return target;
        }
        try {
            ObjectReader updater = getJsonMapper().readerForUpdating(target);
            return updater.readValue(patchJson);
        } catch (JacksonException e) {
            log.error("[JsonUtils] JSON patch 更新对象失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 JSON 中的某个字段（支持路径）
     *
     * @param node JsonNode
     * @param path path
     * @return JsonNode
     */
    public static JsonNode get(JsonNode node, String path) {
        if (node == null || StringUtils.isBlank(path)) {
            return null;
        }
        String[] parts = path.split("\\.");
        JsonNode current = node;
        for (String p : parts) {
            current = current.get(p);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * clone 对象（深拷贝）
     *
     * @param obj   对象
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        return fromJson(toJson(obj), clazz);
    }
}
