package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.ListObjectsRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 ListObjectsArguments 转 S3 ListObjectsRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 19:31
 */
public class ArgumentsToListObjectsRequestConverter extends ArgumentsToBucketConverter<ListObjectsArguments, ListObjectsRequest> {

    @Override
    public ListObjectsRequest getInstance(ListObjectsArguments arguments) {
        return new ListObjectsRequest(arguments.getBucketName(), arguments.getPrefix(), arguments.getMarker(), arguments.getDelimiter(), arguments.getMaxKeys());
    }
}
