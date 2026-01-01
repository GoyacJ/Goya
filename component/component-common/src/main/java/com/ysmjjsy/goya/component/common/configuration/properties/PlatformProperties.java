package com.ysmjjsy.goya.component.common.configuration.properties;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.enums.ArchitectureEnum;
import com.ysmjjsy.goya.component.common.enums.CryptoStrategyEnum;
import com.ysmjjsy.goya.component.common.enums.LocaleEnum;
import com.ysmjjsy.goya.component.common.enums.ProtocolEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 21:48
 */
@Schema(description = "平台配置")
@ConfigurationProperties(prefix = IBaseConstants.PROPERTY_PLATFORM)
public record PlatformProperties(

        @Schema(description = "平台信息")
        PlatformInfo platformInfo,

        @Schema(description = "系统架构模式")
        ArchitectureEnum architecture,

        @Schema(description = "系统协议")
        @DefaultValue("HTTP")
        ProtocolEnum protocol,

        @Schema(description = "系统语言")
        LocaleEnum locale,

        @Schema(description = "加密策略")
        @DefaultValue("STANDARD")
        CryptoStrategyEnum crypto,

        @Schema(description = "加密过期时间")
        @DefaultValue("PT5H")
        Duration cryptoExpire
) {

}
