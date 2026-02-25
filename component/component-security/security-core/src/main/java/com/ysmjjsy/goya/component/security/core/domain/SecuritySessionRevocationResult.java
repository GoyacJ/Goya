package com.ysmjjsy.goya.component.security.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>会话撤销结果</p>
 *
 * @author goya
 * @since 2026/2/25
 */
@Schema(defaultValue = "会话撤销结果")
public record SecuritySessionRevocationResult(

        @Schema(defaultValue = "命中会话数量")
        long matchedCount,

        @Schema(defaultValue = "实际撤销数量")
        long revokedCount
) {

    public static SecuritySessionRevocationResult empty() {
        return new SecuritySessionRevocationResult(0L, 0L);
    }
}
