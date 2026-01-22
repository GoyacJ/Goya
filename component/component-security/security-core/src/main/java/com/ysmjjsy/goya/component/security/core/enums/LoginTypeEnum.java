package com.ysmjjsy.goya.component.security.core.enums;

import com.ysmjjsy.goya.component.core.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 11:37
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@Schema(defaultValue = "登录类型")
public enum LoginTypeEnum implements IEnum<String> {

    @Schema(defaultValue = "密码")
    PASSWORD("PASSWORD", "password"),

    @Schema(defaultValue = "短信")
    SMS("SMS", "sms"),

    @Schema(defaultValue = "社交登录")
    SOCIAL("SOCIAL", "social"),

    ;
    private final String code;
    private final String description;

    public static LoginTypeEnum resolve(HttpServletRequest request) {
        String loginType = request.getParameter("login_type");
        return LoginTypeEnum.valueOf(loginType);
    }
}
