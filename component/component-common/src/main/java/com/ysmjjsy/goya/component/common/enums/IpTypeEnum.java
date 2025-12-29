package com.ysmjjsy.goya.component.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
import com.ysmjjsy.goya.component.common.utils.NetUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>IP 类型</p>
 *
 * @author goya
 * @since 2025/10/21 10:33
 */
@Getter
@AllArgsConstructor
@Schema(description = "IP 类型")
public enum IpTypeEnum implements IEnum<Integer> {

    @Schema(description = "ipv4")
    IPV4(1, "IPv4"),

    @Schema(description = "ipv6")
    IPV6(2, "IPv6"),

    @Schema(description = "未知")
    UNKNOWN(3, "Unknown");
    ;
    private final Integer code;
    private final String description;

    /**
     * 获取 IP 地址类型
     *
     * @param ip IP 地址
     * @return IP 类型
     */
    public static IpTypeEnum ipType(String ip) {
        return NetUtils.ipType(ip);
    }

    private static final Map<Integer, IpTypeEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (IpTypeEnum anEnum : IpTypeEnum.values()) {
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

    public static IpTypeEnum getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }

    @JsonCreator
    public static IpTypeEnum fromJson(Map<String, String> map) {
        return getByCode(Integer.parseInt(map.get(IBaseConstants.STR_CODE)));
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
