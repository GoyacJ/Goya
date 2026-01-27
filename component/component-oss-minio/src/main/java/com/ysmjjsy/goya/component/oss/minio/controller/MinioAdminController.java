package com.ysmjjsy.goya.component.oss.minio.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimited;
import com.ysmjjsy.goya.component.oss.minio.service.MinioAdminService;
import io.minio.admin.messages.DataUsageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> 对象存储管理接口-Minio-Minio 统计信息接口 </p>
 *
 * @author goya
 * @since 2023/6/28 14:58
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/minio/admin")
@Tag(name = "Minio统计信息")
public class MinioAdminController {

    private final MinioAdminService minioAdminService;

    public MinioAdminController(MinioAdminService minioAdminService) {
        this.minioAdminService = minioAdminService;
    }

    @AccessLimited
    @Operation(summary = "获取 Minio 统计信息", description = "获取 Minio 统计信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "统计信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataUsageInfo.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @GetMapping("/usage")
    public ApiRes<DataUsageInfo> dataUsageInfo() {
        DataUsageInfo info = minioAdminService.getDataUsageInfo();
        return ApiRes.ok(info, "查询成功");
    }
}
