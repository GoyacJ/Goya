package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.GetObjectMetadataArguments;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.time.Instant;

/**
 * <p>统一定义 GetObjectMetadataArguments 转 S3 HeadObjectRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:50
 */
public class ArgumentsToHeadObjectRequestConverter implements Converter<GetObjectMetadataArguments, HeadObjectRequest> {
    
    @Override
    public HeadObjectRequest convert(GetObjectMetadataArguments source) {
        HeadObjectRequest.Builder builder = HeadObjectRequest.builder()
                .bucket(source.getBucketName())
                .key(source.getObjectName());

        if (source.getVersionId() != null) {
            builder.versionId(source.getVersionId());
        }
        
        if (source.getMatchEtag() != null && !source.getMatchEtag().isEmpty()) {
            builder.ifMatch(source.getMatchEtag().getFirst());
        }
        
        if (source.getNotMatchEtag() != null && !source.getNotMatchEtag().isEmpty()) {
            builder.ifNoneMatch(source.getNotMatchEtag().getFirst());
        }
        
        if (source.getModifiedSince() != null) {
            builder.ifModifiedSince(Instant.ofEpochMilli(source.getModifiedSince().getTime()));
        }
        
        if (source.getUnmodifiedSince() != null) {
            builder.ifUnmodifiedSince(Instant.ofEpochMilli(source.getUnmodifiedSince().getTime()));
        }
        
        if (source.getOffset() != null && source.getLength() != null) {
            String range = "bytes=" + source.getOffset() + "-" + (source.getOffset() + source.getLength() - 1);
            builder.range(range);
        } else if (source.getOffset() != null) {
            String range = "bytes=" + source.getOffset() + "-";
            builder.range(range);
        }

        return builder.build();
    }
}
