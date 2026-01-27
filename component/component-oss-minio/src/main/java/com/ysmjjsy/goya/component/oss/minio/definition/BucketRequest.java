package com.ysmjjsy.goya.component.oss.minio.definition;

import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import io.minio.BucketArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * <p> Minio 基础 Bucket Dto </p>
 *
 * @author goya
 * @since 2022/7/1 23:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BucketRequest<B extends BucketArgs.Builder<B, A>, A extends BucketArgs> extends BaseRequest<B, A> {

    @Serial
    private static final long serialVersionUID = 7824153994804741786L;

    @Schema(name = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "存储桶名称不能为空")
    @Length(min = 3, max = 62, message = "存储桶名称不能少于3个字符，不能大于63个字符")
    @Pattern(regexp = OssConstants.DNS_COMPATIBLE, message = "存储桶名称无法与DNS兼容")
    private String bucketName;
    @Schema(name = "存储区域")
    private String region;
    @Override
    public void prepare(B builder) {
        builder.bucket(getBucketName());
        if (StringUtils.isNotBlank(getRegion())) {
            builder.region(getRegion());
        }

        super.prepare(builder);
    }
}
