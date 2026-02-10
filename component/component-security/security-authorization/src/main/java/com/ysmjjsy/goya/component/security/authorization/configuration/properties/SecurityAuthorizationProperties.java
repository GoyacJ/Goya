package com.ysmjjsy.goya.component.security.authorization.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Arrays;
import java.util.List;

/**
 * <p>资源服务配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(defaultValue = "Security 资源服务配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY_RESOURCE)
public record SecurityAuthorizationProperties(

        @DefaultValue("true")
        boolean enabled,

        @DefaultValue("AUTO")
        ResourceTokenMode mode,

        @DefaultValue("/security/login,/security/login/session,/api/security/auth/**,/actuator/health,/error")
        String permitAllPatterns,

        @DefaultValue("X-Tenant-Id")
        String tenantHeader,

        @DefaultValue("X-User-Id")
        String userHeader,

        @DefaultValue("tenant_id")
        String tenantClaim,

        @DefaultValue("sub")
        String userClaim,

        @DefaultValue("role_ids")
        String roleIdsClaim,

        @DefaultValue("team_ids")
        String teamIdsClaim,

        @DefaultValue("org_ids")
        String orgIdsClaim,

        @DefaultValue("STRICT")
        ConsistencyMode consistencyMode,

        @DefaultValue("false")
        boolean requireUserHeaderForMachineToken,

        @DefaultValue("ACCESS")
        String apiAction,

        @DefaultValue("")
        String issuerUri,

        @DefaultValue("")
        String jwkSetUri,

        @DefaultValue("")
        String introspectionUri,

        @DefaultValue("")
        String introspectionClientId,

        @DefaultValue("")
        String introspectionClientSecret,

        @DefaultValue("goya:security:token:revoked")
        String revokedCacheName,

        @DefaultValue("true")
        boolean policyEnabled
) {

    public enum ResourceTokenMode {
        AUTO,
        JWT,
        OPAQUE
    }

    public enum ConsistencyMode {
        STRICT,
        LENIENT,
        OFF
    }

    public List<String> permitAllPatternList() {
        if (permitAllPatterns == null || permitAllPatterns.isBlank()) {
            return List.of();
        }
        return Arrays.stream(permitAllPatterns.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isBlank())
                .toList();
    }
}
