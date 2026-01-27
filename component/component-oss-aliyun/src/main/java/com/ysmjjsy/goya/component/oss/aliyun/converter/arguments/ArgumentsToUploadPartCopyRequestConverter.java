package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.UploadPartCopyRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.multipart.UploadPartCopyArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 UploadPartCopyArguments 转 S3 CopyPartRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 17:46
 */
public class ArgumentsToUploadPartCopyRequestConverter extends ArgumentsToBucketConverter<UploadPartCopyArguments, UploadPartCopyRequest> {
    @Override
    public UploadPartCopyRequest getInstance(UploadPartCopyArguments arguments) {

        UploadPartCopyRequest request = new UploadPartCopyRequest();

        request.setSourceBucketName(arguments.getBucketName());
        request.setSourceKey(arguments.getObjectName());
        request.setUploadId(arguments.getUploadId());
        request.setPartNumber(arguments.getPartNumber());
        request.setBucketName(arguments.getDestinationBucketName());
        request.setKey(arguments.getDestinationObjectName());
        request.setMatchingETagConstraints(arguments.getMatchingEtagConstraints());
        request.setNonmatchingETagConstraints(arguments.getNonmatchingEtagConstraints());
        request.setModifiedSinceConstraint(arguments.getModifiedSinceConstraint());
        request.setUnmodifiedSinceConstraint(arguments.getUnmodifiedSinceConstraint());

        return request;
    }
}
