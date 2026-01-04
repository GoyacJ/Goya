package com.ysmjjsy.goya.security.authentication.enums;

import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.Arrays;
import java.util.Objects;

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
public enum LoginGrantType implements IEnum<String> {

    @Schema(defaultValue = "密码")
    PASSWORD("PASSWORD", "password", new AuthorizationGrantType("password")),

    @Schema(defaultValue = "短信")
    SMS("SMS", "sms", new AuthorizationGrantType("sms")),

    @Schema(defaultValue = "社交登录")
    SOCIAL("SOCIAL", "social", new AuthorizationGrantType("social")),

    ;
    private final String code;
    private final String description;
    private final AuthorizationGrantType grantType;

    public static boolean check(HttpServletRequest request, LoginGrantType loginGrantType) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (StringUtils.isBlank(grantType)) {
            return false;
        }
        if (Objects.isNull(loginGrantType)) {
            return false;
        }
        return Strings.CI.equals(grantType, loginGrantType.getCode());
    }

    public static LoginGrantType resolve(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (StringUtils.isBlank(grantType)) {
            return null;
        }
        return Arrays.stream(LoginGrantType.values())
                .filter(l -> Strings.CI.equals(l.code, grantType))
                .findFirst()
                .orElse(null);
    }

}
