package com.ysmjjsy.goya.component.framework.core.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Locale;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 15:21
 */
@ConfigurationProperties(PropertyConst.PROPERTY_GOYA_FRAMEWORK + ".i18n")
public record I18nProperties(
        /*
         * 是否启用 i18n。
         */
        @DefaultValue("true")
        Boolean enabled,

        /*
         * MessageSource 的 basename 列表。
         *
         * <p>默认值建议为：classpath:i18n/messages</p>
         * <p>注意：basename 不带语言后缀，不带 .properties。</p>
         */
        @DefaultValue("classpath:i18n/messages")
        List<String> baseNames,

        /*
         * 默认 Locale。
         *
         * <p>当无法从上下文获取 Locale 时使用。</p>
         */
        @DefaultValue("SIMPLIFIED_CHINESE")
        Locale defaultLocale,

        /*
         * 资源文件编码。
         */
        @DefaultValue("UTF-8")
        String encoding,

        /*
         * 是否使用 key 作为默认 message（缺失文案时不抛异常）。
         *
         * <p>企业系统强烈建议开启，避免因缺文案导致接口异常。</p>
         */
        @DefaultValue("true")
        Boolean useCodeAsDefaultMessage
) {
}
