package com.ysmjjsy.goya.component.cache.redis.codec;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 23:54
 */
public class TypedJsonMapperCodec extends BaseCodec {

    /**
     * 类型字段名
     */
    private static final String TYPE_FIELD = "@type";

    /**
     * 数据字段名
     */
    private static final String DATA_FIELD = "data";

    /**
     * JsonMapper（Jackson 3）
     */
    private final JsonMapper jsonMapper;

    /**
     * 反序列化基础包列表（用于拼接全限定类名）
     */
    private final Set<String> typeBasePackages;

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
     * @param jsonMapper       JsonMapper（从 Spring 容器注入）
     * @param typeBasePackages 反序列化基础包列表
     */
    public TypedJsonMapperCodec(JsonMapper jsonMapper, List<String> typeBasePackages) {
        if (jsonMapper == null) {
            throw new IllegalArgumentException("JsonMapper 不能为空");
        }
        this.jsonMapper = jsonMapper;
        this.typeBasePackages = SpringContext.getPackageNames();
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
     * 类型化编码器：写入 simpleName
     */
    private final class TypedEncoder implements Encoder {

        @Override
        public ByteBuf encode(Object in) {
            if (in == null) {
                return null;
            }

            try {
                ObjectNode wrapper = jsonMapper.createObjectNode();
                wrapper.put(TYPE_FIELD, in.getClass().getSimpleName());
                wrapper.set(DATA_FIELD, jsonMapper.valueToTree(in));

                byte[] bytes = jsonMapper.writeValueAsBytes(wrapper);
                return Unpooled.wrappedBuffer(bytes);
            } catch (JacksonException e) {
                throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).userMessage("TypedJsonMapperCodec 编码失败").build();
            }
        }
    }

    /**
     * 类型化解码器：根据 simpleName + basePackages 尝试恢复类型
     */
    private final class TypedDecoder implements Decoder<Object> {

        @Override
        public Object decode(ByteBuf buf, State state) {
            if (buf == null || !buf.isReadable()) {
                return null;
            }

            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);

                JsonNode root = jsonMapper.readTree(bytes);
                if (!root.isObject()) {
                    return jsonMapper.treeToValue(root, Object.class);
                }

                ObjectNode obj = (ObjectNode) root;
                JsonNode dataNode = obj.get(DATA_FIELD);
                if (dataNode == null) {
                    dataNode = root;
                }

                JsonNode typeNode = obj.get(TYPE_FIELD);
                if (typeNode == null || typeNode.isNull()) {
                    return jsonMapper.treeToValue(dataNode, Object.class);
                }

                String simpleName = typeNode.asText("");
                if (simpleName.isBlank()) {
                    return jsonMapper.treeToValue(dataNode, Object.class);
                }

                Class<?> target = resolveBySimpleName(simpleName);
                return jsonMapper.treeToValue(dataNode, target);
            } catch (Exception e) {
                throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).userMessage("TypedJsonMapperCodec 解码失败").build();
            }
        }

        /**
         * 根据 simpleName 解析目标类型
         *
         * @param simpleName 简单类名
         * @return 解析到的类型；解析失败则返回 Object.class
         */
        private Class<?> resolveBySimpleName(String simpleName) {
            // 1) 快速路径：如果就是全限定（极少数情况下你可能传入），直接尝试
            if (simpleName.indexOf('.') > 0) {
                try {
                    return Class.forName(simpleName);
                } catch (ClassNotFoundException _) {
                    return Object.class;
                }
            }

            // 2) 按基础包逐个拼接尝试
            for (String base : typeBasePackages) {
                if (base == null || base.isBlank()) {
                    continue;
                }
                String fqn = base.endsWith(".") ? base + simpleName : base + "." + simpleName;
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException _) {
                    // 继续尝试下一个
                }
            }

            // 3) 兜底
            return Object.class;
        }
    }

    /**
     * 将类型名写成 UTF-8（一些场景你可能想复用）
     *
     * @param typeName 类型名
     * @return UTF-8 字节
     */
    public static byte[] typeNameBytes(String typeName) {
        return typeName == null ? new byte[0] : typeName.getBytes(StandardCharsets.UTF_8);
    }
}