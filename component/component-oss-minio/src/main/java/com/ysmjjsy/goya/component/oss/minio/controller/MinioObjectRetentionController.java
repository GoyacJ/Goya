package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import com.ysmjjsy.goya.component.oss.minio.request.object.SetObjectRetentionRequest;
import com.ysmjjsy.goya.component.oss.minio.service.MinioObjectRetentionService;
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
 * <p> 对象存储对象管理接口 </p>
 *
 * @author goya
 * @since 2023/4/16 21:29
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/minio/object/retention")
@Tag(name = "Minio Object Retention管理")
public class MinioObjectRetentionController implements IController {

    private final MinioObjectRetentionService minioObjectRetentionService;

    public MinioObjectRetentionController(MinioObjectRetentionService minioObjectRetentionService) {
        this.minioObjectRetentionService = minioObjectRetentionService;
    }

    @Idempotent
    @Operation(summary = "设置对象的保留配置", description = "设置对象的保留配置",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {@ApiResponse(description = "已保存数据", content = @Content(mediaType = "application/json"))})
    @Parameter(name = "request", required = true, description = "设置对象保留配置请求参数实体", schema = @Schema(implementation = SetObjectRetentionRequest.class))
    @PutMapping
    public ApiRes<Boolean> set(@Validated @RequestBody SetObjectRetentionRequest request) {
        minioObjectRetentionService.setObjectRetention(request.build());
        return response(true);
    }
}
