package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * <p>Protocol枚举 </p>
 *
 * @author goya
 * @since 2021/6/12 14:48
 */
@Schema(description = "协议类型")
@Getter
@AllArgsConstructor
public enum ProtocolEnum implements PropertyEnum {

    /**
     * http
     */
    @Schema(description = "http")
    HTTP("HTTP", "http", "http://", "http"),

    /**
     * https
     */
    @Schema(description = "https")
    HTTPS("HTTPS", "https", "https://", "https"),

    ;

    @Schema(description = "状态编码")
    private final String code;

    @Schema(description = "状态描述")
    private final String label;

    @Schema(description = "格式")
    private final String format;

    @Schema(description = "前缀")
    private final String prefix;

    @Override
    public String getPrefix() {
        return PropertyConst.PROPERTY_PROTOCOL;
    }

    /**
     * 根据前缀获取协议
     *
     * @param prefix 前缀
     * @return 协议
     */
    public static ProtocolEnum getByPrefix(String prefix) {
        return Arrays.stream(values()).filter(e -> e.getPrefix().equals(prefix)).findFirst().orElse(null);
    }
}
