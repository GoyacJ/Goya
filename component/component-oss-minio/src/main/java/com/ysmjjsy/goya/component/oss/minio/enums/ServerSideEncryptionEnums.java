package com.ysmjjsy.goya.component.oss.minio.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>服务端加密方法</p>
 *
 * @author goya
 * @since 2025/11/1 15:46
 */
@Schema(name = "服务端加密方法")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum ServerSideEncryptionEnums implements CodeEnum<Integer> {

    AWS_KMS(1, "SSE_KMS"),
    AES256(2, "SSE_S3"),
    CUSTOM(3, "自定义")
    ;
    @Schema(name = "常量值")
    @JsonValue
    private final Integer code;
    @Schema(name = "文字")
    private final String label;

    private static final Map<Integer, ServerSideEncryptionEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (ServerSideEncryptionEnums anEnum : ServerSideEncryptionEnums.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("label", anEnum.getLabel())
                            .build());
        }
    }

    @JsonCreator
    public static ServerSideEncryptionEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
