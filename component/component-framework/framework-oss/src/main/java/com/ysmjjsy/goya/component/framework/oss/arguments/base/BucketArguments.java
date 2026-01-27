package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * <p>基础的存储桶请求参数定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BucketArguments extends BaseArguments {

    @Serial
    private static final long serialVersionUID = 7726189762942177731L;

    @Schema(name = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    @Length(min = 3, max = 62, message = "存储桶名称不能少于3个字符，不能大于63个字符")
    @Pattern(regexp = OssConstants.DNS_COMPATIBLE, message = "存储桶名称无法与DNS兼容")
    private String bucketName;
    @Schema(name = "存储区域", description = "仅在Minio环境下使用，Amazon S3 已废弃")
    private String region;
}
