package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.request.object.DisableObjectLegalHoldRequest;
import com.ysmjjsy.goya.component.oss.minio.request.object.EnableObjectLegalHoldRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioObjectLegalHoldService;
import com.ysmjjsy.goya.component.web.annotation.Idempotent;
import com.ysmjjsy.goya.component.web.definition.IController;
import com.ysmjjsy.goya.component.web.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> Minio 对象LegalHold管理接口 </p>
 *
 * @author goya
 * @since 2023/6/11 10:14
 */
@RestController
@RequestMapping("/oss/minio/object/legal-hold")
@Tag(name = "Minio对象LegalHold管理")
public class MinioObjectLegalHoldController implements IController {

    private final MinioObjectLegalHoldService minioObjectLegalHoldService;

    public MinioObjectLegalHoldController(MinioObjectLegalHoldService minioObjectLegalHoldService) {
        this.minioObjectLegalHoldService = minioObjectLegalHoldService;
    }

    @Idempotent
    @Operation(summary = "设置开启对象持有配置", description = "设置开启对象持有配置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "EnableObjectLegalHoldRequest请求参数实体", schema = @Schema(implementation = EnableObjectLegalHoldRequest.class))
    @PutMapping("/enable")
    public Response<Boolean> enable(@Validated @RequestBody EnableObjectLegalHoldRequest request) {
        minioObjectLegalHoldService.enableObjectLegalHold(request.build());
        return response(true);
    }

    @Idempotent
    @Operation(summary = "设置关闭对象持有配置", description = "设置关闭对象持有配置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API 无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server 无法访问或未启动")
            })
    @Parameter(name = "request", required = true, description = "DisableObjectLegalHoldRequest请求参数实体", schema = @Schema(implementation = DisableObjectLegalHoldRequest.class))
    @PutMapping("/disable")
    public Response<Boolean> disable(@Validated @RequestBody DisableObjectLegalHoldRequest request) {
        minioObjectLegalHoldService.disableObjectLegalHold(request.build());
        return response(true);
    }
}
