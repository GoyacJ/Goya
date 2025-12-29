package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.definition.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/7 22:54
 */
@Schema(description = "key")
public record KeyDTO(

        @Schema(description = "公钥")
        String publicKey,

        @Schema(description = "私钥")
        String privateKey
) implements DTO {
}
