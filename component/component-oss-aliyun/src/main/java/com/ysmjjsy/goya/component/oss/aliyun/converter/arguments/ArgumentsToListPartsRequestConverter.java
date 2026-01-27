package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.ListPartsRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.ListPartsArguments;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>统一定义 ListPartsArguments 转 S3 ListPartsRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:21
 */
public class ArgumentsToListPartsRequestConverter implements Converter<ListPartsArguments, ListPartsRequest> {
    @Override
    public ListPartsRequest convert(ListPartsArguments source) {

        ListPartsRequest request = new ListPartsRequest(source.getBucketName(), source.getObjectName(), source.getUploadId());
        request.setMaxParts(source.getMaxParts());
        request.setPartNumberMarker(source.getPartNumberMarker());
        return request;
    }
}
