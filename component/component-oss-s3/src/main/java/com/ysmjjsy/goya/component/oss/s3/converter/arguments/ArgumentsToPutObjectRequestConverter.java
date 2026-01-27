package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.PutObjectArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

/**
 * <p>统一定义 PutObjectArguments 转 S3 PutObjectRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 22:10
 */
public class ArgumentsToPutObjectRequestConverter extends ArgumentsToBucketConverter<PutObjectArguments, PutObjectRequest> {
    
    @Override
    public PutObjectRequest getInstance(PutObjectArguments arguments) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(arguments.getBucketName())
                .key(arguments.getObjectName());

        if (arguments.getContentType() != null) {
            builder.contentType(arguments.getContentType());
        }
        
        if (arguments.getMetadata() != null && !arguments.getMetadata().isEmpty()) {
            builder.metadata(arguments.getMetadata());
        }
        
        if (arguments.getRequestHeaders() != null && !arguments.getRequestHeaders().isEmpty()) {
            Map<String, String> headers = arguments.getRequestHeaders();
            builder.overrideConfiguration(overrideConfig -> headers.forEach(overrideConfig::putHeader));
        }

        return builder.build();
    }
}
