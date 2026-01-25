package com.ysmjjsy.goya.component.framework.core.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import com.ysmjjsy.goya.component.framework.core.enums.ArchitectureEnum;
import com.ysmjjsy.goya.component.framework.core.enums.ProtocolEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 21:48
 */
@Schema(description = "平台配置")
@ConfigurationProperties(prefix = PropertyConst.PROPERTY_GOYA)
public record GoyaProperties(

        @Schema(description = "平台信息")
        @DefaultValue
        PlatformInfo platformInfo,

        @Schema(description = "系统架构模式")
        ArchitectureEnum architecture,

        @Schema(description = "系统协议")
        @DefaultValue("HTTP")
        ProtocolEnum protocol

) {

}
