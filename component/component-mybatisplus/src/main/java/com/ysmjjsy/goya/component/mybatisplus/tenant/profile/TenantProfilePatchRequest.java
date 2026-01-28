package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>租户画像局部更新请求</p>
 *
 * @author goya
 * @since 2026/1/29 00:07
 */
@Schema(name = "TenantProfilePatchRequest", description = "租户画像局部更新请求（仅更新非 null 字段）")
public record TenantProfilePatchRequest(

        @Schema(description = "租户模式：CORE_SHARED / DEDICATED_DB", example = "CORE_SHARED")
        TenantMode mode,

        @Schema(description = "dynamic-datasource 的 dsKey", example = "core")
        String dsKey,

        @Schema(description = "独库是否仍追加 tenant_id 条件", example = "false")
        Boolean tenantLineEnabled
) {
}
