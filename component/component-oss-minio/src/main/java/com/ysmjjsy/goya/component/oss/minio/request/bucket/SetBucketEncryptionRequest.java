package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import com.ysmjjsy.goya.component.oss.minio.enums.SseConfigurationEnums;
import io.minio.SetBucketEncryptionArgs;
import io.minio.messages.SseConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serial;

/**
 * <p> 设置存储桶加密方式请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 22:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置存储桶加密方式请求参数实体", title = "设置存储桶加密方式请求参数实体")
public class SetBucketEncryptionRequest extends BucketRequest<SetBucketEncryptionArgs.Builder, SetBucketEncryptionArgs> {

    @Serial
    private static final long serialVersionUID = 6417141984616966070L;

    @Schema(name = "服务端加密算法", description = "1：为AWS_KMS；2：为AES256", requiredMode = Schema.RequiredMode.REQUIRED)
//    @EnumeratedValue(names = {"AWS_KMS", "AES256"}, message = "设置存储桶加密方式，必须为有效加密方式， 不能为 0 (Disabled)")
    private SseConfigurationEnums sseConfiguration;

    @Schema(name = "KMS加密MasterKeyId", description = "可选参数，主要用于AWS_KMS加密算法")
    private String kmsMasterKeyId;

    @Override
    public void prepare(SetBucketEncryptionArgs.Builder builder) {
        SseConfigurationEnums enums = getSseConfiguration();
        if (ObjectUtils.isNotEmpty(enums)) {
            if (enums == SseConfigurationEnums.AES256) {
                builder.config(SseConfiguration.newConfigWithSseS3Rule());
            } else {
                builder.config(SseConfiguration.newConfigWithSseKmsRule(getKmsMasterKeyId()));
            }
        }
        super.prepare(builder);
    }

    @Override
    public SetBucketEncryptionArgs.Builder getBuilder() {
        return SetBucketEncryptionArgs.builder();
    }
}
