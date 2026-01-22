package com.ysmjjsy.goya.component.framework.configuration.properties;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/21 22:24
 */
@Schema(description = "平台信息")
public record PlatformInfo(

        @Schema(description = "平台名称")
        @DefaultValue("Goya")
        String projectName,

        @Schema(description = "平台网站url")
        @DefaultValue("www.ysmjjsy.com")
        String website,

        @Schema(description = "基础包名")
        @DefaultValue("com.ysmjjsy.goya")
        String baskPackageName
) {
}
