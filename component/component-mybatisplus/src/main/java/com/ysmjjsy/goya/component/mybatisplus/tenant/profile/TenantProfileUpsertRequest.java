package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.ysmjjsy.goya.component.framework.common.pojo.DTO;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * <p>租户画像写入</p>
 *
 * @author goya
 * @since 2026/1/29 00:01
 */
@Schema(name = "TenantProfileUpsertRequest", description = "租户画像创建/更新请求（PUT 幂等 upsert）")
public record TenantProfileUpsertRequest(

        @Schema(description = "租户模式：CORE_SHARED（核心共享库）/ DEDICATED_DB（独库）", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "DEDICATED_DB")
        @NotNull(message = "mode 不能为空")
        TenantMode mode,

        @Schema(description = "dynamic-datasource 的 dsKey。CORE_SHARED 通常为 core；DEDICATED_DB 通常为 tenant_xxx",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "tenant_10001")
        @NotBlank(message = "dsKey 不能为空")
        String dsKey,

        @Schema(description = "独库是否仍追加 tenant_id 条件。null 表示使用默认值（推荐 true）",
                example = "true")
        Boolean tenantLineEnabled
) implements DTO {
}