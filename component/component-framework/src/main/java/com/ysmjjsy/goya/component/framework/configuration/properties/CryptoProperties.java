package com.ysmjjsy.goya.component.framework.configuration.properties;

import com.ysmjjsy.goya.component.framework.constants.PropertyConst;
import com.ysmjjsy.goya.component.framework.enums.CryptoStrategyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/14 16:18
 */
@Schema(description = "加密策略")
@ConfigurationProperties(PropertyConst.PROPERTY_CRYPTO)
public record CryptoProperties(

        /*
          加密策略
         */
        @DefaultValue("STANDARD")
        CryptoStrategyEnum strategy,

        /*
          加密过期时间
         */
        @DefaultValue("PT5M")
        Duration expire
) {
}
