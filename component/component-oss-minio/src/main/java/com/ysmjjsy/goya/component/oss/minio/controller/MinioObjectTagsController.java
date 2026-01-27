package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.request.object.DeleteObjectTagsRequest;
import com.ysmjjsy.goya.component.oss.minio.request.object.SetObjectTagsRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioObjectTagsService;
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
 * <p> Minio 对象桶标签管理接口 </p>
 *
 * @author goya
 * @since 2023/6/10 15:13
 */
@RestController
@RequestMapping("/oss/minio/object/tags")
@Tag(name = "Minio对象桶标签管理")
public class MinioObjectTagsController implements IController {

    private final MinioObjectTagsService minioObjectTagsService;

    public MinioObjectTagsController(MinioObjectTagsService minioObjectTagsService) {
        this.minioObjectTagsService = minioObjectTagsService;
    }

    @Idempotent
    @Operation(summary = "设置对象标签", description = "设置对象标签",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "SetObjectTagsRequest请求参数实体", schema = @Schema(implementation = SetObjectTagsRequest.class))
    @PutMapping
    public Response<Boolean> set(@Validated @RequestBody SetObjectTagsRequest request) {
        minioObjectTagsService.setObjectTags(request.build());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "清空对象标签", description = "利用Tags的增减就可以实现Tags的删除，所以这个删除应该理解成清空",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "DeleteObjectTagsRequest请求参数实体", schema = @Schema(implementation = DeleteObjectTagsRequest.class))
    @DeleteMapping
    public Response<Boolean> delete(@Validated @RequestBody DeleteObjectTagsRequest request) {
        minioObjectTagsService.deleteObjectTags(request.build());
        return response(true);
    }
}
