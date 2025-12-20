package com.ysmjjsy.goya.component.common.enums;

import com.ysmjjsy.goya.component.common.configuration.properties.PlatformProperties;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.enums.IPropertyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

/**
 * <p>系统语言</p>
 *
 * @author goya
 * @since 2025/12/20 21:41
 */
@Schema(description = "系统语言")
@Getter
@AllArgsConstructor
public enum LocaleEnum implements IPropertyEnum {

    /**
     * 中国（Monolith）
     */
    @Schema(description = "中国（ZH_CN）")
    ZH_CN("ZH_CN", "中国", Locale.SIMPLIFIED_CHINESE),

    /**
     * 美国（Distributed）
     */
    @Schema(description = "美国（EN_US）")
    EN_US("EN_US", "美国", Locale.US),


    ;

    @Schema(description = "状态编码")
    private final String code;

    @Schema(description = "状态描述")
    private final String description;

    @Schema(description = "对应的 Java Locale")
    private final Locale javaLocale;

    @Override
    public String getPrefix() {
        return IBaseConstants.PROPERTY_PLATFORM_LOCALE;
    }

    public static Locale getDefaultLocal() {
        PlatformProperties properties = SpringContext.getBean(PlatformProperties.class);
        if (properties == null || properties.locale() == null) {
            return Locale.getDefault();
        }
        return properties.locale().getJavaLocale();
    }
}
