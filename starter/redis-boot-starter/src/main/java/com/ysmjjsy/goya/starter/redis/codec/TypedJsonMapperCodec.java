package com.ysmjjsy.goya.starter.redis.codec;

import com.ysmjjsy.goya.component.cache.local.NullValueWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * 基于 JsonMapper 的 Redisson Codec，支持类型信息保存和恢复
 *
 * <p>使用 Jackson 3 的 JsonMapper 进行序列化/反序列化，并在序列化时保存类型信息，
 * 反序列化时根据类型信息恢复对象类型。
 *
 * <p><b>序列化格式：</b>
 * <pre>{@code
 * {
 *   "@type": "com.example.User",
 *   "@keyType": "java.lang.String",
 *   "data": {
 *     "id": 1,
 *     "name": "张三"
 *   }
 * }
 * }</pre>
 *
 * <p><b>特殊值处理：</b>
 * <ul>
 *   <li>null 值：直接序列化为 null（不包装）</li>
 *   <li>NullValueWrapper：序列化为特殊标记，反序列化时恢复</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class TypedJsonMapperCodec extends BaseCodec {

    /**
     * 类型信息字段名
     */
    private static final String TYPE_FIELD = "@type";

    /**
     * 键类型信息字段名（可选）
     */
    private static final String KEY_TYPE_FIELD = "@keyType";

    /**
     * 数据字段名
     */
    private static final String DATA_FIELD = "data";

    /**
     * NullValueWrapper 标记
     */
    private static final String NULL_VALUE_WRAPPER_MARKER = "@nullValueWrapper";

    /**
     * JsonMapper 实例
     */
    private final JsonMapper jsonMapper;

    /**
     * 编码器
     */
    private final Encoder encoder = new TypedEncoder();

    /**
     * 解码器
     */
    private final Decoder<Object> decoder = new TypedDecoder();

    /**
     * 构造函数
     *
     * @param jsonMapper JsonMapper 实例（从 Spring 容器注入）
     */
    public TypedJsonMapperCodec(JsonMapper jsonMapper) {
        if (jsonMapper == null) {
            throw new IllegalArgumentException("JsonMapper cannot be null");
        }
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    /**
     * 类型化编码器
     *
     * <p>序列化时保存类型信息，格式：{"@type":"类型全限定名","data":实际数据}
     */
    private class TypedEncoder implements Encoder {
        @Override
        public ByteBuf encode(Object in) {
            if (in == null) {
                return null;
            }

            try {
                byte[] bytes;
                // 处理 NullValueWrapper
                if (NullValueWrapper.isNullValue(in)) {
                    ObjectNode node = jsonMapper.createObjectNode();
                    node.put(TYPE_FIELD, NullValueWrapper.class.getName());
                    node.put(NULL_VALUE_WRAPPER_MARKER, true);
                    bytes = jsonMapper.writeValueAsBytes(node);
                } else {
                    // 获取实际类型
                    Class<?> actualType = in.getClass();
                    String typeName = actualType.getName();

                    // 创建包装对象
                    ObjectNode wrapper = jsonMapper.createObjectNode();
                    wrapper.put(TYPE_FIELD, typeName);

                    // 序列化实际数据
                    JsonNode dataNode = jsonMapper.valueToTree(in);
                    wrapper.set(DATA_FIELD, dataNode);

                    // 序列化为字节数组
                    bytes = jsonMapper.writeValueAsBytes(wrapper);
                }

                // 将字节数组包装为 ByteBuf
                return Unpooled.wrappedBuffer(bytes);
            } catch (JacksonException e) {
                log.error("Failed to encode object: type={}", in.getClass().getName(), e);
                throw new RuntimeException("Failed to encode object", e);
            }
        }
    }

    /**
     * 类型化解码器
     *
     * <p>反序列化时根据 @type 字段恢复类型
     */
    private class TypedDecoder implements Decoder<Object> {
        @Override
        public Object decode(ByteBuf buf, State state) {
            if (buf == null || buf.readableBytes() == 0) {
                return null;
            }

            try {
                // 从 ByteBuf 读取字节数组
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);

                // 解析 JSON
                JsonNode rootNode = jsonMapper.readTree(bytes);

                // 检查是否是 NullValueWrapper
                if (rootNode.isObject()) {
                    ObjectNode objectNode = (ObjectNode) rootNode;
                    if (objectNode.has(NULL_VALUE_WRAPPER_MARKER) &&
                            objectNode.get(NULL_VALUE_WRAPPER_MARKER).asBoolean()) {
                        return NullValueWrapper.INSTANCE;
                    }
                }

                // 获取类型信息
                if (!rootNode.isObject() || !rootNode.has(TYPE_FIELD)) {
                    // 如果没有类型信息，尝试直接反序列化（兼容旧数据）
                    log.warn("No type information found in serialized data, attempting direct deserialization");
                    return jsonMapper.treeToValue(rootNode, Object.class);
                }

                String typeName = rootNode.get(TYPE_FIELD).asText();
                Class<?> targetType;
                try {
                    targetType = Class.forName(typeName);
                } catch (ClassNotFoundException e) {
                    log.error("Class not found: typeName={}", typeName, e);
                    // 降级到 Object 类型
                    targetType = Object.class;
                }

                // 获取数据节点
                JsonNode dataNode = rootNode.get(DATA_FIELD);
                if (dataNode == null) {
                    log.warn("No data field found in serialized data, using root node");
                    dataNode = rootNode;
                }

                // 反序列化为目标类型
                return jsonMapper.treeToValue(dataNode, targetType);
            } catch (JacksonException e) {
                log.error("Failed to decode object", e);
                throw new RuntimeException("Failed to decode object", e);
            }
        }
    }
}

