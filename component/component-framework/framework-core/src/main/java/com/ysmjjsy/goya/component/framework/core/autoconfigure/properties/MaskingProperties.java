package com.ysmjjsy.goya.component.framework.core.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.common.mask.MaskingKeys;
import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * <p>脱敏配置项</p>
 *
 * @author goya
 * @since 2026/1/24 14:28
 */
@ConfigurationProperties(PropertyConst.PROPERTY_GOYA_FRAMEWORK + ".mask")
public record MaskingProperties(
        /*
         * 是否启用脱敏（用于日志输出）。
         */
        @DefaultValue("true")
        Boolean enabled,

        /*
         * Map 脱敏：敏感键名列表（大小写不敏感，内部会统一转小写判断）。
         */
        @DefaultValue(MaskingKeys.DEFAULT_SENSITIVE_STRING_KEYS)
        String sensitiveKeys,

        /*
         * 是否对字符串应用正则脱敏（手机号/邮箱/身份证/银行卡等）。
         */
        Boolean enableRegexMasking,

        /*
         * 字符串最大长度（超过则截断），避免日志爆炸。
         */
        @DefaultValue("2048")
        Integer maxTextLength,

        /*
         * 超长截断后追加的后缀。
         */
        @DefaultValue("...(truncated)")
        String truncationSuffix,

        /*
         * 自定义正则规则（按顺序依次替换），每条规则格式：{@code regex=>replacement}。
         *
         * <p>示例：{@code (\\d{3})\\d{4}(\\d{4})=>$1****$2}</p>
         */
        @DefaultValue
        List<String> customRegexRules
) {
}
