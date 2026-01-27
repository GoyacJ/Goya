package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.UploadPartCopyArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;

import java.time.Instant;

/**
 * <p>统一定义 UploadPartCopyArguments 转 S3 UploadPartCopyRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 17:46
 */
public class ArgumentsToCopyPartRequestConverter extends ArgumentsToBucketConverter<UploadPartCopyArguments, UploadPartCopyRequest> {
    
    @Override
    public UploadPartCopyRequest getInstance(UploadPartCopyArguments arguments) {
        UploadPartCopyRequest.Builder builder = UploadPartCopyRequest.builder()
                .sourceBucket(arguments.getBucketName())
                .sourceKey(arguments.getObjectName())
                .destinationBucket(arguments.getDestinationBucketName())
                .destinationKey(arguments.getDestinationObjectName())
                .uploadId(arguments.getUploadId())
                .partNumber(arguments.getPartNumber());

        if (arguments.getMatchingEtagConstraints() != null && !arguments.getMatchingEtagConstraints().isEmpty()) {
            builder.copySourceIfMatch(arguments.getMatchingEtagConstraints().getFirst());
        }
        
        if (arguments.getNonmatchingEtagConstraints() != null && !arguments.getNonmatchingEtagConstraints().isEmpty()) {
            builder.copySourceIfNoneMatch(arguments.getNonmatchingEtagConstraints().getFirst());
        }
        
        if (arguments.getModifiedSinceConstraint() != null) {
            builder.copySourceIfModifiedSince(Instant.ofEpochMilli(arguments.getModifiedSinceConstraint().getTime()));
        }
        
        if (arguments.getUnmodifiedSinceConstraint() != null) {
            builder.copySourceIfUnmodifiedSince(Instant.ofEpochMilli(arguments.getUnmodifiedSinceConstraint().getTime()));
        }

        return builder.build();
    }
}
