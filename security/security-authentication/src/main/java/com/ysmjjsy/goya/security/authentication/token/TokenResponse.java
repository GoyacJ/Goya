package com.ysmjjsy.goya.security.authentication.token;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/*
 * Token响应DTO
 * <p>
 * 封装OAuth2 Token生成后的响应信息，包含access_token、refresh_token等
 * </p>
 *
 * @author goya
 * @since 2025/12/17 22:41
 */
@Schema(description = "Token响应DTO")
public record TokenResponse(
        
        /*
         * 访问令牌
         */
        @Schema(description = "访问令牌")
        String accessToken,

        /*
         * 刷新令牌(可选)
         */
        @Schema(description = "刷新令牌")
        String refreshToken,

        /*
         * Token类型，通常是"Bearer"或"DPoP"（当使用DPoP绑定时）
         */
        @Schema(description = "Token类型，通常是\"Bearer\"或\"DPoP\"（当使用DPoP绑定时）")
        String tokenType,

        /*
         * 访问令牌过期时间(秒)
         */
        @Schema(description = "访问令牌过期时间(秒)")
        Long expiresIn,

        /*
         * 访问令牌签发时间
         */
        @Schema(description = "访问令牌签发时间")
        Instant issuedAt,

        /*
         * 访问令牌过期时间
         */
        @Schema(description = "访问令牌过期时间")
        Instant expiresAt,

        /*
         * 授权范围（可选）
         */
        @Schema(description = "授权范围")
        String scope
) {
    /*
     * 创建Token响应（默认Bearer类型）
     *
     * @param accessToken  访问令牌
     * @param refreshToken 刷新令牌（可为null）
     * @param expiresIn    过期时间（秒）
     * @param issuedAt     签发时间
     * @param expiresAt    过期时间
     * @return Token响应对象
     */
    public static TokenResponse of(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            Instant issuedAt,
            Instant expiresAt) {
        return of(accessToken, refreshToken, "Bearer", expiresIn, issuedAt, expiresAt);
    }

    /*
     * 创建Token响应（支持自定义token_type）
     * <p>根据Spring Security官方规范，当有DPoP proof时，token_type应为"DPoP"</p>
     * <p>参考：https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html</p>
     *
     * @param accessToken  访问令牌
     * @param refreshToken 刷新令牌（可为null）
     * @param tokenType    Token类型（"Bearer"或"DPoP"）
     * @param expiresIn    过期时间（秒）
     * @param issuedAt     签发时间
     * @param expiresAt    过期时间
     * @return Token响应对象
     */
    public static TokenResponse of(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            Instant issuedAt,
            Instant expiresAt) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenType,
                expiresIn,
                issuedAt,
                expiresAt,
                null
        );
    }
}

