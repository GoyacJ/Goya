package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.ListMultipartUploadsArguments;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsRequest;

/**
 * <p>统一定义 ListMultipartUploadsArguments 转 S3 ListMultipartUploadsRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:26
 */
public class ArgumentsToListMultipartUploadsRequestConverter implements Converter<ListMultipartUploadsArguments, ListMultipartUploadsRequest> {
    
    @Override
    public ListMultipartUploadsRequest convert(ListMultipartUploadsArguments source) {
        ListMultipartUploadsRequest.Builder builder = ListMultipartUploadsRequest.builder()
                .bucket(source.getBucketName());

        if (source.getDelimiter() != null) {
            builder.delimiter(source.getDelimiter());
        }
        
        if (source.getPrefix() != null) {
            builder.prefix(source.getPrefix());
        }
        
        if (source.getMaxUploads() != null) {
            builder.maxUploads(source.getMaxUploads());
        }
        
        if (source.getKeyMarker() != null) {
            builder.keyMarker(source.getKeyMarker());
        }
        
        if (source.getUploadIdMarker() != null) {
            builder.uploadIdMarker(source.getUploadIdMarker());
        }
        
        if (source.getEncodingType() != null) {
            builder.encodingType(source.getEncodingType());
        }

        return builder.build();
    }
}
