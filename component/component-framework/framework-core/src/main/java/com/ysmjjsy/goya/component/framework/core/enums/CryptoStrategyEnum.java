package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>加密策略</p>
 *
 * @author goya
 * @since 2025/10/9 16:45
 */
@Getter
@AllArgsConstructor
@Schema(description = "加密策略")
public enum CryptoStrategyEnum implements PropertyEnum {

    /**
     * 传统加密算法
     */
    @Schema(description = "传统加密算法")
    STANDARD("STANDARD", "传统加密算法"),

    /**
     * 国密加密算法
     */
    @Schema(description = "国密加密算法")
    SM("SM", "国密加密算法"),

    ;

    @Schema(description = "状态编码")
    private final String code;

    @Schema(description = "状态描述")
    private final String label;

    @Override
    public String getPrefix() {
        return PropertyConst.PROPERTY_CRYPTO + ".strategy";
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String label() {
        return label;
    }
}
