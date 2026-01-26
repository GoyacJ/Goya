package com.ysmjjsy.goya.component.framework.core.enums;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
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
public enum StatusEnum implements CodeEnum<String> {

    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 失败
     */
    FAILURE("FAILURE", "失败");

    private final String code;
    private final String label;

    private static final Map<String, StatusEnum> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (StatusEnum anEnum : StatusEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("label", anEnum.getLabel())
                            .build());
        }
    }

    public static StatusEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
