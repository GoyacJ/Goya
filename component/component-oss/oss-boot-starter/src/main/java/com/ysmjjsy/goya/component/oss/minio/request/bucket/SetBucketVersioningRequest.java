package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import com.ysmjjsy.goya.component.oss.minio.domain.VersioningConfigurationDomain;
import io.minio.SetBucketVersioningArgs;
import io.minio.messages.VersioningConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 设置存储桶版本请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/28 17:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置存储桶版本请求参数实体", title = "设置存储桶版本请求参数实体")
public class SetBucketVersioningRequest extends BucketRequest<SetBucketVersioningArgs.Builder, SetBucketVersioningArgs> {

    @Serial
    private static final long serialVersionUID = 6515802258831232579L;

    @Schema(name = "存储桶版本配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "存储桶版本配置不能为空")
    private VersioningConfigurationDomain config;

    @Override
    public void prepare(SetBucketVersioningArgs.Builder builder) {
        builder.config(new VersioningConfiguration(VersioningConfiguration.Status.valueOf(config.getStatus()), config.getMfaDelete()));
        super.prepare(builder);
    }

    @Override
    public SetBucketVersioningArgs.Builder getBuilder() {
        return SetBucketVersioningArgs.builder();
    }
}
