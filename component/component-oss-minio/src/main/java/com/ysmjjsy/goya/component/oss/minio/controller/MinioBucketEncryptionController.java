package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import com.ysmjjsy.goya.component.oss.minio.request.bucket.DeleteBucketEncryptionRequest;
import com.ysmjjsy.goya.component.oss.minio.request.bucket.SetBucketEncryptionRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioBucketEncryptionService;
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
 * <p> Minio 存储桶加密方式接口 </p>
 *
 * @author goya
 * @since 2023/6/6 22:00
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/minio/bucket/encryption")
@Tag(name = "Minio存储桶加密方式管理")
public class MinioBucketEncryptionController implements IController {

    private final MinioBucketEncryptionService minioBucketEncryptionService;

    public MinioBucketEncryptionController(MinioBucketEncryptionService minioBucketEncryptionService) {
        this.minioBucketEncryptionService = minioBucketEncryptionService;
    }

    @Idempotent
    @Operation(summary = "设置存储桶加密方式策略", description = "设置存储桶加密方式策略",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "SetBucketEncryptionRequest请求参数实体", schema = @Schema(implementation = SetBucketEncryptionRequest.class))
    @PutMapping
    public ApiRes<Boolean> set(@Validated @RequestBody SetBucketEncryptionRequest request) {
        minioBucketEncryptionService.setBucketEncryption(request.build());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除存储桶加密方式策略", description = "删除存储桶加密方式策略",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "DeleteBucketEncryptionRequest请求参数实体", schema = @Schema(implementation = DeleteBucketEncryptionRequest.class))
    @DeleteMapping
    public ApiRes<Boolean> delete(@Validated @RequestBody DeleteBucketEncryptionRequest request) {
        minioBucketEncryptionService.deleteBucketEncryption(request.build());
        return response(true);
    }
}
