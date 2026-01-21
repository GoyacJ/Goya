package com.ysmjjsy.goya.component.security.core.enums;

import com.ysmjjsy.goya.component.core.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/20 22:46
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@Schema(defaultValue = "认证操作类型")
public enum SecurityOperationEnum implements IEnum<String> {

    @Schema(defaultValue = "登陆成功")
    LOGIN_SUCCESS("LOGIN_SUCCESS", "登陆成功"),

    @Schema(defaultValue = "登陆失败")
    LOGIN_FAILURE("LOGIN_FAILURE", "登陆失败"),

    @Schema(defaultValue = "Token生成")
    TOKEN_GENERATE("TOKEN_GENERATE", "Token生成"),

    @Schema(defaultValue = "Token刷新")
    TOKEN_REFRESH("TOKEN_REFRESH", "Token刷新"),

    @Schema(defaultValue = "Token撤销")
    TOKEN_REVOKE("TOKEN_REVOKE", "Token撤销"),

    @Schema(defaultValue = "授权码生成")
    AUTHORIZATION_CODE_GENERATE("AUTHORIZATION_CODE_GENERATE", "授权码生成"),

    @Schema(defaultValue = "客户端认证失败")
    CLIENT_AUTH_FAILURE("CLIENT_AUTH_FAILURE", "客户端认证失败"),

    ;
    private final String code;
    private final String description;
}
