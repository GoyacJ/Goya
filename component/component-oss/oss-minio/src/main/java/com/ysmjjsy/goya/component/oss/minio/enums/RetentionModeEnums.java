package com.ysmjjsy.goya.component.oss.minio.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>保留模式</p>
 *
 * @author goya
 * @since 2025/11/1 15:44
 */
@Schema(name = "保留模式枚举")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum RetentionModeEnums implements IEnum<Integer> {

    /**
     * 治理模式。用户不能覆盖或删除对象版本或更改其锁定设置。
     * 要覆盖或删除治理模式保留设置，用户必须拥有 `s3:BypassGovernanceRetention` 权限，并且必须明确包括 `x-amz-bypass-governance-retention:true` 作为任何要求覆盖治理模式的请求的请求头。
     */
    GOVERNANCE(0, "治理模式"),
    /**
     * 合规模式。任何用户都不能覆盖或删除受保护对象版本
     */
    COMPLIANCE(1, "合规模式"),

    ;
    @Schema(name = "常量值")
    @JsonValue
    private final Integer code;
    @Schema(name = "文字")
    private final String description;

    private static final Map<Integer, RetentionModeEnums> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (RetentionModeEnums anEnum : RetentionModeEnums.values()) {
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
    public static RetentionModeEnums getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
