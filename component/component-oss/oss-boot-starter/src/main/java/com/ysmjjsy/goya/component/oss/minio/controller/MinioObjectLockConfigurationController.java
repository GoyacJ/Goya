package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.request.bucket.DeleteObjectLockConfigurationRequest;
import com.ysmjjsy.goya.component.oss.minio.request.bucket.SetObjectLockConfigurationRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioObjectLockConfigurationService;
import com.ysmjjsy.goya.component.web.annotation.Idempotent;
import com.ysmjjsy.goya.component.web.definition.IController;
import com.ysmjjsy.goya.component.web.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> Minio 存储桶对象锁定管理接口 </p>
 *
 * @author goya
 * @since 2023/6/6 23:37
 */
@RestController
@RequestMapping("/oss/minio/bucket/object-lock")
@Tag(name = "Minio存储桶对象锁定管理")
public class MinioObjectLockConfigurationController implements IController {

    private final MinioObjectLockConfigurationService minioObjectLockConfigurationService;

    public MinioObjectLockConfigurationController(MinioObjectLockConfigurationService minioObjectLockConfigurationService) {
        this.minioObjectLockConfigurationService = minioObjectLockConfigurationService;
    }

    @Idempotent
    @Operation(summary = "设置存储桶对象锁定配置", description = "设置存储桶对象锁定配置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "SetObjectLockConfigurationRequest请求参数实体", schema = @Schema(implementation = SetObjectLockConfigurationRequest.class))
    @PutMapping
    public Response<Boolean> set(@Validated @RequestBody SetObjectLockConfigurationRequest request) {
        minioObjectLockConfigurationService.setObjectLockConfiguration(request.build());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除存储桶对象锁定配置", description = "删除存储桶对象锁定配置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "DeleteObjectLockConfigurationRequest请求参数实体", schema = @Schema(implementation = DeleteObjectLockConfigurationRequest.class))
    @DeleteMapping
    public Response<Boolean> delete(@Validated @RequestBody DeleteObjectLockConfigurationRequest request) {
        minioObjectLockConfigurationService.deleteObjectLockConfiguration(request.build());
        return response(true);
    }
}
