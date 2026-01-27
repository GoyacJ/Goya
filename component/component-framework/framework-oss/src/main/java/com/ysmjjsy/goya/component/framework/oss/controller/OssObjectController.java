package com.ysmjjsy.goya.component.framework.oss.controller;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.DeleteObjectArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.DeleteObjectsArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsV2Arguments;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.object.DeleteObjectDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ListObjectsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ListObjectsV2Domain;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.framework.servlet.idempotent.Idempotent;
import com.ysmjjsy.goya.component.framework.servlet.secure.AccessLimited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>对象存储管理接口-OSS统一对象管理接口</p>
 *
 * @author goya
 * @since 2025/11/3 09:49
 */
@RestController
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/oss/object")
@Tag(name = "Oss-统一对象管理")
public class OssObjectController implements IController {

    private final OssObjectRepository ossObjectRepository;

    public OssObjectController(OssObjectRepository ossObjectRepository) {
        this.ossObjectRepository = ossObjectRepository;
    }

    @AccessLimited
    @Operation(summary = "获取对象列表", description = "获取对象列表",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有对象", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ListObjectsDomain.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "ListObjectsArguments参数实体", schema = @Schema(implementation = ListObjectsArguments.class))
    @GetMapping("/list")
    public ApiRes<ListObjectsDomain> list(@Validated ListObjectsArguments arguments) {
        ListObjectsDomain domain = ossObjectRepository.listObjects(arguments);
        return response(domain);
    }

    @AccessLimited
    @Operation(summary = "获取对象列表V2", description = "获取对象列表V2",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "所有对象", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ListObjectsDomain.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "ListObjectsV2Arguments参数实体", schema = @Schema(implementation = ListObjectsV2Arguments.class))
    @GetMapping("/v2/list")
    public ApiRes<ListObjectsV2Domain> list(@Validated ListObjectsV2Arguments arguments) {
        ListObjectsV2Domain domain = ossObjectRepository.listObjectsV2(arguments);
        return response(domain);
    }

    @Idempotent
    @Operation(summary = "删除一个对象", description = "根据传入的参数对指定对象进行删除",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "Minio API无返回值，所以返回200即表示成功，不成功会抛错", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "500", description = "操作失败，具体查看错误信息内容"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "DeleteObjectArguments参数实体", schema = @Schema(implementation = DeleteObjectArguments.class))
    @DeleteMapping
    public ApiRes<Boolean> deleteObject(@Validated @RequestBody DeleteObjectArguments arguments) {
        ossObjectRepository.deleteObject(arguments);
        return response(true);
    }

    @Idempotent
    @Operation(summary = "删除多个对象", description = "根据传入的参数对指定多个对象进行删除",
            requestBody = @RequestBody(content = @Content(mediaType = "application/json")),
            responses = {
                    @ApiResponse(description = "返回删除操作出错对象的具体信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "200", description = "查询成功，查到数据"),
                    @ApiResponse(responseCode = "204", description = "查询成功，未查到数据"),
                    @ApiResponse(responseCode = "500", description = "查询失败"),
                    @ApiResponse(responseCode = "503", description = "Minio Server无法访问或未启动")
            })
    @Parameter(name = "arguments", required = true, description = "删除对象请求参数实体", schema = @Schema(implementation = DeleteObjectsArguments.class))
    @DeleteMapping("/multi")
    public ApiRes<List<DeleteObjectDomain>> removeObjects(@Validated @RequestBody DeleteObjectsArguments arguments) {
        List<DeleteObjectDomain> domains = ossObjectRepository.deleteObjects(arguments);
        return response(domains);
    }
}
