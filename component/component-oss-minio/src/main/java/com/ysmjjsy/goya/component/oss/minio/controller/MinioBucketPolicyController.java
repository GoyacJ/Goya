package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.request.bucket.DeleteBucketPolicyRequest;
import com.ysmjjsy.goya.component.oss.minio.request.bucket.SetBucketPolicyRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioBucketPolicyService;
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
 * <p> Minio 存储桶访问策略接口 </p>
 *
 * @author goya
 * @since 2023/6/6 21:45
 */
@RestController
@RequestMapping("/oss/minio/bucket/policy")
@Tag(name = "Minio存储桶访问策略管理")
public class MinioBucketPolicyController implements IController {

    private final MinioBucketPolicyService minioBucketPolicyService;

    public MinioBucketPolicyController(MinioBucketPolicyService minioBucketPolicyService) {
        this.minioBucketPolicyService = minioBucketPolicyService;
    }

    @Idempotent
    @Operation(summary = "设置存储桶访问策略", description = "设置存储桶访问策略",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "SetBucketPolicyRequest请求参数实体", schema = @Schema(implementation = SetBucketPolicyRequest.class))
    @PutMapping
    public Response<Boolean> set(@Validated @RequestBody SetBucketPolicyRequest request) {
        minioBucketPolicyService.setBucketPolicy(request.build());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除存储桶访问策略", description = "删除存储桶访问策略",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "DeleteBucketPolicyRequest请求参数实体", schema = @Schema(implementation = DeleteBucketPolicyRequest.class))
    @DeleteMapping
    public Response<Boolean> delete(@Validated @RequestBody DeleteBucketPolicyRequest request) {
        minioBucketPolicyService.deleteBucketPolicy(request.build());
        return response(true);
    }
}
