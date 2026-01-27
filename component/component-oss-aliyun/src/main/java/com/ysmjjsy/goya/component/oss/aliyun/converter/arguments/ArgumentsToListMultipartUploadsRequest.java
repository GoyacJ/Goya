package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.ListMultipartUploadsRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.ListMultipartUploadsArguments;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>统一定义 ListMultipartUploadsArguments 转 S3 ListMultipartUploadsRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:26
 */
public class ArgumentsToListMultipartUploadsRequest implements Converter<ListMultipartUploadsArguments, ListMultipartUploadsRequest> {
    @Override
    public ListMultipartUploadsRequest convert(ListMultipartUploadsArguments source) {

        ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(source.getBucketName());
        request.setDelimiter(source.getDelimiter());
        request.setPrefix(source.getPrefix());
        request.setMaxUploads(source.getMaxUploads());
        request.setKeyMarker(source.getKeyMarker());
        request.setUploadIdMarker(source.getUploadIdMarker());
        request.setEncodingType(source.getEncodingType());
        return request;
    }
}
