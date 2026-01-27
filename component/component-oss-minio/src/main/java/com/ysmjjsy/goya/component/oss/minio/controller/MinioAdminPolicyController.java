package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimited;
import com.ysmjjsy.goya.component.oss.minio.domain.UserDomain;
import com.ysmjjsy.goya.component.oss.minio.service.MinioAdminPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p> Minio 屏蔽策略管理接口 </p>
 *
 * @author goya
 * @since 2023/6/25 16:02
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/minio/admin/policy")
@Tag(name = "Minio屏蔽策略管理")
public class MinioAdminPolicyController implements IController {

    private final MinioAdminPolicyService minioAdminPolicyService;

    public MinioAdminPolicyController(MinioAdminPolicyService minioAdminPolicyService) {
        this.minioAdminPolicyService = minioAdminPolicyService;
    }

    @AccessLimited
    @Operation(summary = "获取全部屏蔽策略信息", description = "获取全部屏蔽策略信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有屏蔽策略信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @GetMapping("/list")
    public ApiRes<Map<String, String>> list() {
        Map<String, String> policies = minioAdminPolicyService.listCannedPolicies();
        return ApiRes.ok(policies,"查询成功");
    }

    @Idempotent
    @Operation(summary = "添加屏蔽策略信息", description = "添加屏蔽策略信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "domain", required = true, description = "UserDomain实体", schema = @Schema(implementation = UserDomain.class))
    @PostMapping
    public ApiRes<Boolean> add(@RequestParam(value = "name") String name, @RequestParam(value = "policy") String policy) {
        minioAdminPolicyService.addCannedPolicy(name, policy);
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除屏蔽策略信息", description = "删除屏蔽策略信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "name", required = true, description = "屏蔽策略名称")
    @DeleteMapping
    public ApiRes<Boolean> remove(String name) {
        minioAdminPolicyService.removeCannedPolicy(name);
        return response(true);
    }
}
