package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.AbortMultipartUploadArguments;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;

/**
 * <p>统一定义 AbortMultipartUploadArguments 转 S3 AbortMultipartUploadRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:17
 */
public class ArgumentsToAbortMultipartUploadRequestConverter implements Converter<AbortMultipartUploadArguments, AbortMultipartUploadRequest> {
    
    @Override
    public AbortMultipartUploadRequest convert(AbortMultipartUploadArguments source) {
        return AbortMultipartUploadRequest.builder()
                .bucket(source.getBucketName())
                .key(source.getObjectName())
                .uploadId(source.getUploadId())
                .build();
    }
}
