package com.ysmjjsy.goya.component.framework.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import com.ysmjjsy.goya.component.core.enums.IpTypeEnum;
import com.ysmjjsy.goya.component.core.utils.GoyaNetUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>网络类型</p>
 *
 * @author goya
 * @since 2025/10/21 09:51
 */
@Getter
@AllArgsConstructor
@Schema(description = "网络类型")
public enum NetworkTypeEnum implements IEnum<Integer> {

    /**
     * 内网
     */
    @Schema(description = "内网")
    INNER(1, "内网"),

    /**
     * 外网
     */
    @Schema(description = "外网")
    OUTER(2, "外网"),

    /**
     * 未知
     */
    @Schema(description = "未知")
    UNKNOWN(3, "未知");;

    @JsonValue
    private final Integer code;
    private final String description;

    /**
     * 判断是否为内网
     *
     * @param networkTypeEnum 网络类型
     * @return 是否为内网
     */
    public static boolean isInner(NetworkTypeEnum networkTypeEnum) {
        return INNER == networkTypeEnum;
    }

    /**
     * 判断是否为内网
     *
     * @param ip ip
     * @return 是否为内网
     */
    public static boolean isInner(String ip) {
        return INNER == networkType(ip);
    }

    /**
     * 判断是否为外网
     *
     * @param networkTypeEnum 网络类型
     * @return 是否为外网
     */
    public static boolean isOuter(NetworkTypeEnum networkTypeEnum) {
        return OUTER == networkTypeEnum;
    }

    /**
     * 判断是否为外网
     *
     * @param ip ip
     * @return 是否为外网
     */
    public static boolean isOuter(String ip) {
        return OUTER == networkType(ip);
    }

    /**
     * 根据Ip获取网络类型
     *
     * @param ip ip
     * @return 网络类型
     */
    public static NetworkTypeEnum networkType(String ip) {
        IpTypeEnum ipType = IpTypeEnum.ipType(ip);
        return switch (ipType) {
            case IPV4 -> {
                boolean innerIpv4 = GoyaNetUtils.isInnerIpv4(ip);
                yield innerIpv4 ? INNER : OUTER;
            }
            case IPV6 -> {
                boolean innerIpv6 = GoyaNetUtils.isInnerIpv6(ip);
                yield innerIpv6 ? INNER : OUTER;
            }
            default -> UNKNOWN;
        };
    }

    private static final Map<Integer, NetworkTypeEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (NetworkTypeEnum anEnum : NetworkTypeEnum.values()) {
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

    @JsonCreator
    public static NetworkTypeEnum getByCode(Integer code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
