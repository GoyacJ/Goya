package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsV2Arguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

/**
 * <p>统一定义 ListObjectsV2Arguments 转 S3 ListObjectsV2Request 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:25
 */
public class ArgumentsToListObjectsV2RequestConverter extends ArgumentsToBucketConverter<ListObjectsV2Arguments, ListObjectsV2Request> {
    
    @Override
    public ListObjectsV2Request getInstance(ListObjectsV2Arguments arguments) {
        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(arguments.getBucketName());

        if (arguments.getPrefix() != null) {
            builder.prefix(arguments.getPrefix());
        }
        
        if (arguments.getContinuationToken() != null) {
            builder.continuationToken(arguments.getContinuationToken());
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
        
        if (arguments.getFetchOwner() != null) {
            builder.fetchOwner(arguments.getFetchOwner());
        }
        
        if (arguments.getMarker() != null) {
            builder.startAfter(arguments.getMarker());
        }

        return builder.build();
    }
}
