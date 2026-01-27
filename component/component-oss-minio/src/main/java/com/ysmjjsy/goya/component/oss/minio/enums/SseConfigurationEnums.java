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
 * <p>SseConfiguration 枚举</p>
 * 定义该枚举是为了方便前后端数据传输，以及具体Sse 具体策略切换
 * 通过统一的接口发送到前端，作为字典使用。用于单选，传索引值即可。
 * @author goya
 * @since 2025/11/1 15:46
 */
@Schema(name = "")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum SseConfigurationEnums implements CodeEnum<Integer> {

    DISABLED(0, "DISABLED"),
    AWS_KMS(1, "SSE_KMS"),
    AES256(2, "SSE_S3"),

    ;
    @Schema(name = "常量值")
    @JsonValue
    private final Integer code;
    @Schema(name = "文字")
    private final String label;

    private static final Map<Integer, SseConfigurationEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (SseConfigurationEnums anEnum : SseConfigurationEnums.values()) {
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
    public static SseConfigurationEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
