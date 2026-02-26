package com.ysmjjsy.goya.component.admin.configuration.properties;

import com.ysmjjsy.goya.component.admin.constants.AdminConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>admin 组件配置</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Schema(defaultValue = "Admin 配置")
@ConfigurationProperties(prefix = AdminConst.PROPERTY_ADMIN)
public record AdminProperties(

        @Schema(defaultValue = "是否启用")
        @DefaultValue("true")
        boolean enabled,

        @Schema(defaultValue = "默认租户ID")
        @DefaultValue(AdminConst.DEFAULT_TENANT_ID)
        String defaultTenantId,

        @Schema(defaultValue = "租户请求头")
        @DefaultValue(AdminConst.TENANT_HEADER)
        String tenantHeader,

        @Schema(defaultValue = "租户issuer模板，支持 {tenantId} 占位")
        @DefaultValue("")
        String tenantIssuerTemplate,

        @Schema(defaultValue = "引导配置")
        @DefaultValue
        Bootstrap bootstrap,

        @Schema(defaultValue = "RBAC 缓存配置")
        @DefaultValue
        Rbac rbac,

        @Schema(defaultValue = "策略配置")
        @DefaultValue
        Policy policy,

        @Schema(defaultValue = "会话配置")
        @DefaultValue
        Session session
) {

    @Schema(defaultValue = "引导配置")
    public record Bootstrap(

            @Schema(defaultValue = "是否启用初始化")
            @DefaultValue("true")
            boolean enabled,

            @Schema(defaultValue = "管理员用户名")
            @DefaultValue("admin")
            String adminUsername,

            @Schema(defaultValue = "管理员密码")
            @DefaultValue("${GOYA_ADMIN_BOOTSTRAP_PASSWORD:}")
            String adminPassword,

            @Schema(defaultValue = "超管角色编码")
            @DefaultValue(AdminConst.SUPER_ADMIN_ROLE_CODE)
            String superRoleCode
    ) {
    }

    @Schema(defaultValue = "rbac 配置")
    public record Rbac(

            @Schema(defaultValue = "是否启用缓存")
            @DefaultValue("true")
            boolean cacheEnabled,

            @Schema(defaultValue = "缓存ttl")
            @DefaultValue("PT10M")
            Duration cacheTtl
    ) {
    }

    @Schema(defaultValue = "策略配置")
    public record Policy(

            @Schema(defaultValue = "角色权限是否同步策略")
            @DefaultValue("true")
            boolean syncEnabled
    ) {
    }

    @Schema(defaultValue = "会话配置")
    public record Session(

            @Schema(defaultValue = "角色权限变更是否撤销会话")
            @DefaultValue("true")
            boolean revokeOnRoleChange
    ) {
    }
}
