package com.ysmjjsy.goya.component.cache.redis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

/**
 * <p>于 JsonMapper 的 Redisson 全局 JSON Codec（无类型信息版）</p>
 *
 * <p>设计目标：</p>
 * <ul>
 *   <li>全局统一使用项目内的 JsonMapper（Jackson 3）进行序列化/反序列化</li>
 *   <li>不写入任何类型信息字段（例如 @type），更适合微服务跨服务演进</li>
 *   <li>序列化结果为 JSON bytes；反序列化默认解析为 {@code Object.class}</li>
 * </ul>
 *
 * <p><b>重要说明（必须配套 CacheService 做类型转换）：</b></p>
 * <ul>
 *   <li>由于不包含类型信息，解码结果通常是 Map/List/Number/String/Boolean 等</li>
 *   <li>如果业务希望得到 DTO/record，需要在缓存读取时根据目标类型做 convert（convertValue/treeToValue）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 22:24
 */
public class TypedJsonMapperCodec extends BaseCodec {
    private final JsonMapper jsonMapper;

    private final Encoder encoder = new JsonMapperEncoder();
    private final Decoder<Object> decoder = new JsonMapperDecoder();

    /**
     * 构造函数。
     *
     * @param jsonMapper 项目统一 JsonMapper（Spring 容器注入）
     */
    public TypedJsonMapperCodec(JsonMapper jsonMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper 不能为空");
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /**
     * JSON 编码器（无类型信息）。
     */
    private class JsonMapperEncoder implements Encoder {
        @Override
        public ByteBuf encode(Object in) {
            if (in == null) {
                return null;
            }
            try {
                byte[] bytes = jsonMapper.writeValueAsBytes(in);
                return Unpooled.wrappedBuffer(bytes);
            } catch (JacksonException e) {
                throw new IllegalStateException("Redisson JSON 编码失败，type=" + in.getClass().getName(), e);
            }
        }
    }

    /**
     * JSON 解码器（无类型信息，默认解为 Object）。
     *
     * <p>典型返回：</p>
     * <ul>
     *   <li>JSON object -> {@code java.util.LinkedHashMap}</li>
     *   <li>JSON array -> {@code java.util.ArrayList}</li>
     *   <li>number/string/bool -> 对应基础类型</li>
     * </ul>
     */
    private class JsonMapperDecoder implements Decoder<Object> {
        @Override
        public Object decode(ByteBuf buf, State state) {
            if (buf == null || !buf.isReadable()) {
                return null;
            }
            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                return jsonMapper.readValue(bytes, Object.class);
            } catch (JacksonException e) {
                throw new IllegalStateException("Redisson JSON 解码失败", e);
            }
        }
    }
}