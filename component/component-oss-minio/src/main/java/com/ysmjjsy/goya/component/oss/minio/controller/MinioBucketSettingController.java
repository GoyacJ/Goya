package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.oss.minio.bo.BucketSettingBusiness;
import com.ysmjjsy.goya.component.oss.minio.service.MinioBucketSettingService;
import com.ysmjjsy.goya.component.web.annotation.AccessLimited;
import com.ysmjjsy.goya.component.web.definition.IController;
import com.ysmjjsy.goya.component.web.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> 对象存储桶设置接口 </p>
 *
 * @author goya
 * @since 2023/6/5 22:31
 */
@RestController
@RequestMapping("/oss/minio/bucket/setting")
@Tag(name = "Minio对象存储桶设置")
public class MinioBucketSettingController implements IController {

    private final MinioBucketSettingService settingService;

    public MinioBucketSettingController(MinioBucketSettingService settingService) {
        this.settingService = settingService;
    }

    @AccessLimited
    @Operation(summary = "获取存储桶设置信息", description = "获取存储桶设置信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "存储桶设置信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BucketSettingBusiness.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败")
            })
    @Parameter(name = "bucketName", required = true, description = "存储桶名称")
    @GetMapping
    public Response<BucketSettingBusiness> get(@RequestParam(value = "bucketName") String bucketName) {
        BucketSettingBusiness entity = settingService.get(bucketName);
        return response(entity);
    }
}
