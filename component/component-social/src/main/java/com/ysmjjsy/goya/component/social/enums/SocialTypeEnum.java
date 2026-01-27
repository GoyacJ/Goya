package com.ysmjjsy.goya.component.social.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
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
public enum SocialTypeEnum implements CodeEnum<String> {

    SMS("SMS", "短信", "SMS"),
    THIRD_PART("THIRD_PART", "第三方", "THIRDPART"),
    WECHAT_MINI_PROGRAM("WECHAT_MINI_PROGRAM", "微信小程序授权登录", "WECHAT_MINI_PROGRAM"),
    ;

    private final String code;
    private final String label;
    private final String mark;

    public static SocialTypeEnum findByCode(String code) {
        if (GoyaStringUtils.isEmpty(code)) {
            return null;
        }
        String upperCase = code.toUpperCase();
        return valueOf(upperCase);
    }
}
