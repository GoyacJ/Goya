package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.UploadPartArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

/**
 * <p>统一定义 UploadPartArguments 转 S3 UploadPartRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 17:37
 */
public class ArgumentsToUploadPartRequestConverter extends ArgumentsToBucketConverter<UploadPartArguments, UploadPartRequest> {
    
    @Override
    public UploadPartRequest getInstance(UploadPartArguments arguments) {
        UploadPartRequest.Builder builder = UploadPartRequest.builder()
                .bucket(arguments.getBucketName())
                .key(arguments.getObjectName())
                .uploadId(arguments.getUploadId())
                .partNumber(arguments.getPartNumber());

        if (arguments.getPartSize() != null) {
            builder.contentLength(arguments.getPartSize());
        }

        return builder.build();
    }
}
