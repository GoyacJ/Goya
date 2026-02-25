package com.ysmjjsy.goya.component.security.core.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>社交登录类型</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@Getter
@AllArgsConstructor
@Schema(defaultValue = "社交类型")
public enum SocialTypeEnum implements CodeEnum<String> {

    THIRD_PART("THIRD_PART", "第三方登录"),
    WECHAT_MINI_PROGRAM("WECHAT_MINI_PROGRAM", "微信小程序登录");

    private final String code;
    private final String label;

    public static SocialTypeEnum findByCode(String code) {
        if (GoyaStringUtils.isEmpty(code)) {
            return null;
        }
        String upperCase = code.toUpperCase();
        return valueOf(upperCase);
    }
}
