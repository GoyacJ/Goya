package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.Serial;
import java.util.Objects;

/**
 * <p>租户画像配置管理 Controller</p>
 *
 * @author goya
 * @since 2026/1/29 00:03
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/tenant-profiles")
@RequiredArgsConstructor
public class TenantProfileController {

    private final TenantProfileService service;

    /**
     * 分页列表查询。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @param tenantIdLike tenantId 前缀匹配（可选）
     * @return 分页结果
     */
    @Operation(
            summary = "分页查询租户画像列表",
            description = "分页返回 tenant_profile。支持 tenantId 前缀匹配（likeRight），用于快速定位租户。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public IPage<TenantProfileEntity> page(
            @Parameter(in = ParameterIn.QUERY, description = "页码（从 1 开始）", example = "1")
            @RequestParam(defaultValue = "1") long page,

            @Parameter(in = ParameterIn.QUERY, description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") long size,

            @Parameter(in = ParameterIn.QUERY, description = "tenantId 前缀匹配（likeRight）", example = "tenant_10")
            @RequestParam(required = false) String tenantIdLike
    ) {
        Page<TenantProfileEntity> p = Page.of(page, size);

        LambdaQueryWrapper<TenantProfileEntity> qw = new LambdaQueryWrapper<>();
        if (tenantIdLike != null && !tenantIdLike.trim().isEmpty()) {
            qw.likeRight(TenantProfileEntity::getTenantId, tenantIdLike.trim());
        }
        qw.orderByAsc(TenantProfileEntity::getTenantId);

        return service.page(p, qw);
    }

    /**
     * 查询单个租户画像。
     *
     * @param tenantId 租户 ID
     * @return entity
     */
    @Operation(summary = "查询租户画像", description = "按 tenantId 查询 tenant_profile 记录。不存在返回 404。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TenantProfileEntity.class))),
            @ApiResponse(responseCode = "404", description = "租户画像不存在",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{tenantId}")
    public TenantProfileEntity get(
            @Parameter(in = ParameterIn.PATH, description = "租户 ID", required = true, example = "tenant_10001")
            @PathVariable String tenantId
    ) {
        TenantProfileEntity e = service.getById(tenantId);
        if (e == null) {
            throw new TenantProfileNotFoundException(tenantId);
        }
        return e;
    }

    /**
     * 创建或更新（幂等 upsert）。
     *
     * @param tenantId 租户 ID
     * @param req 请求体
     * @return 最新 entity
     */
    @Operation(
            summary = "创建或更新（幂等 Upsert）",
            description = "PUT 幂等 upsert：不存在则创建，存在则全量覆盖 mode/dsKey/tenantLineEnabled。更新将触发 @Version 递增，用于缓存快速生效。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建/更新成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TenantProfileEntity.class))),
            @ApiResponse(responseCode = "400", description = "参数错误（mode/dsKey 不合法）",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{tenantId}")
    public TenantProfileEntity upsert(
            @Parameter(in = ParameterIn.PATH, description = "租户 ID", required = true, example = "tenant_10001")
            @PathVariable String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Upsert 请求体",
                    content = @Content(schema = @Schema(implementation = TenantProfileUpsertRequest.class))
            )
            @RequestBody @Valid TenantProfileUpsertRequest req
    ) {
        TenantProfileEntity e = service.getById(tenantId);
        boolean creating = (e == null);

        if (creating) {
            e = new TenantProfileEntity();
            e.setTenantId(tenantId);
        }

        e.setMode(req.mode());
        e.setDsKey(req.dsKey());
        e.setTenantLineEnabled(req.tenantLineEnabled() == null || req.tenantLineEnabled());

        if (creating) {
            service.save(e);
        } else {
            service.updateById(e);
        }

        TenantProfileEntity latest = service.getById(tenantId);
        if (latest == null) {
            throw new IllegalStateException("保存成功但读取失败，tenantId=" + tenantId);
        }
        return latest;
    }

    /**
     * 局部更新（PATCH）。
     *
     * @param tenantId 租户 ID
     * @param req patch 请求体
     * @return 最新 entity
     */
    @Operation(
            summary = "局部更新（PATCH）",
            description = "仅更新非 null 字段（mode/dsKey/tenantLineEnabled）。更新将触发 @Version 递增，用于缓存快速生效。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TenantProfileEntity.class))),
            @ApiResponse(responseCode = "404", description = "租户画像不存在",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{tenantId}")
    public TenantProfileEntity patch(
            @Parameter(in = ParameterIn.PATH, description = "租户 ID", required = true, example = "tenant_10001")
            @PathVariable String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Patch 请求体（仅更新非 null 字段）",
                    content = @Content(schema = @Schema(implementation = TenantProfilePatchRequest.class))
            )
            @RequestBody TenantProfilePatchRequest req
    ) {
        Objects.requireNonNull(req, "请求体不能为空");

        TenantProfileEntity e = service.getById(tenantId);
        if (e == null) {
            throw new TenantProfileNotFoundException(tenantId);
        }

        if (req.mode() != null) {
            e.setMode(req.mode());
        }
        if (req.dsKey() != null && !req.dsKey().trim().isEmpty()) {
            e.setDsKey(req.dsKey().trim());
        }
        if (req.tenantLineEnabled() != null) {
            e.setTenantLineEnabled(req.tenantLineEnabled());
        }

        service.updateById(e);

        TenantProfileEntity latest = service.getById(tenantId);
        if (latest == null) {
            throw new IllegalStateException("更新成功但读取失败，tenantId=" + tenantId);
        }
        return latest;
    }

    /**
     * 删除租户画像（慎用）。
     *
     * @param tenantId 租户 ID
     * @return 是否删除成功
     */
    @Operation(
            summary = "删除租户画像",
            description = "删除 tenant_profile 记录（慎用）。若 requireTenant=true，被删租户的请求将因 profile 不存在而被拒绝。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功（true/false）",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)))
    })
    @DeleteMapping("/{tenantId}")
    public boolean delete(
            @Parameter(in = ParameterIn.PATH, description = "租户 ID", required = true, example = "tenant_10001")
            @PathVariable String tenantId
    ) {
        return service.removeById(tenantId);
    }

    /**
     * 租户画像不存在异常（用于返回 404）。
     *
     * @author Goya
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class TenantProfileNotFoundException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 4335462241923684705L;

        public TenantProfileNotFoundException(String tenantId) {
            super("租户画像不存在，tenantId=" + tenantId);
        }
    }
}
