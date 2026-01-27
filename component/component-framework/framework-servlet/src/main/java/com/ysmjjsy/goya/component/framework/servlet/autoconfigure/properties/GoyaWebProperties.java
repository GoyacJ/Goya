package com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.servlet.constant.WebConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:33
 */
@ConfigurationProperties(prefix = WebConst.PROPERTY_WEB)
@Schema(description = "web 配置")
public record GoyaWebProperties(

        @Schema(description = "扫描配置")
        @DefaultValue
        Scan scan,

        @Schema(description = "扫描配置")
        @DefaultValue
        Idempotent idempotent,

        @Schema(description = "扫描配置")
        @DefaultValue
        AccessLimited accessLimited,

        @Schema(description = "加密配置")
        @DefaultValue
        Crypto crypto
) {

    public record Crypto(
            /*
              加密过期时间
             */
            @DefaultValue("PT5M")
            Duration expire
    ) {

    }

    public record Scan(
            /*
              是否启用扫描
             */
            @DefaultValue("true")
            @Schema(description = "是否启用扫描")
            Boolean enabled,

            /*
              指定扫描的命名空间。未指定的命名空间中，即使包含RequestMapping，也不会被添加进来。
             */
            @DefaultValue
            @Schema(description = "指定扫描的命名空间")
            List<String> scanGroupIds,

            /*
             * Spring 中会包含 Controller和 RestController，
             * 如果该配置设置为True，那么就只扫描RestController
             * 如果该配置设置为False，那么Controller和 RestController斗扫描。
             */
            @DefaultValue("false")
            @Schema(description = "是否只扫描 RestController")
            Boolean justScanRestController
    ) implements Serializable {
    }

    public record Idempotent(

            @Schema(description = "过期时间")
            @DefaultValue("PT5S")
            Duration expire
    ) {
    }

    public record AccessLimited(

            @Schema(description = "最大次数")
            @DefaultValue("10")
            int maxTimes,

            @Schema(description = "过期时间")
            @DefaultValue("PT5S")
            Duration expire
    ) {
    }

}
