package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimited;
import com.ysmjjsy.goya.component.oss.minio.converter.GroupInfoToDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.domain.GroupDomain;
import com.ysmjjsy.goya.component.oss.minio.domain.UserDomain;
import com.ysmjjsy.goya.component.oss.minio.service.MinioAdminGroupService;
import io.minio.admin.GroupInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.convert.converter.Converter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p> 对象存储管理接口-Minio-Minio 组管理接口 </p>
 *
 * @author goya
 * @since 2023/6/25 15:18
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/minio/admin/group")
@Tag(name = "Minio组管理")
public class MinioAdminGroupController implements IController {

    private final MinioAdminGroupService minioAdminGroupService;
    private final Converter<GroupInfo, GroupDomain> toDomain;

    public MinioAdminGroupController(MinioAdminGroupService minioAdminGroupService) {
        this.minioAdminGroupService = minioAdminGroupService;
        this.toDomain = new GroupInfoToDomainConverter();
    }

    @AccessLimited
    @Operation(summary = "获取全部组信息", description = "获取全部组信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有Buckets", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @GetMapping("/list")
    public ApiRes<List<String>> list() {
        List<String> groups = minioAdminGroupService.listGroups();
        return ApiRes.ok(groups, "查询成功");
    }

    @AccessLimited
    @Operation(summary = "获取组信息", description = "获取 Minio 组信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "组信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDomain.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "accessKey", required = true, description = "用户对应 AccessKey 标识")
    @GetMapping
    public ApiRes<GroupDomain> get(String group) {
        GroupInfo groupInfo = minioAdminGroupService.getGroupInfo(group);
        GroupDomain groupDomain = toDomain.convert(groupInfo);
        return response(groupDomain);
    }

    @Idempotent
    @Operation(summary = "创建或更新组", description = "创建或更新组",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "domain", required = true, description = "UserDomain实体", schema = @Schema(implementation = UserDomain.class))
    @PostMapping
    public ApiRes<Boolean> add(@Validated @RequestBody GroupDomain domain) {
        minioAdminGroupService.addUpdateGroup(domain.getName(), domain.getStatus(), domain.getMembers());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除组", description = "删除组信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "group", required = true, description = "组对应标识")
    @DeleteMapping
    public ApiRes<Boolean> remove(String group) {
        minioAdminGroupService.removeGroup(group);
        return response(true);
    }
}
