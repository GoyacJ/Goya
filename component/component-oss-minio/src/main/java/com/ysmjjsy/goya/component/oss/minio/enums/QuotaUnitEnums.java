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
 * <p>配额单位</p>
 *
 * @author goya
 * @since 2025/11/1 15:43
 */
@Schema(name = "配额单位")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum QuotaUnitEnums implements CodeEnum<Integer> {

    KB(0, "KB"),
    MB(1, "MB"),
    GB(2, "GB"),
    TB(2, "TB"),
    ;

    @JsonValue
    @Schema(name = "常量值")
    private final Integer code;
    @Schema(name = "文字")
    private final String label;

    private static final Map<Integer, QuotaUnitEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (QuotaUnitEnums anEnum : QuotaUnitEnums.values()) {
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
    public static QuotaUnitEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
