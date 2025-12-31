package com.ysmjjsy.goya.component.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>接口网络类型</p>
 *
 * @author goya
 * @since 2025/10/21 10:23
 */
@Slf4j
@Getter
@AllArgsConstructor
@Schema(description = "接口网络类型")
public enum NetworkAccessTypeEnum implements IEnum<String> {

    /**
     * 内网访问
     */
    @Schema(description = "内网访问")
    ACCESS_INNER("inner", "内网访问"),

    /**
     * 外网访问
     */
    @Schema(description = "外网访问")
    ACCESS_OUTER("outer", "外网访问"),

    /**
     * 不限制
     */
    @Schema(description = "不限制")
    UNLIMITED("unlimited", "不限制"),

    ;
    private final String code;
    private final String description;

    /**
     * 检查 IP 是否符合当前网络类型
     *
     * @param ip IP 地址
     * @return 是否符合
     */
    public boolean check(String ip) {
        if (StringUtils.isBlank(ip)) {
            log.debug("interface network type check: ip is blank");
            return false;
        }

        // 不限制
        if (this == UNLIMITED) {
            return true;
        }

        NetworkTypeEnum networkTypeEnum = NetworkTypeEnum.networkType(ip);
        switch (networkTypeEnum) {
            case INNER -> {
                return this == ACCESS_INNER;
            }
            case OUTER -> {
                return this == ACCESS_OUTER;
            }
            default -> {
                return false;
            }
        }

    }

    private static final Map<String, NetworkAccessTypeEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (NetworkAccessTypeEnum anEnum : NetworkAccessTypeEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    Map.of(
                            "index", anEnum.ordinal(),
                            "code", anEnum.getCode(),
                            "name", anEnum.name(),
                            "description", anEnum.getDescription()
                    ));
        }
    }

    public static NetworkAccessTypeEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }

    @JsonCreator
    public static NetworkAccessTypeEnum fromJson(Object value) {
        // 如果是字符串，直接根据 code 查找
        if (value instanceof String va) {
            return INDEX_MAP.get(va);
        }
        // 如果是 Map 类型，根据 STR_CODE 查找
        if (value instanceof Map<?, ?> map) {
            Object code = map.get(IBaseConstants.STR_CODE);
            return code == null ? null : INDEX_MAP.get(code.toString());
        }

        // 不支持的类型返回 null 或抛异常
        throw new IllegalArgumentException("Unsupported JSON value: " + value);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
