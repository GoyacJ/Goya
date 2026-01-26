package com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 13:54
 */
@ConfigurationProperties(prefix = PropertyConst.PROPERTY_SERVLET + ".error")
public record ServletErrorProperties(

        /*
          是否让业务异常（ErrorCategory.BIZ）使用 HTTP 200。
         */
        @DefaultValue("true")
        boolean bizUseHttp200,

        /**
         * traceId 请求头名称
         */
        @DefaultValue("X-Trace-Id")
        String traceHeaderName,

        @DefaultValue("API")
        ResponseStyle responseStyle
) {

        /**
         * 响应输出风格。
         */
        public enum ResponseStyle {
                /**
                 * 仅输出 ApiResponse。
                 */
                API,

                /**
                 * 仅输出 ProblemDetail（RFC7807）。
                 */
                PROBLEM,

                /**
                 * 输出 ProblemDetail，并在扩展属性中附带 code/traceId/success/timestamp 等字段。
                 *
                 * <p>适用于希望兼容标准 ProblemDetail，同时又想保留企业字段的场景。</p>
                 */
                BOTH
        }
}
