package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.DeleteObjectArguments;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

/**
 * <p>统一定义 DeleteObjectArguments 转 S3 DeleteObjectRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:35
 */
public class ArgumentsToDeleteObjectRequestConverter implements Converter<DeleteObjectArguments, DeleteObjectRequest> {
    
    @Override
    public DeleteObjectRequest convert(DeleteObjectArguments source) {
        DeleteObjectRequest.Builder builder = DeleteObjectRequest.builder()
                .bucket(source.getBucketName())
                .key(source.getObjectName());

        if (source.getVersionId() != null) {
            builder.versionId(source.getVersionId());
        }
        
        if (source.getBypassGovernanceMode() != null && source.getBypassGovernanceMode()) {
            builder.bypassGovernanceRetention(source.getBypassGovernanceMode());
        }

        return builder.build();
    }
}
