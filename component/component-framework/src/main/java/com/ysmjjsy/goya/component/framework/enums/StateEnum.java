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
public enum StateEnum implements IEnum<Integer> {

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 禁用
     */
    DISABLED(2, "禁用");

    private final Integer code;
    private final String description;

    private static final Map<Integer, StateEnum> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (StateEnum anEnum : StateEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("description", anEnum.getDescription())
                            .build());
        }
    }

    public static StateEnum getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
