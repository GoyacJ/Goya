package com.ysmjjsy.goya.component.framework.json;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/21 21:56
 */
@Slf4j
@JacksonComponent
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommaDelimitedCollectionJacksonComponent {

    /**
     * List集合序列化器
     * 将List集合序列化为逗号分隔的字符串
     */
    public static class ListSerializer extends ValueSerializer<List<?>> {

        @Override
        public void serialize(List<?> value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            if (CollectionUtils.isEmpty(value)) {
                gen.writeString(SymbolConst.BLANK);
                return;
            }
            String result = value.stream()
                    .map(item -> item == null ? SymbolConst.BLANK : item.toString())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(SymbolConst.COMMA));
            gen.writeString(result);
        }
    }

    /**
     * List集合反序列化器
     * 将逗号分隔的字符串反序列化为List集合
     */
    public static class ListDeserializer extends ValueDeserializer<List<String>> {

        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = p.getString();
            if (StringUtils.isBlank(text)) {
                return new ArrayList<>();
            }
            return Arrays.stream(text.split(SymbolConst.COMMA))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Set集合序列化器
     * 将Set集合序列化为逗号分隔的字符串
     */
    public static class SetSerializer extends ValueSerializer<Set<?>> {
        @Override
        public void serialize(Set<?> value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            if (CollectionUtils.isEmpty(value)) {
                gen.writeString(SymbolConst.BLANK);
                return;
            }
            String result = value.stream()
                    .map(item -> item == null ? SymbolConst.BLANK : item.toString())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(SymbolConst.COMMA));
            gen.writeString(result);
        }
    }

    /**
     * Set集合反序列化器
     * 将逗号分隔的字符串反序列化为Set集合
     */
    public static class SetDeserializer extends ValueDeserializer<Set<String>> {

        @Override
        public Set<String> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = p.getString();
            if (StringUtils.isBlank(text)) {
                return new LinkedHashSet<>();
            }
            return Arrays.stream(text.split(SymbolConst.COMMA))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * 通用Collection集合序列化器
     * 将Collection集合序列化为逗号分隔的字符串
     */
    public static class CollectionSerializer extends ValueSerializer<Collection<?>> {

        @Override
        public void serialize(Collection<?> value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            if (CollectionUtils.isEmpty(value)) {
                gen.writeString("");
                return;
            }
            String result = value.stream()
                    .map(item -> item == null ? SymbolConst.BLANK : item.toString())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(SymbolConst.COMMA));
            gen.writeString(result);
        }
    }

    /**
     * 通用Collection集合反序列化器
     * 将逗号分隔的字符串反序列化为List集合（默认使用List）
     */
    public static class CollectionDeserializer extends ValueDeserializer<Collection<String>> {

        @Override
        public Collection<String> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = p.getString();
            if (StringUtils.isBlank(text)) {
                return new ArrayList<>();
            }
            return Arrays.stream(text.split(SymbolConst.COMMA))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 字符串转数组序列化器
     * 将逗号分隔的字符串序列化为数组
     * 例如："1,2,3" -> ["1", "2", "3"]
     */
    public static class StringToArraySerializer extends ValueSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            if (StringUtils.isBlank(value)) {
                gen.writeStartArray();
                gen.writeEndArray();
                return;
            }
            gen.writeStartArray();
            String[] parts = value.split(SymbolConst.COMMA);
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    gen.writeString(trimmed);
                }
            }
            gen.writeEndArray();
        }
    }

    /**
     * 数组转字符串反序列化器
     * 将数组或字符串反序列化为逗号分隔的字符串
     * 例如：["1", "2", "3"] -> "1,2,3" 或 "1,2,3" -> "1,2,3"
     */
    public static class ArrayToStringDeserializer extends ValueDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            // 获取当前token
            JsonToken currentToken = p.currentToken();

            // 如果是数组开始token
            if (currentToken == JsonToken.START_ARRAY) {
                // 解析数组并转换为逗号分隔字符串
                List<String> list = new ArrayList<>();
                JsonToken token;
                while ((token = p.nextToken()) != null && token != JsonToken.END_ARRAY) {
                    if (token == JsonToken.VALUE_STRING) {
                        String value = p.getString();
                        if (StringUtils.isNotBlank(value)) {
                            list.add(value.trim());
                        }
                    } else if (token ==JsonToken.VALUE_NULL) {
                        // 跳过null值
                    } else if (token.isStructStart()) {
                        // 嵌套结构，跳过
                        p.skipChildren();
                    } else {
                        // 其他标量值，转换为字符串
                        String value = p.getString();
                        if (StringUtils.isNotBlank(value)) {
                            list.add(value.trim());
                        }
                    }
                }
                if (list.isEmpty()) {
                    return SymbolConst.BLANK;
                }
                return String.join(SymbolConst.COMMA, list);
            } else if (currentToken == JsonToken.VALUE_STRING) {
                // 如果已经是字符串，直接返回
                String text = p.getString();
                return StringUtils.isBlank(text) ? SymbolConst.BLANK : text;
            } else if (currentToken == JsonToken.VALUE_NULL) {
                // null值，返回空字符串
                return SymbolConst.BLANK;
            } else {
                // 其他类型，尝试读取为字符串
                String text = p.getString();
                return StringUtils.isBlank(text) ? SymbolConst.BLANK : text;
            }
        }
    }
}
