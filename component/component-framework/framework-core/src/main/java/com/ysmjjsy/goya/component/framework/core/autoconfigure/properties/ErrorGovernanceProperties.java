package com.ysmjjsy.goya.component.framework.core.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p>错误码治理配置项</p>
 *
 * @author goya
 * @since 2026/1/24 14:22
 */
@ConfigurationProperties(PropertyConst.PROPERTY_ERROR + ".governance")
public record ErrorGovernanceProperties(
        /*
         * 是否启用错误码治理校验
         */
        @DefaultValue("true")
        Boolean enabled,

        /*
         * 校验失败时是否阻止应用启动
         */
        @DefaultValue("true")
        Boolean failFast,

        /*
         * 错误码格式校验正则
         */
        @DefaultValue("^GOYA-[A-Z0-9]+-[A-Z0-9]+-[0-9]{4}$")
        String codePattern,

        /*
         * 是否强制 defaultMessage 非空
         */
        @DefaultValue("true")
        Boolean requireDefaultMessage,

        /*
         * 是否强制 messageKey 非空
         */
        @DefaultValue("true")
        Boolean requireMessageKey
) {
}
