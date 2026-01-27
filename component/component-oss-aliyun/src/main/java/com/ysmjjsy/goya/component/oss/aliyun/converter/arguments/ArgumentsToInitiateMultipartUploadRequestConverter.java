package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.multipart.InitiateMultipartUploadArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 InitiateMultipartUploadArguments 转 S3 InitiateMultipartUploadRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 17:31
 */
public class ArgumentsToInitiateMultipartUploadRequestConverter extends ArgumentsToBucketConverter<InitiateMultipartUploadArguments, InitiateMultipartUploadRequest> {

    @Override
    public InitiateMultipartUploadRequest getInstance(InitiateMultipartUploadArguments arguments) {
        return new InitiateMultipartUploadRequest(arguments.getBucketName(), arguments.getObjectName());
    }
}
