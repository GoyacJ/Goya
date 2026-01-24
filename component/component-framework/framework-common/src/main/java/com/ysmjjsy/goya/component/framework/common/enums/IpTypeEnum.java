package com.ysmjjsy.goya.component.framework.common.enums;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaNetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>IP 类型</p>
 *
 * @author goya
 * @since 2025/10/21 10:33
 */
@Getter
@AllArgsConstructor
public enum IpTypeEnum implements CodeEnum<Integer> {

    IPV4(1, "IPv4"),

    IPV6(2, "IPv6"),

    UNKNOWN(3, "Unknown"),
    ;
    private final Integer code;
    private final String label;

    /**
     * 获取 IP 地址类型
     *
     * @param ip IP 地址
     * @return IP 类型
     */
    public static IpTypeEnum ipType(String ip) {
        return GoyaNetUtils.ipType(ip);
    }

    @Override
    public Integer code() {
        return code;
    }

    @Override
    public String label() {
        return label;
    }
}
