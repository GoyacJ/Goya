package com.ysmjjsy.goya.component.oss.minio.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>访问策略枚举</p>
 * 定义该枚举是为了方便前后端数据传输，以及具体Sse 具体策略切换
 * 通过统一的接口发送到前端，作为字典使用。用于单选，传索引值即可。
 * @author goya
 * @since 2025/11/1 14:58
 */
@Getter
@AllArgsConstructor
public enum PolicyEnums implements IEnum<Integer> {

    PRIVATE(0, "私有"),
    PUBLIC(1, "公开"),
    CUSTOM(2, "自定义"),

    ;

    @JsonValue
    private final Integer code;
    private final String description;

    private static final Map<Integer, PolicyEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (PolicyEnums anEnum : PolicyEnums.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("description", anEnum.getDescription())
                            .build());
        }
    }
    @JsonCreator
    public static PolicyEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
