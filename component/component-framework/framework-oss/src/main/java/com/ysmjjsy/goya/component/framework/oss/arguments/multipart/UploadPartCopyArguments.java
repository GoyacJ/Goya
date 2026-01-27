package com.ysmjjsy.goya.component.framework.oss.arguments.multipart;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BasePartArguments;
import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>上传分片拷贝请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "上传分片拷贝请求参数实体", title = "上传分片拷贝请求参数实体")
public class UploadPartCopyArguments extends BasePartArguments {

    @Serial
    private static final long serialVersionUID = 6125073696768484323L;

    /**
     * ETag值反向匹配约束列表，该列表将复制请求约束为仅在源对象的ETag与任何指定的ETag约束值不匹配时执行。
     */
    @Schema(name = "ETag值反向匹配约束列表")
    private final List<String> nonmatchingEtagConstraints = new ArrayList<>();
    @Schema(name = "分片编号", description = "当前分片在所有分片中的编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "分片变化不能小于1")
    @Max(value = 10000, message = "分片变化不能大于10000")
    private int partNumber;
    @Schema(name = "目标存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标存储桶名称不能为空")
    @Length(min = 3, max = 62, message = "目标存储桶名称不能少于3个字符，不能大于63个字符")
    @Pattern(regexp = OssConstants.DNS_COMPATIBLE, message = "存储桶名称无法与DNS兼容")
    private String destinationBucketName;
    @NotBlank(message = "目的对象名称不能为空")
    @Schema(name = "目的对象名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String destinationObjectName;
    /**
     * ETag值匹配约束列表，该列表约束复制请求仅在源对象的ETag与指定的ETag值之一匹配时执行。
     */
    @Schema(name = "ETag值匹配约束列表")
    private List<String> matchingEtagConstraints = new ArrayList<>();
    /**
     * 可选字段，用于将复制请求限制为仅在源对象自指定日期以来已被修改时才执行。
     */
    @Schema(name = "修改时间匹配约束")
    private Date modifiedSinceConstraint;

    /**
     * 可选字段，用于将复制请求限制为仅在源对象自指定日期以来未修改时执行
     */
    @Schema(name = "修改时间反向匹配约束")
    private Date unmodifiedSinceConstraint;
}
