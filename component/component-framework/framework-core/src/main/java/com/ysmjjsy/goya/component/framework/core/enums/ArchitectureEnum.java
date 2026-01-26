package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:58
 */
@Schema(description = "系统架构模式")
@Getter
@AllArgsConstructor
public enum ArchitectureEnum implements PropertyEnum {

    /**
     * 单体架构（Monolith）
     */
    @Schema(description = "单体架构（Monolith）")
    MONOLITH("MONOLITH", "单体架构"),

    /**
     * 分布式架构（Distributed）
     */
    @Schema(description = "分布式架构（Distributed）")
    DISTRIBUTED("DISTRIBUTED", "分布式架构"),

    ;

    @Schema(description = "状态编码")
    private final String code;

    @Schema(description = "状态描述")
    private final String label;

    @Override
    public String getPrefix() {
        return PropertyConst.PROPERTY_ARCHITECTURE;
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
