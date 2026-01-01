package com.ysmjjsy.goya.component.social.enums;

import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:43
 */
@Slf4j
@Getter
@AllArgsConstructor
@Schema(defaultValue = "社交类型")
public enum SocialTypeEnum implements IEnum<String> {

    SMS("SMS", "短信"),
    EMAIL("EMAIL", "邮件"),
    ALI_PAY("ALI_PAY", "支付宝"),
    WE_CHAT_MINI_PROGRAM("WE_CHAT_MINI_PROGRAM", "微信小程序"),
    WE_CHAT_OFFICIAL_ACCOUNT("WE_CHAT_OFFICIAL_ACCOUNT", "微信公众号"),
    THIRD_PARTY("THIRD_PARTY", "第三方社交登录"),
    ;

    private final String code;
    private final String description;
}
