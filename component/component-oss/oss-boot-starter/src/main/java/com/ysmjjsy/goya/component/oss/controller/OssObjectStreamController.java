package com.ysmjjsy.goya.component.oss.controller;

import com.ysmjjsy.goya.component.oss.arguments.ObjectStreamDownloadArguments;
import com.ysmjjsy.goya.component.oss.core.domain.object.PutObjectDomain;
import com.ysmjjsy.goya.component.oss.service.OssObjectStreamService;
import com.ysmjjsy.goya.component.web.annotation.Idempotent;
import com.ysmjjsy.goya.component.web.definition.IController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>对象存储管理接口-OSS统一流式上传下载接口</p>
 *
 * @author goya
 * @since 2025/11/3 09:49
 */
@RestController
@RequestMapping("/oss/object/stream")
@Tag(name = "Oss-统一流式上传下载管理")
public class OssObjectStreamController implements IController {

    private static final Logger log = LoggerFactory.getLogger(OssObjectStreamController.class);

    private final OssObjectStreamService ossObjectStreamService;

    public OssObjectStreamController(OssObjectStreamService ossObjectStreamService) {
        this.ossObjectStreamService = ossObjectStreamService;
    }

    @Idempotent
    @Operation(summary = "流式下载", description = "后台返回响应流，下载对应的对象",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有对象", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败"),
                    @ApiResponse(responseCode = "503", description = "Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "ObjectStreamDownloadArguments参数实体", schema = @Schema(implementation = ObjectStreamDownloadArguments.class))
    @PostMapping("/download")
    public void download(@Validated @RequestBody ObjectStreamDownloadArguments arguments, HttpServletResponse response) {
        try {
            ossObjectStreamService.download(arguments.getBucketName(), arguments.getObjectName(), response);
        } catch (IOException e) {
            log.error("[Goya] |- Download file from minio catch error", e);
        }
    }

    @Idempotent
    @Operation(summary = "流式打开", description = "后台返回响应流，直接打开对应的对象",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有对象", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败"),
                    @ApiResponse(responseCode = "503", description = "Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "ObjectStreamDownloadArguments参数实体", schema = @Schema(implementation = ObjectStreamDownloadArguments.class))
    @PostMapping("/display")
    public void display(@Validated @RequestBody ObjectStreamDownloadArguments arguments, HttpServletResponse response) {
        try {
            ossObjectStreamService.display(arguments.getBucketName(), arguments.getObjectName(), response);
        } catch (IOException e) {
            log.error("[Goya] |- Download file from minio catch error", e);
        }
    }

    @Idempotent
    @Operation(summary = "流式文件上传", description = "以文件流的方式上传文件",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有对象", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PutObjectDomain.class))),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "bucketName", required = true, description = "存储桶名称")
    @Parameter(name = "file", required = true, description = "文件", schema = @Schema(implementation = MultipartFile.class))
    @PostMapping("/upload")
    public PutObjectDomain upload(@RequestParam(value = "bucketName") String bucketName, @RequestPart(value = "file") MultipartFile file) {
        return ossObjectStreamService.upload(bucketName, file);
    }
}
