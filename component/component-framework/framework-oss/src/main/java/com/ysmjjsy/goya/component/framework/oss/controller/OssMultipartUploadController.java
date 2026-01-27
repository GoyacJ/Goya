package com.ysmjjsy.goya.component.framework.oss.controller;

import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.oss.arguments.CompleteMultipartUploadArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.CreateMultipartUploadArguments;
import com.ysmjjsy.goya.component.framework.oss.business.CreateMultipartUploadBusiness;
import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import com.ysmjjsy.goya.component.framework.oss.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.CompleteMultipartUploadDomain;
import com.ysmjjsy.goya.component.framework.oss.proxy.OssPresignedUrlProxy;
import com.ysmjjsy.goya.component.framework.oss.service.OssMultipartUploadService;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>对象存储管理接口-OSS统一大文件分片接口</p>
 *
 * @author goya
 * @since 2025/11/3 09:49
 */
@RestController
@RequestMapping(OssConstants.OSS_MULTIPART_UPLOAD_REQUEST_MAPPING)
@Tag(name = "Oss-统一大文件分片")
public class OssMultipartUploadController implements IController {

    private final OssMultipartUploadService ossMultipartUploadService;
    private final OssPresignedUrlProxy ossPresignedUrlProxy;

    public OssMultipartUploadController(OssMultipartUploadService ossMultipartUploadService, OssPresignedUrlProxy ossPresignedUrlProxy) {
        this.ossMultipartUploadService = ossMultipartUploadService;
        this.ossPresignedUrlProxy = ossPresignedUrlProxy;
    }

    @Idempotent
    @Operation(summary = "创建分片上传信息", description = "创建分片上传信息",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "uploadId 和 预下载地址", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateMultipartUploadBusiness.class))),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "204", description = "无结果"),
                    @ApiResponse(responseCode = "500", description = "操作失败")
            })
    @Parameter(name = "arguments", required = true, description = "CreateMultipartUploadArguments参数实体", schema = @Schema(implementation = CreateMultipartUploadArguments.class))
    @PostMapping("/create")
    public ApiRes<CreateMultipartUploadBusiness> createMultipartUpload(@Validated @RequestBody CreateMultipartUploadArguments arguments) {
        CreateMultipartUploadBusiness result = ossMultipartUploadService.createMultipartUpload(arguments.getBucketName(), arguments.getObjectName(), arguments.getPartNumber());
        return response(result);
    }

    @Idempotent
    @Operation(summary = "完成分片上传", description = "完成分片上传，Minio将上传完成的分片信息进行合并",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "操作结果", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ObjectWriteDomain.class))),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "204", description = "无结果"),
                    @ApiResponse(responseCode = "500", description = "操作失败")
            })
    @Parameter(name = "arguments", required = true, description = "CompleteMultipartUploadArguments参数实体", schema = @Schema(implementation = CompleteMultipartUploadArguments.class))
    @PostMapping("/complete")
    public ApiRes<CompleteMultipartUploadDomain> completeMultipartUpload(@Validated @RequestBody CompleteMultipartUploadArguments arguments) {
        CompleteMultipartUploadDomain entity = ossMultipartUploadService.completeMultipartUpload(arguments.getBucketName(), arguments.getObjectName(), arguments.getUploadId());
        return response(entity);
    }

    @Operation(summary = "预下载代理地址", description = "预下载代理地址，避免前端直接访问OSS，同时导致微服务寻址错误",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "操作结果", content = @Content(mediaType = "application/json")),
            })
    @PutMapping(value = OssConstants.OSS_PRESIGNED_OBJECT_PROXY_REQUEST_MAPPING)
    public ResponseEntity<String> presignedUrlProxy(HttpServletRequest request) {
        return ossPresignedUrlProxy.delegate(request);
    }
}