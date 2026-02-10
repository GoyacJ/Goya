package com.ysmjjsy.goya.component.security.core.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 16:51
 */
@Schema(defaultValue = "Security 核心配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY)
public record SecurityCoreProperties(

        @Schema(defaultValue = "认证中心地址")
        @DefaultValue("")
        String authServiceUri,

        @Schema(defaultValue = "认证中心名称")
        @DefaultValue("")
        String authServiceName,

        @Schema(defaultValue = "租户配置")
        @DefaultValue
        Tenant tenant,

        @Schema(defaultValue = "Claim 配置")
        @DefaultValue
        Claims claims
) {

    @Schema(defaultValue = "租户配置")
    public record Tenant(

            @Schema(defaultValue = "是否启用租户解析")
            @DefaultValue("true")
            boolean enabled,

            @Schema(defaultValue = "租户请求头名称")
            @DefaultValue("X-Tenant-Id")
            String tenantHeader,

            @Schema(defaultValue = "租户Claim名称")
            @DefaultValue("tenant_id")
            String tenantClaim,

            @Schema(defaultValue = "默认租户")
            @DefaultValue("public")
            String defaultTenantId,

            @Schema(defaultValue = "已认证请求是否允许回退请求头租户")
            @DefaultValue("false")
            boolean allowHeaderFallbackWhenAuthenticated
    ) {
    }

    @Schema(defaultValue = "Claim 配置")
    public record Claims(

            @Schema(defaultValue = "用户ID claim")
            @DefaultValue("sub")
            String userId,

            @Schema(defaultValue = "用户名 claim")
            @DefaultValue("username")
            String username,

            @Schema(defaultValue = "角色 claim")
            @DefaultValue("roles")
            String roles,

            @Schema(defaultValue = "权限 claim")
            @DefaultValue("authorities")
            String authorities,

            @Schema(defaultValue = "OpenId claim")
            @DefaultValue("openId")
            String openId,

            @Schema(defaultValue = "客户端类型 claim")
            @DefaultValue("client_type")
            String clientType,

            @Schema(defaultValue = "会话ID claim")
            @DefaultValue("sid")
            String sid,

            @Schema(defaultValue = "MFA claim")
            @DefaultValue("mfa")
            String mfa
    ) {
    }
}
