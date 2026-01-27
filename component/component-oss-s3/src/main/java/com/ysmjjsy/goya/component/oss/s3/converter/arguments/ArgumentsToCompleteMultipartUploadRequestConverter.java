package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.CompleteMultipartUploadArguments;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import org.apache.commons.collections4.CollectionUtils;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>统一定义 CompleteMultipartUploadArguments 转 S3 CompleteMultipartUploadRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 17:58
 */
public class ArgumentsToCompleteMultipartUploadRequestConverter extends ArgumentsToBucketConverter<CompleteMultipartUploadArguments, CompleteMultipartUploadRequest> {
    
    @Override
    public CompleteMultipartUploadRequest getInstance(CompleteMultipartUploadArguments arguments) {
        List<CompletedPart> completedParts = convertParts(arguments.getParts());

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        return CompleteMultipartUploadRequest.builder()
                .bucket(arguments.getBucketName())
                .key(arguments.getObjectName())
                .uploadId(arguments.getUploadId())
                .multipartUpload(completedMultipartUpload)
                .build();
    }

    private List<CompletedPart> convertParts(List<PartSummaryDomain> parts) {
        if (CollectionUtils.isNotEmpty(parts)) {
            return parts.stream()
                    .map(part -> CompletedPart.builder()
                            .partNumber(part.getPartNumber())
                            .eTag(part.getEtag())
                            .build())
                    .toList();
        }
        return new ArrayList<>();
    }
}
