package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.converter.UserInfoToDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.converter.UsersToDomainsConverter;
import com.ysmjjsy.goya.component.oss.minio.domain.UserDomain;
import com.ysmjjsy.goya.component.oss.minio.service.MinioAdminUserService;
import com.ysmjjsy.goya.component.web.annotation.AccessLimited;
import com.ysmjjsy.goya.component.web.annotation.Idempotent;
import com.ysmjjsy.goya.component.web.definition.IController;
import com.ysmjjsy.goya.component.web.response.Response;
import io.minio.admin.UserInfo;
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
import java.util.Map;

/**
 * <p> Minio 用户管理接口 </p>
 *
 * @author goya
 * @since 2023/6/25 14:06
 */
@RestController
@RequestMapping("/oss/minio/admin/user")
@Tag(name = "Minio用户管理")
public class MinioAdminUserController implements IController {

    private final MinioAdminUserService minioAdminUserService;
    private final Converter<Map<String, UserInfo>, List<UserDomain>> toDomains;
    private final Converter<UserInfo, UserDomain> toDomain;

    public MinioAdminUserController(MinioAdminUserService minioAdminUserService) {
        this.minioAdminUserService = minioAdminUserService;
        this.toDomains = new UsersToDomainsConverter();
        this.toDomain = new UserInfoToDomainConverter();
    }

    @AccessLimited
    @Operation(summary = "获取全部用户信息", description = "获取全部用户信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有Buckets", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @GetMapping("/list")
    public Response<List<UserDomain>> list() {
        Map<String, UserInfo> users = minioAdminUserService.listUsers();
        List<UserDomain> domains = toDomains.convert(users);
        return response(domains);
    }

    @AccessLimited
    @Operation(summary = "获取用户信息", description = "根据用户 AccessKey 获取 Minio 用户信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "用户信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDomain.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "accessKey", required = true, description = "用户对应 AccessKey 标识")
    @GetMapping
    public Response<UserDomain> get(String accessKey) {
        UserInfo userInfo = minioAdminUserService.getUserInfo(accessKey);
        UserDomain userDomain = toDomain.convert(userInfo);
        return response(userDomain);
    }

    @Idempotent
    @Operation(summary = "创建用户", description = "创建 Minio 用户",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "domain", required = true, description = "UserDomain实体", schema = @Schema(implementation = UserDomain.class))
    @PostMapping
    public Response<Boolean> add(@Validated @RequestBody UserDomain domain) {
        minioAdminUserService.addUser(domain.getAccessKey(), UserInfo.Status.fromString(domain.getStatus().name()), domain.getSecretKey(), domain.getPolicyName(), domain.getMemberOf());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除用户", description = "根据用户 AccessKey 删除用户信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "accessKey", required = true, description = "用户对应 AccessKey 标识")
    @DeleteMapping
    public Response<Boolean> remove(String accessKey) {
        minioAdminUserService.deleteUser(accessKey);
        return response(true);
    }
}
