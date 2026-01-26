package com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * <p>Servlet i18n 配置项</p>
 *
 * @author goya
 * @since 2026/1/24 15:19
 */
@ConfigurationProperties(PropertyConst.PROPERTY_SERVLET + ".i18n")
public record ServletI18nProperties(
        /*
         * URL 参数名（例如：?lang=zh-CN）。
         */
        @DefaultValue("lang")
        String langParam,

        /*
         * Header 名称（例如：X-Lang: zh-CN）。
         */
        @DefaultValue("x-Lang")
        String langHeader,

        /*
         * 支持的语言列表（字符串形式），例如：zh-CN、en-US。
         *
         * <p>为空表示不限制。</p>
         */
        @DefaultValue
        List<String> supported
) {
}
