package com.ysmjjsy.goya.component.framework.servlet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.core.enums.NetworkTypeEnum;
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
public enum NetworkAccessTypeEnum implements CodeEnum<String> {

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

    @JsonValue
    private final String code;
    private final String label;

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
                            "label", anEnum.getLabel()
                    ));
        }
    }

    @JsonCreator
    public static NetworkAccessTypeEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
