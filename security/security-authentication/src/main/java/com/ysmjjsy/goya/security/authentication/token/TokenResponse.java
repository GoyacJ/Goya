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
         * Token类型，通常是"Bearer"
         */
        @Schema(description = "Token类型，通常是\"Bearer\"")
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
     * 创建Token响应
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
        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                issuedAt,
                expiresAt,
                null
        );
    }

    /*
     * 转换为Map格式（用于HTTP响应）
     *
     * @return Map格式的Token响应
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("access_token", accessToken);
        map.put("token_type", tokenType);
        map.put("expires_in", expiresIn);
        map.put("issued_at", issuedAt != null ? issuedAt.getEpochSecond() : null);

        if (refreshToken != null) {
            map.put("refresh_token", refreshToken);
        }

        if (scope != null) {
            map.put("scope", scope);
        }

        return map;
    }
}

