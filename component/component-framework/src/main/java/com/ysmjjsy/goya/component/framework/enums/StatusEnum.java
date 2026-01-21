package com.ysmjjsy.goya.component.framework.enums;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>状态</p>
 *
 * @author goya
 * @since 2025/10/9 09:38
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements IEnum<Integer> {

    /**
     * 成功
     */
    SUCCESS(1, "成功"),

    /**
     * 失败
     */
    FAILURE(2, "失败");

    private final Integer code;
    private final String description;

    private static final Map<Integer, StatusEnum> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (StatusEnum anEnum : StatusEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("description", anEnum.getDescription())
                            .build());
        }
    }

    public static StatusEnum getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
