package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

/**
 * <p>统一定义 ListObjectsArguments 转 S3 ListObjectsRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:15
 */
public class ArgumentsToListObjectsRequestConverter extends ArgumentsToBucketConverter<ListObjectsArguments, ListObjectsRequest> {
    
    @Override
    public ListObjectsRequest getInstance(ListObjectsArguments arguments) {
        ListObjectsRequest.Builder builder = ListObjectsRequest.builder()
                .bucket(arguments.getBucketName());

        if (arguments.getPrefix() != null) {
            builder.prefix(arguments.getPrefix());
        }
        
        if (arguments.getMarker() != null) {
            builder.marker(arguments.getMarker());
        }
        
        if (arguments.getDelimiter() != null) {
            builder.delimiter(arguments.getDelimiter());
        }
        
        if (arguments.getMaxKeys() != null) {
            builder.maxKeys(arguments.getMaxKeys());
        }
        
        if (arguments.getEncodingType() != null) {
            builder.encodingType(arguments.getEncodingType());
        }

        return builder.build();
    }
}
