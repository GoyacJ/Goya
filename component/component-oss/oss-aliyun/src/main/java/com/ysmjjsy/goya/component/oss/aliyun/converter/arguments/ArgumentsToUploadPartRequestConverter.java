package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.UploadPartRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.multipart.UploadPartArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 UploadPartArguments 转 S3 UploadPartRequest 转换器  </p>
 *
 * @author goya
 * @since 2023/8/14 17:37
 */
public class ArgumentsToUploadPartRequestConverter extends ArgumentsToBucketConverter<UploadPartArguments, UploadPartRequest> {
    @Override
    public UploadPartRequest getInstance(UploadPartArguments arguments) {

        UploadPartRequest request = new UploadPartRequest();

        request.setBucketName(arguments.getBucketName());
        request.setKey(arguments.getObjectName());
        request.setUploadId(arguments.getUploadId());
        request.setPartNumber(arguments.getPartNumber());
        request.setPartSize(arguments.getPartSize());
        request.setInputStream(arguments.getInputStream());
        request.setMd5Digest(arguments.getMd5Digest());

        return request;
    }
}
