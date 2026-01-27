package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.ListPartsArguments;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;

/**
 * <p>统一定义 ListPartsArguments 转 S3 ListPartsRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:21
 */
public class ArgumentsToListPartsRequestConverter implements Converter<ListPartsArguments, ListPartsRequest> {
    
    @Override
    public ListPartsRequest convert(ListPartsArguments source) {
        ListPartsRequest.Builder builder = ListPartsRequest.builder()
                .bucket(source.getBucketName())
                .key(source.getObjectName())
                .uploadId(source.getUploadId());

        if (source.getMaxParts() != null) {
            builder.maxParts(source.getMaxParts());
        }
        
        if (source.getPartNumberMarker() != null) {
            builder.partNumberMarker(source.getPartNumberMarker());
        }

        return builder.build();
    }
}
