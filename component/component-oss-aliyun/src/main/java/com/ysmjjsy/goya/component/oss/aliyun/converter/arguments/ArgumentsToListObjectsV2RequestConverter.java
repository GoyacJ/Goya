package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.ListObjectsV2Request;
import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsV2Arguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 ListObjectsV2Arguments 转 S3 ListObjectsV2Request 转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 19:31
 */
public class ArgumentsToListObjectsV2RequestConverter extends ArgumentsToBucketConverter<ListObjectsV2Arguments, ListObjectsV2Request> {

    @Override
    public ListObjectsV2Request getInstance(ListObjectsV2Arguments arguments) {
        return new ListObjectsV2Request(arguments.getBucketName(), arguments.getPrefix(), arguments.getContinuationToken(), arguments.getMarker(), arguments.getDelimiter(), arguments.getMaxKeys(), arguments.getEncodingType(), arguments.getFetchOwner());
    }
}
