package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.InitiateMultipartUploadArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;

/**
 * <p>统一定义 InitiateMultipartUploadArguments 转 S3 CreateMultipartUploadRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 17:31
 */
public class ArgumentsToInitiateMultipartUploadRequestConverter extends ArgumentsToBucketConverter<InitiateMultipartUploadArguments, CreateMultipartUploadRequest> {

    @Override
    public CreateMultipartUploadRequest getInstance(InitiateMultipartUploadArguments arguments) {
        return CreateMultipartUploadRequest.builder()
                .bucket(arguments.getBucketName())
                .key(arguments.getObjectName())
                .build();
    }
}
