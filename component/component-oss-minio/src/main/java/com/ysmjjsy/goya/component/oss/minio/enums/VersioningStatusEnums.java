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
 * <p>版本状态</p>
 *
 * @author goya
 * @since 2025/11/1 15:47
 */
@Schema(name = "版本状态")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum VersioningStatusEnums implements CodeEnum<Integer> {

    OFF(0, "未开启"),
    ENABLED(1, "开启"),
    SUSPENDED(2, "暂停")
    ;
    @Schema(name = "常量值")
    @JsonValue
    private final Integer code;
    @Schema(name = "文字")
    private final String label;

    private static final Map<Integer, VersioningStatusEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (VersioningStatusEnums anEnum : VersioningStatusEnums.values()) {
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
    public static VersioningStatusEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
